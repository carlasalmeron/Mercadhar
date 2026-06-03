package mercadhar.service;

import mercadhar.dto.timeslot.TimeSlotRequest;
import mercadhar.dto.timeslot.TimeSlotResponse;
import mercadhar.exception.ResourceNotFoundException;
import mercadhar.model.TimeSlot;
import mercadhar.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlotResponse> findAvailableByDate(LocalDate date) {
        return timeSlotRepository.findByDateAndAvailableTrue(date)
                .stream().map(this::toResponse).toList();
    }

    public List<TimeSlotResponse> findAllByDate(LocalDate date) {
        return timeSlotRepository.findByDate(date)
                .stream().map(this::toResponse).toList();
    }

    public TimeSlot findById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time slot not found with id: " + id));
    }

    public TimeSlotResponse create(TimeSlotRequest request) {
        TimeSlot slot = new TimeSlot();
        slot.setDate(request.getDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setMaxOrders(request.getMaxOrders());
        slot.setCurrentOrders(0);
        slot.setAvailable(true);
        return toResponse(timeSlotRepository.save(slot));
    }

    public void delete(Long id) {
        findById(id);
        timeSlotRepository.deleteById(id);
    }

    // Llamado cuando se crea un pedido
    public void incrementOrders(Long id) {
        TimeSlot slot = findById(id);
        slot.setCurrentOrders(slot.getCurrentOrders() + 1);
        if (slot.isFull()) {
            slot.setAvailable(false);
        }
        timeSlotRepository.save(slot);
    }

    // Llamado cuando se cancela un pedido
    public void decrementOrders(Long id) {
        TimeSlot slot = findById(id);
        if (slot.getCurrentOrders() > 0) {
            slot.setCurrentOrders(slot.getCurrentOrders() - 1);
            slot.setAvailable(true);
        }
        timeSlotRepository.save(slot);
    }

    private TimeSlotResponse toResponse(TimeSlot slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .date(slot.getDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .maxOrders(slot.getMaxOrders())
                .currentOrders(slot.getCurrentOrders())
                .available(slot.isAvailable())
                .spotsLeft(slot.getMaxOrders() - slot.getCurrentOrders())
                .build();
    }
}
