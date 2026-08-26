package com.ada.transferscheduling.service;

import com.ada.transferscheduling.exception.InvalidTransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferFeeCalculatorTests {

    private TransferFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TransferFeeCalculator();
    }

    @Test
    void shouldCalculateSameDayFee() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 8, 19, 15, 0);

        BigDecimal result = calculator.calculate(amount, scheduledDate, transferDate);

        assertEquals(new BigDecimal("25.00"), result);
    }

    @Test
    void shouldCalculateFlatFeeForOneToTenDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 8, 28, 15, 0);

        BigDecimal result = calculator.calculate(amount, scheduledDate, transferDate);

        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void shouldCalculateFeeForElevenToTwentyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 8, 30, 15, 0);

        BigDecimal result = calculator.calculate(amount, scheduledDate, transferDate);

        assertEquals(new BigDecimal("82.00"), result);
    }

    @Test
    void shouldCalculateFeeForTwentyOneToThirtyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 9, 10, 15, 0);

        BigDecimal result = calculator.calculate(amount, scheduledDate, transferDate);

        assertEquals(new BigDecimal("69.00"), result);
    }

    @Test
    void shouldCalculateFeeForThirtyOneToFortyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 9, 20, 15, 0);

        BigDecimal result = calculator.calculate(amount, scheduledDate, transferDate);

        assertEquals(new BigDecimal("47.00"), result);
    }

    @Test
    void shouldCalculateFeeForFortyOneToFiftyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 10, 1, 15, 0);

        BigDecimal result = calculator.calculate(amount, scheduledDate, transferDate);

        assertEquals(new BigDecimal("17.00"), result);
    }

    @Test
    void shouldThrowExceptionWhenTransferIsScheduledMoreThanFiftyDaysInAdvance() {
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDateTime scheduledDate = LocalDateTime.of(2026, 8, 19, 9, 0);
        LocalDateTime transferDate = LocalDateTime.of(2026, 10, 10, 15, 0);

        InvalidTransferException exception = assertThrows(InvalidTransferException.class,
                () -> calculator.calculate(amount, scheduledDate, transferDate));

        assertEquals("Cannot schedule a transfer more than 50 days in advance", exception.getMessage());
    }
}
