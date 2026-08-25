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
import com.ada.transferscheduling.service.DailyTransactionLimitValidator;
import com.ada.transferscheduling.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferMapper transferMapper;
    private final TransferFeeCalculator transferFeeCalculator;
    private final DailyTransactionLimitValidator dailyTransactionLimitValidator;

    @Override
    @Transactional
    public TransferResponse schedule(ScheduleTransferRequest request) {
        validateAccountsExist(request.getSourceAccount(), request.getDestinationAccount());
        dailyTransactionLimitValidator.validate(
                request.getSourceAccount(),
                List.of(request.getAmount()),
                request.getTransferDate().toLocalDate());

        LocalDateTime scheduledDate = LocalDateTime.now();
        BigDecimal fee = transferFeeCalculator.calculate(request.getAmount(), scheduledDate, request.getTransferDate());

        Transfer transfer = transferMapper.toEntity(request);
        transfer.setFee(fee);
        transfer.setScheduledDate(scheduledDate);
        transfer.setCreatedAt(LocalDateTime.now());
        transfer.setStatus(scheduledDate.toLocalDate().isEqual(request.getTransferDate().toLocalDate())
                ? TransferStatus.COMPLETED
                : TransferStatus.SCHEDULED);

        Transfer saved = transferRepository.save(transfer);
        return transferMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> findAll() {
        List<Transfer> transfers = transferRepository.findAll();
        return transferMapper.toResponseList(transfers);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse findById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));
        return transferMapper.toResponse(transfer);
    }

    @Override
    @Transactional
    public TransferResponse cancel(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));

        if (transfer.getStatus() != TransferStatus.SCHEDULED) {
            throw new InvalidTransferException(
                    "Somente transferências com status SCHEDULED podem ser canceladas, status atual é "
                            + transfer.getStatus());
        }

        if (!LocalDateTime.now().isBefore(transfer.getTransferDate())) {
            throw new InvalidTransferException(
                    "A transferência não pode mais ser cancelada porque a data da transferência já chegou");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        Transfer saved = transferRepository.save(transfer);
        return transferMapper.toResponse(saved);
    }

    private void validateAccountsExist(String sourceAccount, String destinationAccount) {
        if (!accountRepository.existsByAccountNumber(sourceAccount)) {
            throw new AccountNotFoundException(sourceAccount);
        }
        if (!accountRepository.existsByAccountNumber(destinationAccount)) {
            throw new AccountNotFoundException(destinationAccount);
        }
    }
}
