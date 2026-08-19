package com.ada.transferscheduling.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*
;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTests {
    private static final String SECRET_KEY = "VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIHRlc3RpbmcgcHVycG9zZXMu";
    private static final long EXPIRATION_MS = 3600000L;

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Authentication authenticationMock;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, EXPIRATION_MS);
    }

    @Test
    void shouldReturnExpirationWithGivenConfiguration() {
        assertEquals(EXPIRATION_MS, jwtTokenProvider.getExpirationMs());
    }

    @Test
     void shouldReturnTrueForValidToken()
    {
        when(authenticationMock.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john.doe");

        String token = jwtTokenProvider.generateToken(authenticationMock);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.isTokenValid(token));
        assertEquals("john.doe", jwtTokenProvider.getUsername(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.isTokenValid("invalid.token.value"));
    }
}
