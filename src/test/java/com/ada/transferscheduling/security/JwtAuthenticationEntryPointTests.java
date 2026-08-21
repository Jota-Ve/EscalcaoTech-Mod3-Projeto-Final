package com.ada.transferscheduling.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtAuthenticationEntryPointTests {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();
    private HttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authenticationException;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = new MockHttpServletResponse();
        authenticationException = mock(AuthenticationException.class);
        when(request.getRequestURI()).thenReturn("/api/transfers");
    }

    @Test
    void shouldReturnUnauthorizedResponse() throws IOException {
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldReturnJsonResponse() throws IOException {
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());

        assertEquals("application/json", response.getContentType());
        assertTrue(body.isObject());
        assertEquals(401, body.get("status").asInt());
    }

    @Test
    void shouldReturnCorrectErrorMessage() throws IOException {
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());

        assertEquals("Missing or invalid credentials", body.get("message").asText());
        assertEquals("Unauthorized", body.get("error").asText());
        assertEquals("/api/transfers", body.get("path").asText());
    }
}
