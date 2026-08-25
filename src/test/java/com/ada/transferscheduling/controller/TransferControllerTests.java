package com.ada.transferscheduling.controller;

import com.ada.transferscheduling.dto.response.TransferResponse;
import com.ada.transferscheduling.entity.TransferStatus;
import com.ada.transferscheduling.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTests {

    private static final String TRANSFERS_URL = "/api/transfers";
    private static final String TRANSFER_ID_1_URL = "/api/transfers/1";
    private static final String TRANSFER_CANCEL_ID_1_URL = "/api/transfers/1/cancel";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    void scheduleShouldReturnCreated() throws Exception {
        TransferResponse response = buildResponse(1L);

        when(transferService.schedule(any())).thenReturn(response);

        mockMvc.perform(post(TRANSFERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(validScheduleRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/transfers/1"))
                .andExpect(jsonPath("$.id").value(1L));

        verify(transferService).schedule(any());
    }

    @Test
    void findAllShouldReturnOk() throws Exception {
        when(transferService.findAll()).thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get(TRANSFERS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(transferService).findAll();
    }

    @Test
    void findByIdShouldReturnOk() throws Exception {
        when(transferService.findById(1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(get(TRANSFER_ID_1_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(transferService).findById(1L);
    }

    @Test
    void cancelShouldReturnOk() throws Exception {
        when(transferService.cancel(1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(patch(TRANSFER_CANCEL_ID_1_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(transferService).cancel(1L);
    }

    private String validScheduleRequestJson() {
        return """
                {
                  "sourceAccount": "00001-1",
                  "destinationAccount": "00002-2",
                  "amount": 1000.00,
                  "transferDate": "2030-01-01T10:00:00"
                }
                """;
    }

    private TransferResponse buildResponse(Long id) {
        return TransferResponse.builder()
                .id(id)
                .sourceAccount("00001-1")
                .destinationAccount("00002-2")
                .amount(new BigDecimal("1000.00"))
                .fee(new BigDecimal("10.00"))
                .scheduledDate(LocalDateTime.of(2030, 1, 1, 9, 0))
                .transferDate(LocalDateTime.of(2030, 1, 1, 10, 0))
                .status(TransferStatus.SCHEDULED)
                .createdAt(LocalDateTime.of(2030, 1, 1, 8, 0))
                .build();
    }
}
