package com.ada.transferscheduling.service;

import com.ada.transferscheduling.exception.InvalidTransferException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TransferFeeCalculator {

    private static final BigDecimal SAME_DAY_FEE = new BigDecimal("0.025");
    private static final BigDecimal FLAT_FEE_UP_TO_10_DAYS = new BigDecimal("10.00");
    private static final BigDecimal FEE_11_TO_20_DAYS = new BigDecimal("0.082");
    private static final BigDecimal FEE_21_TO_30_DAYS = new BigDecimal("0.069");
    private static final BigDecimal FEE_31_TO_40_DAYS = new BigDecimal("0.047");
    private static final BigDecimal FEE_41_TO_50_DAYS = new BigDecimal("0.017");

    public BigDecimal calculate(BigDecimal amount, LocalDateTime scheduledDate, LocalDateTime transferDate) {
        long days = ChronoUnit.DAYS.between(scheduledDate.toLocalDate(), transferDate.toLocalDate());

        if (days == 0) {
            return amount.multiply(SAME_DAY_FEE).setScale(2, RoundingMode.HALF_UP);
        }
        if (days >= 1 && days <= 10) {
            return FLAT_FEE_UP_TO_10_DAYS.setScale(2, RoundingMode.HALF_UP);
        }
        if (days >= 11 && days <= 20) {
            return amount.multiply(FEE_11_TO_20_DAYS).setScale(2, RoundingMode.HALF_UP);
        }
        if (days >= 21 && days <= 30) {
            return amount.multiply(FEE_21_TO_30_DAYS).setScale(2, RoundingMode.HALF_UP);
        }
        if (days >= 31 && days <= 40) {
            return amount.multiply(FEE_31_TO_40_DAYS).setScale(2, RoundingMode.HALF_UP);
        }
        if (days >= 41 && days <= 50) {
            return amount.multiply(FEE_41_TO_50_DAYS).setScale(2, RoundingMode.HALF_UP);
        }

        throw new InvalidTransferException(
                "Cannot schedule a transfer more than 50 days in advance");
    }
}
