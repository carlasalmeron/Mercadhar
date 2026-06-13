package mercadhar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mercadhar.dto.auth.LoginRequest;
import mercadhar.dto.auth.RegisterRequest;
import mercadhar.model.User;
import mercadhar.model.enums.Role;
import mercadhar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - success returns 201")
    void register_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Carla Test");
        request.setEmail("carla@test.com");
        request.setPassword("password123");
        request.setPhone("123456789");
        request.setAddress("Calle Test 1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("carla@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - duplicate email returns 400")
    void register_DuplicateEmail_ShouldReturn400() throws Exception {
        User existing = new User();
        existing.setName("Existing");
        existing.setEmail("carla@test.com");
        existing.setPassword(passwordEncoder.encode("password"));
        existing.setPhone("123456789");
        existing.setAddress("Calle Test 1");
        existing.setRole(Role.ROLE_USER);
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest();
        request.setName("Carla Test");
        request.setEmail("carla@test.com");
        request.setPassword("password123");
        request.setPhone("123456789");
        request.setAddress("Calle Test 1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - success returns token")
    void login_ShouldReturnToken() throws Exception {
        User user = new User();
        user.setName("Carla Test");
        user.setEmail("carla@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setPhone("123456789");
        user.setAddress("Calle Test 1");
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("carla@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("carla@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - wrong password returns 401")
    void login_WrongPassword_ShouldReturn401() throws Exception {
        User user = new User();
        user.setName("Carla Test");
        user.setEmail("carla@test.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));
        user.setPhone("123456789");
        user.setAddress("Calle Test 1");
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("carla@test.com");
        request.setPassword("wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register - missing fields returns 400")
    void register_MissingFields_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("carla@test.com");
        // Falta name, password, phone, address

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
