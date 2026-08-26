package com.ada.transferscheduling.validation;

import com.ada.transferscheduling.exception.AccountNotFoundException;
import com.ada.transferscheduling.exception.InvalidTransferException;
import com.ada.transferscheduling.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DailyTransactionLimitValidatorTests {

	private static final String ACCOUNT_NUMBER = "1L";
	private static final BigDecimal DAILY_LIMIT = new BigDecimal("1000.00");
	private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 8, 19);

	private AccountRepository accountRepository;
	private DailyTransactionLimitValidator validator;

	@BeforeEach
	void setUp() {
		accountRepository = mock(AccountRepository.class);
		validator = new DailyTransactionLimitValidator(accountRepository, DAILY_LIMIT);
	}

	@Test
	void shouldAllowTransactionWithinDailyLimit() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertDoesNotThrow(() -> validator.validate(ACCOUNT_NUMBER,
				List.of(new BigDecimal("400.00"), new BigDecimal("300.00")),
				TRANSACTION_DATE));

		verify(accountRepository).existsByAccountNumber(ACCOUNT_NUMBER);
	}

	@Test
	void shouldBlockTransactionWhenExceedingDailyLimit() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertThrows(InvalidTransferException.class, () -> validator.validate(ACCOUNT_NUMBER,
				List.of(new BigDecimal("500.00"), new BigDecimal("600.00")),
				TRANSACTION_DATE));
	}

	@Test
	void shouldAllowTransactionExactlyAtTheLimit() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertDoesNotThrow(() -> validator.validate(ACCOUNT_NUMBER,
				List.of(new BigDecimal("500.00"), new BigDecimal("500.00")),
				TRANSACTION_DATE));
	}

	@Test
	void shouldThrowExceptionWhenAccountNotFound() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(false);

		assertThrows(AccountNotFoundException.class, () -> validator.validate(ACCOUNT_NUMBER,
				List.of(new BigDecimal("100.00"), new BigDecimal("200.00")),
				TRANSACTION_DATE));
	}

	@Test
	void shouldThrowExceptionWhenTransactionValueIsInvalid() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertThrows(InvalidTransferException.class, () -> validator.validate(ACCOUNT_NUMBER,
				List.of(BigDecimal.ZERO, new BigDecimal("100.00")),
				TRANSACTION_DATE));
	}

	@Test
	void shouldThrowExceptionWhenTransactionValueIsNegative() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertThrows(InvalidTransferException.class, () -> validator.validate(ACCOUNT_NUMBER,
				List.of(new BigDecimal("-100.00")),
				TRANSACTION_DATE));
	}
	
	@Test
	void shouldThrowExceptionWhenTransactionValueIsNull() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertThrows(InvalidTransferException.class, () -> validator.validate(ACCOUNT_NUMBER,
				Arrays.asList((BigDecimal) null),
				TRANSACTION_DATE));
	}

	@Test
	void shouldValidateTransactionsForTheRequestedDay() {
		when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(true);

		assertDoesNotThrow(() -> validator.validate(ACCOUNT_NUMBER,
				List.of(new BigDecimal("500.00"), new BigDecimal("400.00")),
				TRANSACTION_DATE.plusDays(1)));
	}
}
