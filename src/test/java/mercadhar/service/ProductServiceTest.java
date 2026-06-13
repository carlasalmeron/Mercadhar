package mercadhar.service;

import mercadhar.dto.product.ProductRequest;
import mercadhar.dto.product.ProductResponse;
import mercadhar.exception.ResourceNotFoundException;
import mercadhar.model.Category;
import mercadhar.model.Product;
import mercadhar.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private Category mockCategory;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName("Bebidas");
        mockCategory.setDescription("Bebidas venezolanas");

        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Malta");
        mockProduct.setDescription("Malta venezolana");
        mockProduct.setPrice(new BigDecimal("2.50"));
        mockProduct.setAvailable(true);
        mockProduct.setCategory(mockCategory);

        productRequest = new ProductRequest();
        productRequest.setName("Malta");
        productRequest.setDescription("Malta venezolana");
        productRequest.setPrice(new BigDecimal("2.50"));
        productRequest.setAvailable(true);
        productRequest.setCategoryId(1L);
    }

    @Test
    @DisplayName("findAll - returns all products")
    void findAll_ShouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(mockProduct));

        List<ProductResponse> result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals("Malta", result.get(0).getName());
    }

    @Test
    @DisplayName("findAvailable - returns only available products")
    void findAvailable_ShouldReturnOnlyAvailable() {
        when(productRepository.findByAvailableTrue())
                .thenReturn(List.of(mockProduct));

        List<ProductResponse> result = productService.findAvailable();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    @DisplayName("findById - existing product returns response")
    void findById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(mockProduct));

        ProductResponse result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Malta", result.getName());
        assertEquals(new BigDecimal("2.50"), result.getPrice());
    }

    @Test
    @DisplayName("findById - not found throws exception")
    void findById_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.findById(99L));
    }

    @Test
    @DisplayName("create - saves and returns product")
    void create_ShouldSaveAndReturnProduct() {
        when(categoryService.findById(1L)).thenReturn(mockCategory);
        when(productRepository.save(any(Product.class)))
                .thenReturn(mockProduct);

        ProductResponse result = productService.create(productRequest);

        assertNotNull(result);
        assertEquals("Malta", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("toggleAvailability - flips availability")
    void toggleAvailability_ShouldFlipAvailability() {
        mockProduct.setAvailable(true);
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class)))
                .thenReturn(mockProduct);

        productService.toggleAvailability(1L);

        assertFalse(mockProduct.isAvailable());
        verify(productRepository).save(mockProduct);
    }

    @Test
    @DisplayName("delete - removes product")
    void delete_ShouldRemoveProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(mockProduct));

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }
}
