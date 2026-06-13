package mercadhar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mercadhar.dto.timeslot.TimeSlotRequest;
import mercadhar.dto.timeslot.TimeSlotResponse;
import mercadhar.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@Tag(name = "Time Slots", description = "Delivery and pickup time slot management")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping("/available")
    @Operation(summary = "Get available time slots for a date")
    public ResponseEntity<List<TimeSlotResponse>> getAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(timeSlotService.findAvailableByDate(date));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Get all time slots for a date (Admin only)")
    public ResponseEntity<List<TimeSlotResponse>> getAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(timeSlotService.findAllByDate(date));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Create a new time slot (Admin only)")
    public ResponseEntity<TimeSlotResponse> create(
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeSlotService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Auth")
    @Operation(summary = "Delete a time slot (Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}