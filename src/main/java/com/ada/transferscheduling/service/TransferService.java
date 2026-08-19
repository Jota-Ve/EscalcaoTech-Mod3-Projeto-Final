package com.ada.transferscheduling.service;

import com.ada.transferscheduling.dto.request.ScheduleTransferRequest;
import com.ada.transferscheduling.dto.response.TransferResponse;

import java.util.List;

public interface TransferService {

    TransferResponse schedule(ScheduleTransferRequest request);

    List<TransferResponse> findAll();

    TransferResponse findById(Long id);

    TransferResponse cancel(Long id);
}
