package com.ada.transferscheduling.mapper;

import com.ada.transferscheduling.dto.request.ScheduleTransferRequest;
import com.ada.transferscheduling.dto.response.TransferResponse;
import com.ada.transferscheduling.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fee", ignore = true)
    @Mapping(target = "scheduledDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transfer toEntity(ScheduleTransferRequest request);

    TransferResponse toResponse(Transfer entity);

    List<TransferResponse> toResponseList(List<Transfer> entities);
}
