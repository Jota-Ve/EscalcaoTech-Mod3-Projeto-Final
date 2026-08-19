package com.ada.transferscheduling.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data required to schedule a transfer")
public class ScheduleTransferRequest {

    @NotBlank(message = "source account is required")
    @Schema(description = "Source account", example = "00001-1")
    private String sourceAccount;

    @NotBlank(message = "destination account is required")
    @Schema(description = "Destination account", example = "00002-2")
    private String destinationAccount;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    @Schema(description = "Transfer amount", example = "1000.00")
    private BigDecimal amount;

    @NotNull(message = "transfer date is required")
    @FutureOrPresent(message = "transfer date cannot be in the past")
    @Schema(description = "Date and time the transfer should take place", example = "2026-09-01T10:00:00")
    private LocalDateTime transferDate;
}
