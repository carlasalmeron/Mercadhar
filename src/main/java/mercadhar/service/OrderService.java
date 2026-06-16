package mercadhar.service;

import mercadhar.dto.order.OrderItemRequest;
import mercadhar.dto.order.OrderItemResponse;
import mercadhar.dto.order.OrderRequest;
import mercadhar.dto.order.OrderResponse;
import mercadhar.exception.ResourceNotFoundException;
import mercadhar.exception.UnauthorizedException;
import mercadhar.model.*;
import mercadhar.model.enums.OrderStatus;
import mercadhar.model.enums.OrderType;
import mercadhar.model.enums.Role;
import mercadhar.repository.OrderRepository;
import mercadhar.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final TimeSlotService timeSlotService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductService productService,
                        TimeSlotService timeSlotService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.timeSlotService = timeSlotService;
    }

    public OrderResponse create(OrderRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userEmail));

        TimeSlot timeSlot = timeSlotService.findById(request.getTimeSlotId());

        if (!timeSlot.isAvailable()) {
            throw new IllegalArgumentException(
                    "This time slot is not available");
        }

        if (request.getOrderType() == OrderType.DELIVERY &&
                (request.getDeliveryAddress() == null ||
                        request.getDeliveryAddress().isBlank())) {
            throw new IllegalArgumentException(
                    "Delivery address is required for delivery orders");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTimeSlot(timeSlot);
        order.setOrderType(request.getOrderType());
        order.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : "Recogida en tienda");
        order.setNotes(request.getNotes());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService
                    .findProductById(itemRequest.getProductId());

            if (!product.isAvailable()) {
                throw new IllegalArgumentException(
                        "Product not available: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());
            items.add(item);

            total = total.add(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setItems(items);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        timeSlotService.incrementOrders(timeSlot.getId());

        return toResponse(saved);
    }

    public List<OrderResponse> findMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userEmail));
        return orderRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    public OrderResponse updateStatus(Long orderId,
                                      OrderStatus status,
                                      String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userEmail));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (user.getRole() == Role.ROLE_USER) {
            if (!order.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException(
                        "You can only manage your own orders");
            }
            if (status != OrderStatus.CANCELLED) {
                throw new UnauthorizedException(
                        "Users can only cancel orders");
            }
        }

        if (status == OrderStatus.CANCELLED &&
                order.getStatus() != OrderStatus.CANCELLED) {
            timeSlotService.decrementOrders(
                    order.getTimeSlot().getId());
        }

        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getUser().getName())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .timeSlotDate(order.getTimeSlot().getDate().toString())
                .timeSlotStart(order.getTimeSlot().getStartTime())
                .timeSlotEnd(order.getTimeSlot().getEndTime())
                .items(itemResponses)
                .build();
    }
}
