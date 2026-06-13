package mercadhar.service;

import mercadhar.dto.order.OrderItemRequest;
import mercadhar.dto.order.OrderRequest;
import mercadhar.dto.order.OrderResponse;
import mercadhar.exception.ResourceNotFoundException;
import mercadhar.model.*;
import mercadhar.model.enums.OrderStatus;
import mercadhar.model.enums.OrderType;
import mercadhar.model.enums.Role;
import mercadhar.repository.OrderRepository;
import mercadhar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductService productService;
    @Mock
    private TimeSlotService timeSlotService;

    @InjectMocks
    private OrderService orderService;

    private User mockUser;
    private TimeSlot mockTimeSlot;
    private Product mockProduct;
    private Order mockOrder;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Test User");
        mockUser.setEmail("test@test.com");
        mockUser.setRole(Role.ROLE_USER);

        mockTimeSlot = new TimeSlot();
        mockTimeSlot.setId(1L);
        mockTimeSlot.setDate(LocalDate.now());
        mockTimeSlot.setStartTime(LocalTime.of(10, 0));
        mockTimeSlot.setEndTime(LocalTime.of(11, 0));
        mockTimeSlot.setMaxOrders(5);
        mockTimeSlot.setCurrentOrders(0);
        mockTimeSlot.setAvailable(true);

        Category category = new Category();
        category.setId(1L);
        category.setName("Bebidas");

        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Malta");
        mockProduct.setPrice(new BigDecimal("2.50"));
        mockProduct.setAvailable(true);
        mockProduct.setCategory(category);

        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setTimeSlot(mockTimeSlot);
        mockOrder.setOrderType(OrderType.PICK_UP);
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setTotalAmount(new BigDecimal("5.00"));
        mockOrder.setCreatedAt(LocalDateTime.now());
        mockOrder.setItems(new ArrayList<>());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setOrderType(OrderType.PICK_UP);
        orderRequest.setTimeSlotId(1L);
        orderRequest.setItems(List.of(itemRequest));
    }

    @Test
    @DisplayName("create - pickup order success")
    void create_PickupOrder_ShouldReturnOrderResponse() {
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(timeSlotService.findById(1L)).thenReturn(mockTimeSlot);
        when(productService.findProductById(1L)).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderResponse response = orderService.create(
                orderRequest, "test@test.com");

        assertNotNull(response);
        verify(orderRepository).save(any(Order.class));
        verify(timeSlotService).incrementOrders(1L);
    }

    @Test
    @DisplayName("create - delivery without address throws exception")
    void create_DeliveryWithoutAddress_ShouldThrowException() {
        orderRequest.setOrderType(OrderType.DELIVERY);
        orderRequest.setDeliveryAddress(null);

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(timeSlotService.findById(1L)).thenReturn(mockTimeSlot);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(orderRequest, "test@test.com"));
    }

    @Test
    @DisplayName("create - unavailable time slot throws exception")
    void create_UnavailableTimeSlot_ShouldThrowException() {
        mockTimeSlot.setAvailable(false);

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(timeSlotService.findById(1L)).thenReturn(mockTimeSlot);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(orderRequest, "test@test.com"));
    }

    @Test
    @DisplayName("findMyOrders - returns user orders")
    void findMyOrders_ShouldReturnUserOrders() {
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(orderRepository.findByUserId(1L))
                .thenReturn(List.of(mockOrder));

        List<OrderResponse> result = orderService.findMyOrders("test@test.com");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateStatus - user cancels own order")
    void updateStatus_UserCancelsOwnOrder_ShouldSucceed() {
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(mockOrder);

        orderService.updateStatus(1L, OrderStatus.CANCELLED, "test@test.com");

        verify(orderRepository).save(any(Order.class));
        verify(timeSlotService).decrementOrders(anyLong());
    }

    @Test
    @DisplayName("updateStatus - order not found throws exception")
    void updateStatus_OrderNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateStatus(
                        99L, OrderStatus.CANCELLED, "test@test.com"));
    }
}
