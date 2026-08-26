package com.ada.transferscheduling.validation;

import com.ada.transferscheduling.exception.AccountNotFoundException;
import com.ada.transferscheduling.exception.InvalidTransferException;
import com.ada.transferscheduling.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DailyTransactionLimitValidator {

	private final AccountRepository accountRepository;
	private final BigDecimal dailyLimit;

	public DailyTransactionLimitValidator(AccountRepository accountRepository,
			@Value("${transfer.daily-limit:1000.00}") BigDecimal dailyLimit) {
		this.accountRepository = accountRepository;
		this.dailyLimit = dailyLimit;
	}

	public void validate(String accountNumber, List<BigDecimal> transactionValues, LocalDate transactionDate) {
		if (!accountRepository.existsByAccountNumber(accountNumber)) {
			throw new AccountNotFoundException(accountNumber);
		}

		BigDecimal totalForDay = BigDecimal.ZERO;
		for (BigDecimal value : transactionValues) {
			if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
				throw new InvalidTransferException("Transaction value must be greater than zero");
			}
			totalForDay = totalForDay.add(value);
		}

		if (totalForDay.compareTo(dailyLimit) > 0) {
			throw new InvalidTransferException("Daily transaction limit exceeded");
		}
	}
}
