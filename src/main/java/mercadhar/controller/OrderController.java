package mercadhar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mercadhar.dto.order.OrderRequest;
import mercadhar.dto.order.OrderResponse;
import mercadhar.model.enums.OrderStatus;
import mercadhar.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "Bearer Auth")
@Tag(name = "Orders", description = "Order management for customers and admins")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //User Endpoints
    @PostMapping
    @Operation(summary = "Create a new order (authenticated users)")
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request, userDetails.getUsername()));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.findMyOrders(userDetails.getUsername()));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel my order")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                orderService.updateStatus(
                        id,
                        OrderStatus.CANCELLED,
                        userDetails.getUsername()));
    }

    //Admin Endpoints
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all orders (Admin only)")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update order status (Admin only)")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(
                orderService.updateStatus(
                        id,
                        status,
                        userDetails.getUsername()));
    }
}