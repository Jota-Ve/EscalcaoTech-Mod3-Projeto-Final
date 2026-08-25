package com.ada.transferscheduling;

import com.ada.transferscheduling.exception.AccountNotFoundException;
import com.ada.transferscheduling.exception.InvalidTransferException;
import com.ada.transferscheduling.repository.AccountRepository;
import com.ada.transferscheduling.validation.DailyTransactionLimitValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DailyTransactionLimitValidatorIntegrationTest {

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("1000.00");
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 8, 19);

    @Autowired
    private AccountRepository accountRepository;

    private DailyTransactionLimitValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DailyTransactionLimitValidator(accountRepository, DAILY_LIMIT);
    }

    @Test
    void shouldValidateTransactionUsingAccountFromDatabase() {
        assertDoesNotThrow(() -> validator.validate(
                "00001-1",
                List.of(new BigDecimal("400.00"), new BigDecimal("300.00")),
                TRANSACTION_DATE));
    }

    @Test
    void shouldRejectTransactionForAccountNotFoundInDatabase() {
        assertThrows(AccountNotFoundException.class, () -> validator.validate(
                "account-inexistente",
                List.of(new BigDecimal("100.00")),
                TRANSACTION_DATE));
    }

    @Test
    void shouldRejectTransactionAboveDailyLimit() {
        assertThrows(InvalidTransferException.class, () -> validator.validate(
                "00001-1",
                List.of(new BigDecimal("600.00"), new BigDecimal("500.00")),
                TRANSACTION_DATE));
    }
}