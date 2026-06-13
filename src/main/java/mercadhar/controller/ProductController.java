package mercadhar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mercadhar.dto.product.ProductRequest;
import mercadhar.dto.product.ProductResponse;
import mercadhar.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/available")
    @Operation(summary = "Get only available products")
    public ResponseEntity<List<ProductResponse>> getAvailable() {
        return ResponseEntity.ok(productService.findAvailable());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<List<ProductResponse>> getByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.findByCategory(categoryId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Create a new product (Admin only)")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Update a product (Admin only)")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Toggle product availability (Admin only)")
    public ResponseEntity<ProductResponse> toggleAvailability(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleAvailability(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Delete a product (Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}