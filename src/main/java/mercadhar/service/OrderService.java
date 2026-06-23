package mercadhar.service;

import jakarta.transaction.Transactional;
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

    @Transactional
    public OrderResponse create(OrderRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró al usuario: " + userEmail));

        TimeSlot timeSlot = timeSlotService.findById(request.getTimeSlotId());

        if (!timeSlot.isAvailable()) {
            throw new IllegalArgumentException(
                    "Esta franja horaria no está disponible");
        }

        if (request.getOrderType() == OrderType.DELIVERY &&
                (request.getDeliveryAddress() == null ||
                        request.getDeliveryAddress().isBlank())) {
            throw new IllegalArgumentException(
                    "Se requiere una dirección de entrega para los pedidos con entrega a domicilio");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTimeSlot(timeSlot);
        order.setOrderType(request.getOrderType());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setDeliveryAddress(
                request.getDeliveryAddress() != null && !request.getDeliveryAddress().isBlank()
                        ? request.getDeliveryAddress()
                        : "Recogida en tienda");
        order.setNotes(request.getNotes());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.findProductById(itemRequest.getProductId());

            if (!product.isAvailable()) {
                throw new IllegalArgumentException(
                        "El producto no está disponible: " + product.getName());
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

    @Transactional
    public List<OrderResponse> findMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró al usuario: " + userEmail));
        return orderRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId,
                                      OrderStatus status,
                                      String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró al usuario: " + userEmail));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el pedido con el ID: " + orderId));

        if (user.getRole() == Role.ROLE_USER) {
            if (!order.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException(
                        "Solo puedes gestionar tus propios pedidos");
            }
            if (status != OrderStatus.CANCELLED) {
                throw new UnauthorizedException(
                        "Los usuarios solo pueden cancelar pedidos");
            }
        }

        if (status == OrderStatus.CANCELLED &&
                order.getStatus() != OrderStatus.CANCELLED) {
            timeSlotService.decrementOrders(order.getTimeSlot().getId());
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