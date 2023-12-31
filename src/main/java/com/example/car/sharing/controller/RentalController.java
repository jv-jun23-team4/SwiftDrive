package com.example.car.sharing.controller;

import com.example.car.sharing.dto.rental.CreateRentalDto;
import com.example.car.sharing.dto.rental.RentalDto;
import com.example.car.sharing.dto.rental.SetActualReturnDateDto;
import com.example.car.sharing.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental management", description = "Endpoints for managing users' car rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @Operation(summary = "Add a new rental",
            description = "Add a new rental to the selected car if it's available"
                    + " and reduce the car inventory by 1")
    public RentalDto create(@RequestBody CreateRentalDto rental) {
        return rentalService.create(rental);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping
    @Operation(summary = "Get rentals by user ID and its status",
            description = "Get list of all rentals by user ID and "
                    + "whether the rental is still active or not")
    public List<RentalDto> getByUserIdAndStatus(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "is_active", defaultValue = "true") Boolean isActive) {
        return rentalService.getByUserIdAndStatus(userId, isActive);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get specific rental by ID")
    public RentalDto getById(@PathVariable Long id) {
        return rentalService.getById(id);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/all")
    @Operation(summary = "Get list of all rentals")
    public List<RentalDto> getAll() {
        return rentalService.getAll();
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/{id}/return")
    @Operation(summary = "Set actual return date",
            description = "Set actual return date and increase car inventory by 1")
    public void setActualReturnDate(@PathVariable Long id,
                                    @RequestBody SetActualReturnDateDto actualReturnDate) {
        rentalService.setActualReturnDate(id, actualReturnDate.actualReturnDate());
    }
}
