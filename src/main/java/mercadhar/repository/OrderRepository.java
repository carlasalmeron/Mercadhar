package mercadhar.repository;

import mercadhar.model.Order;
import mercadhar.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByTimeSlotId(Long tmeSlotId);
}
