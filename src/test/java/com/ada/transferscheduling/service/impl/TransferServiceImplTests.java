package com.ada.transferscheduling.service.impl;

import com.ada.transferscheduling.dto.request.ScheduleTransferRequest;
import com.ada.transferscheduling.dto.response.TransferResponse;
import com.ada.transferscheduling.entity.Transfer;
import com.ada.transferscheduling.entity.TransferStatus;
import com.ada.transferscheduling.exception.AccountNotFoundException;
import com.ada.transferscheduling.exception.InvalidTransferException;
import com.ada.transferscheduling.exception.TransferNotFoundException;
import com.ada.transferscheduling.mapper.TransferMapper;
import com.ada.transferscheduling.repository.AccountRepository;
import com.ada.transferscheduling.repository.TransferRepository;
import com.ada.transferscheduling.service.TransferFeeCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTests {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private TransferFeeCalculator transferFeeCalculator;

    @InjectMocks
    private TransferServiceImpl transferService;

    private ScheduleTransferRequest request;
    private LocalDateTime transferDate;
    private Transfer mappedTransfer;
    private Transfer savedTransfer;
    private TransferResponse response;

    @BeforeEach
    void setUp() {
        transferDate = LocalDateTime.now().plusDays(2);

        request = ScheduleTransferRequest.builder()
                .sourceAccount("00001-1")
                .destinationAccount("00002-2")
                .amount(new BigDecimal("1000.00"))
                .transferDate(transferDate)
                .build();

        mappedTransfer = Transfer.builder()
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .transferDate(request.getTransferDate())
                .build();

        savedTransfer = Transfer.builder()
                .id(10L)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .fee(new BigDecimal("10.00"))
                .scheduledDate(LocalDateTime.now())
                .transferDate(request.getTransferDate())
                .status(TransferStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        response = TransferResponse.builder()
                .id(10L)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .fee(savedTransfer.getFee())
                .scheduledDate(savedTransfer.getScheduledDate())
                .transferDate(request.getTransferDate())
                .status(savedTransfer.getStatus())
                .createdAt(savedTransfer.getCreatedAt())
                .build();
    }

    @Test
    void scheduleShouldPersistAndReturnResponse() {
        // arrange
        when(accountRepository.existsByAccountNumber(request.getSourceAccount())).thenReturn(true);
        when(accountRepository.existsByAccountNumber(request.getDestinationAccount())).thenReturn(true);
        when(transferFeeCalculator.calculate(eq(request.getAmount()), any(LocalDateTime.class), eq(request.getTransferDate())))
                .thenReturn(savedTransfer.getFee());
        when(transferMapper.toEntity(request)).thenReturn(mappedTransfer);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(transferMapper.toResponse(savedTransfer)).thenReturn(response);

        // act
        TransferResponse result = transferService.schedule(request);

        // assert
        assertSame(response, result);
        verify(accountRepository).existsByAccountNumber(request.getSourceAccount());
        verify(accountRepository).existsByAccountNumber(request.getDestinationAccount());
        verify(transferFeeCalculator).calculate(eq(request.getAmount()), any(LocalDateTime.class), eq(request.getTransferDate()));
        verify(transferMapper).toEntity(request);
        verify(transferRepository).save(any(Transfer.class));
        verify(transferMapper).toResponse(savedTransfer);
    }

    @Test
    void scheduleShouldFailWhenSourceAccountDoesNotExist() {
        // arrange
        when(accountRepository.existsByAccountNumber(request.getSourceAccount())).thenReturn(false);

        // act
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> transferService.schedule(request));

        // assert
        assertEquals("Conta não encontrada para o número da conta: " + request.getSourceAccount(), exception.getMessage());
        verify(accountRepository).existsByAccountNumber(request.getSourceAccount());
        verify(accountRepository, never()).existsByAccountNumber(request.getDestinationAccount());
        verifyNoInteractions(transferFeeCalculator, transferMapper, transferRepository);
    }

    @Test
    void scheduleShouldFailWhenDestinationAccountDoesNotExist() {
        // arrange
        when(accountRepository.existsByAccountNumber(request.getSourceAccount())).thenReturn(true);
        when(accountRepository.existsByAccountNumber(request.getDestinationAccount())).thenReturn(false);

        // act
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> transferService.schedule(request));

        // assert
        assertEquals("Conta não encontrada para o número da conta: " + request.getDestinationAccount(), exception.getMessage());
        verify(accountRepository).existsByAccountNumber(request.getSourceAccount());
        verify(accountRepository).existsByAccountNumber(request.getDestinationAccount());
        verifyNoInteractions(transferFeeCalculator, transferMapper, transferRepository);
    }

    @Test
    void findAllShouldReturnMappedTransfers() {
        // arrange
        List<Transfer> transfers = List.of(savedTransfer, savedTransfer);
        List<TransferResponse> expected = List.of(response, response);
        when(transferRepository.findAll()).thenReturn(transfers);
        when(transferMapper.toResponseList(transfers)).thenReturn(expected);

        // act
        List<TransferResponse> result = transferService.findAll();

        // assert
        assertEquals(expected, result);
        verify(transferRepository).findAll();
        verify(transferMapper).toResponseList(transfers);
    }

    @Test
    void findByIdShouldReturnMappedTransfer() {
        // arrange
        when(transferRepository.findById(10L)).thenReturn(Optional.of(savedTransfer));
        when(transferMapper.toResponse(savedTransfer)).thenReturn(response);

        // act
        TransferResponse result = transferService.findById(10L);

        // assert
        assertSame(response, result);
        verify(transferRepository).findById(10L);
        verify(transferMapper).toResponse(savedTransfer);
    }

    @Test
    void findByIdShouldFailWhenTransferDoesNotExist() {
        // arrange
        when(transferRepository.findById(10L)).thenReturn(Optional.empty());

        // act
        TransferNotFoundException exception = assertThrows(TransferNotFoundException.class, () -> transferService.findById(10L));

        // assert
        assertEquals("Transferência não encontrada para o id: 10", exception.getMessage());
        verify(transferRepository).findById(10L);
        verifyNoInteractions(transferMapper);
    }

    @Test
    void cancelShouldChangeStatusToCancelledWhenTransferIsScheduledAndFuture() {
        // arrange
        Transfer scheduledTransfer = Transfer.builder()
                .id(15L)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .transferDate(LocalDateTime.now().plusDays(1))
                .status(TransferStatus.SCHEDULED)
                .build();

        TransferResponse cancelledResponse = TransferResponse.builder()
                .id(15L)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .transferDate(scheduledTransfer.getTransferDate())
                .status(TransferStatus.CANCELLED)
                .build();

        when(transferRepository.findById(15L)).thenReturn(Optional.of(scheduledTransfer));
        when(transferRepository.save(scheduledTransfer)).thenReturn(scheduledTransfer);
        when(transferMapper.toResponse(scheduledTransfer)).thenReturn(cancelledResponse);

        // act
        TransferResponse result = transferService.cancel(15L);

        // assert
        assertEquals(TransferStatus.CANCELLED, result.getStatus());
        assertEquals(TransferStatus.CANCELLED, scheduledTransfer.getStatus());
        verify(transferRepository).findById(15L);
        verify(transferRepository).save(scheduledTransfer);
        verify(transferMapper).toResponse(scheduledTransfer);
    }

    @Test
    void cancelShouldFailWhenTransferStatusIsNotScheduled() {
        // arrange
        Transfer completedTransfer = Transfer.builder()
                .id(15L)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .transferDate(LocalDateTime.now().plusDays(1))
                .status(TransferStatus.COMPLETED)
                .build();
        when(transferRepository.findById(15L)).thenReturn(Optional.of(completedTransfer));

        // act
        InvalidTransferException exception = assertThrows(InvalidTransferException.class, () -> transferService.cancel(15L));

        // assert
        assertEquals("Somente transferências com status SCHEDULED podem ser canceladas, status atual é COMPLETED", exception.getMessage());
        verify(transferRepository).findById(15L);
        verify(transferRepository, never()).save(any());
    }

    @Test
    void cancelShouldFailWhenTransferDateIsAlreadyReached() {
        // arrange
        Transfer scheduledTransfer = Transfer.builder()
                .id(15L)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .transferDate(LocalDateTime.now().minusMinutes(1))
                .status(TransferStatus.SCHEDULED)
                .build();
        when(transferRepository.findById(15L)).thenReturn(Optional.of(scheduledTransfer));

        // act
        InvalidTransferException exception = assertThrows(InvalidTransferException.class, () -> transferService.cancel(15L));

        // assert
        assertEquals("A transferência não pode mais ser cancelada porque a data da transferência já chegou", exception.getMessage());
        verify(transferRepository).findById(15L);
        verify(transferRepository, never()).save(any());
    }

    @Test
    void cancelShouldFailWhenTransferDoesNotExist() {
        // arrange
        when(transferRepository.findById(20L)).thenReturn(Optional.empty());

        // act
        TransferNotFoundException exception = assertThrows(TransferNotFoundException.class, () -> transferService.cancel(20L));

        // assert
        assertEquals("Transferência não encontrada para o id: 20", exception.getMessage());
        verify(transferRepository).findById(20L);
        verify(transferRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }
}