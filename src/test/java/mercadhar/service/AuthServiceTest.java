package mercadhar.service;

import mercadhar.dto.auth.AuthResponse;
import mercadhar.dto.auth.LoginRequest;
import mercadhar.dto.auth.RegisterRequest;
import mercadhar.model.User;
import mercadhar.model.enums.Role;
import mercadhar.repository.UserRepository;
import mercadhar.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Test User");
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setPhone("123456789");
        mockUser.setAddress("Test Address");
        mockUser.setRole(Role.ROLE_USER);

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("123456789");
        registerRequest.setAddress("Test Address");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Register - success returns token")
    void register_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("test@test.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - duplicate email throws exception")
    void register_DuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login - success returns token")
    void login_ShouldReturnAuthResponse() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(mockUser));
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("Test User", response.getName());
    }

    @Test
    @DisplayName("Login - user not found throws exception")
    void login_UserNotFound_ShouldThrowException() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> authService.login(loginRequest));
    }
}
