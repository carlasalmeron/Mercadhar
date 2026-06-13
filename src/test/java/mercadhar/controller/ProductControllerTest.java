package mercadhar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mercadhar.model.Category;
import mercadhar.model.Product;
import mercadhar.model.User;
import mercadhar.model.enums.Role;
import mercadhar.repository.CategoryRepository;
import mercadhar.repository.ProductRepository;
import mercadhar.repository.UserRepository;
import mercadhar.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Category category;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Admin
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setPhone("000000000");
        admin.setAddress("Admin HQ");
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        // User
        User user = new User();
        user.setName("User");
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setPhone("111111111");
        user.setAddress("User Address");
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        // Tokens
        UserDetails adminDetails = new org.springframework.security.core.userdetails.User(
                admin.getEmail(), admin.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        adminToken = "Bearer " + jwtService.generateToken(adminDetails);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        userToken = "Bearer " + jwtService.generateToken(userDetails);

        // Category
        category = new Category();
        category.setName("Bebidas");
        category.setDescription("Bebidas venezolanas");
        category = categoryRepository.save(category);

        // Product
        Product product = new Product();
        product.setName("Malta");
        product.setDescription("Malta venezolana");
        product.setPrice(new BigDecimal("2.50"));
        product.setAvailable(true);
        product.setCategory(category);
        productRepository.save(product);
    }

    @Test
    @DisplayName("GET /api/products - public access returns 200")
    void getAll_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Malta"));
    }

    @Test
    @DisplayName("GET /api/products/available - returns only available")
    void getAvailable_ShouldReturnOnlyAvailable() throws Exception {
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    @DisplayName("POST /api/products - admin creates product returns 201")
    void create_AsAdmin_ShouldReturn201() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "Harina PAN",
                "description", "Harina de maíz",
                "price", 3.50,
                "available", true,
                "categoryId", category.getId()
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Harina PAN"));
    }

    @Test
    @DisplayName("POST /api/products - user forbidden returns 403")
    void create_AsUser_ShouldReturn403() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "Harina PAN",
                "price", 3.50,
                "available", true,
                "categoryId", category.getId()
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/products - no auth returns 403")
    void create_NoAuth_ShouldReturn403() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "Harina PAN",
                "price", 3.50,
                "available", true,
                "categoryId", category.getId()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
