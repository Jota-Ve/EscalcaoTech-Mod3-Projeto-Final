package com.ada.transferscheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferAuthenticationLimitIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldAuthenticateAndRejectTransferAboveDailyLimit() throws Exception {
                String token = loginAndGetToken();
                String futureTransferDate = LocalDateTime.now().plusDays(1).withNano(0).toString();

                mockMvc.perform(post("/api/transfers")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "sourceAccount": "00001-1",
                                                  "destinationAccount": "00002-2",
                                                  "amount": 1100.00,
                                                  "transferDate": "%s"
                                                }
                                                """.formatted(futureTransferDate)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Daily transaction limit exceeded"));
        }

        @Test
        void shouldRejectTransferWithoutAuthentication() throws Exception {
                mockMvc.perform(post("/api/transfers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "sourceAccount": "00001-1",
                                                  "destinationAccount": "00002-2",
                                                  "amount": 100.00,
                                                  "transferDate": "2026-08-25T10:00:00"
                                                }
                                                """))
                                .andExpect(status().isUnauthorized());
        }

        private String loginAndGetToken() throws Exception {
                String response = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username": "user",
                                                  "password": "user123"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                JsonNode body = objectMapper.readTree(response);
                return body.get("token").asText();
        }
}