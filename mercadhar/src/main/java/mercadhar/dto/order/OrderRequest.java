package mercadhar.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mercadhar.model.enums.OrderType;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Time slot is required")
    private Long timeSlotId;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    private String deliveryAddress;
    private String notes;
}
