package mercadhar.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mercadhar.model.enums.OrderStatus;
import mercadhar.model.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String customerName;
    private OrderType orderType;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
    private String timeSlotDate;
    private LocalTime timeSlotStart;
    private LocalTime timeSlotEnd;
    private List<OrderItemResponse> items;
}
