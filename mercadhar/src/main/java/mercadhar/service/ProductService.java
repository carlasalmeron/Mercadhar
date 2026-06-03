package mercadhar.service;

import mercadhar.dto.product.ProductRequest;
import mercadhar.dto.product.ProductResponse;
import mercadhar.exception.ResourceNotFoundException;
import mercadhar.model.Category;
import mercadhar.model.Product;
import mercadhar.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> findAvailable() {
        return productRepository.findByAvailableTrue()
                .stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream().map(this::toResponse).toList();
    }

    public ProductResponse findById(Long id) {
        return toResponse(findProductById(id));
    }

    public ProductResponse create(ProductRequest request) {
        Category category = categoryService.findById(request.getCategoryId());
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setAvailable(request.isAvailable());
        product.setCategory(category);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProductById(id);
        Category category = categoryService.findById(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setAvailable(request.isAvailable());
        product.setCategory(category);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse toggleAvailability(Long id) {
        Product product = findProductById(id);
        product.setAvailable(!product.isAvailable());
        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        findProductById(id);
        productRepository.deleteById(id);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .available(product.isAvailable())
                .categoryName(product.getCategory().getName())
                .categoryId(product.getCategory().getId())
                .build();
    }
}
