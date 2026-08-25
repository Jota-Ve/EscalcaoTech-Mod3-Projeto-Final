package com.ada.transferscheduling.security;

import com.ada.transferscheduling.entity.User;
import com.ada.transferscheduling.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomUserDetailsService customUserDetailsService = new CustomUserDetailsService(userRepository);

    private final User user = User.builder()
            .username("usuario.teste")
            .password("senha-codificada")
            .enabled(true)
            .roles(List.of("ROLE_USER"))
            .build();

    @Test
    void shouldFindUser() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userRepository.findByUsername(user.getUsername());

        assertTrue(foundUser.isPresent());
        verify(userRepository).findByUsername(user.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotFound() {
        String username = "usuario.inexistente";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username));

        assertEquals("User not found: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldReturnUserDetails() {

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());

        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(user.getRoles().get(0))));
        verify(userRepository).findByUsername(user.getUsername());
    }   
}
