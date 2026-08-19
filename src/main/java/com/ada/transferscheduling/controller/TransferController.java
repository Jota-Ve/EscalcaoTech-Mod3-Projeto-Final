package com.ada.transferscheduling.controller;

import com.ada.transferscheduling.dto.request.ScheduleTransferRequest;
import com.ada.transferscheduling.dto.response.TransferResponse;
import com.ada.transferscheduling.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Scheduling and querying transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Schedules a new transfer", description = "Use case 1: schedule a transfer, calculating the fee based on the number of days in advance")
    public ResponseEntity<TransferResponse> schedule(@Valid @RequestBody ScheduleTransferRequest request) {
        TransferResponse response = transferService.schedule(request);
        return ResponseEntity.created(URI.create("/api/transfers/" + response.getId())).body(response);
    }

    @GetMapping
    @Operation(summary = "Lists all scheduled transfers", description = "Use case 2: list transfers")
    public ResponseEntity<List<TransferResponse>> findAll() {
        return ResponseEntity.ok(transferService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds a transfer by id", description = "Use case 3: query a transfer by id")
    public ResponseEntity<TransferResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.findById(id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancels a scheduled transfer",
            description = "Use case 4: cancel a transfer. Only transfers with status SCHEDULED can be cancelled, and only before the transfer date")
    public ResponseEntity<TransferResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.cancel(id));
    }
}
