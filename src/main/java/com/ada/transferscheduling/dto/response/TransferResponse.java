package com.ada.transferscheduling.dto.response;

import com.ada.transferscheduling.entity.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Data of a scheduled transfer")
public class TransferResponse {

    private Long id;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private BigDecimal fee;
    private LocalDateTime scheduledDate;
    private LocalDateTime transferDate;
    private TransferStatus status;
    private LocalDateTime createdAt;
}
