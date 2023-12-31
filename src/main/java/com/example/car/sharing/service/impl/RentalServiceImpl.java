package com.example.car.sharing.service.impl;

import com.example.car.sharing.dto.rental.CreateRentalDto;
import com.example.car.sharing.dto.rental.RentalDto;
import com.example.car.sharing.exception.EntityNotFoundException;
import com.example.car.sharing.mapper.RentalMapper;
import com.example.car.sharing.model.Car;
import com.example.car.sharing.model.Rental;
import com.example.car.sharing.model.User;
import com.example.car.sharing.repository.car.CarRepository;
import com.example.car.sharing.repository.rental.RentalRepository;
import com.example.car.sharing.repository.user.UserRepository;
import com.example.car.sharing.service.NotificationService;
import com.example.car.sharing.service.RentalService;
import com.example.car.sharing.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private static final Logger logger = LoggerFactory.getLogger(RentalServiceImpl.class);
    private static final String NOTIFICATION_NEW_RENTAL = """
            Hello there!
            We are excited to inform you about your new rental details:
            Car Model: %s
            Return Date: %s
            Total Price: %s
            Thank you for choosing our service!
            """;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;

    @Override
    @Transactional
    public RentalDto create(CreateRentalDto createRentalDto) {
        Car car = carRepository.findById(createRentalDto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: "
                        + createRentalDto.getCarId()));
        User user = userService.getAuthenticatedUser();
        List<Rental> rentals = rentalRepository.findByUserId(user.getId());
        for (Rental currentRental : rentals) {
            if (currentRental.isActive()) {
                throw new EntityNotFoundException("You cannot rent more than one car at a time. "
                        + "Please complete the current rental transaction "
                        + "before initiating a new one.");
            }
        }
        if (car.getInventory() <= 0) {
            throw new EntityNotFoundException("Sorry this car is not available now.");
        }
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        Rental newRental = new Rental();
        newRental.setUserId(user.getId());
        newRental.setCarId(car.getId());
        newRental.setRentalDate(LocalDate.now());
        newRental.setReturnDate(createRentalDto.getReturnDate());
        try {
            sendNotificationOfNewRentalToUser(userService.getAuthenticatedUser(), newRental);
            sendNotificationOfNewRentalToAdmins(newRental);
        } catch (Exception e) {
            logger.warn("Error occurred while executing notification in rental service: ", e);
        }
        return rentalMapper.toDto(rentalRepository.save(newRental));
    }

    @Override
    public List<RentalDto> getByUserIdAndStatus(Long userId, Boolean isActive) {
        return rentalRepository.findByUserIdAndIsActive(userId, isActive).stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public List<RentalDto> getAll() {
        return rentalRepository.findAll().stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RentalDto getById(Long id) {
        return rentalMapper.toDto(rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with id: " + id)));
    }

    @Override
    public void setActualReturnDate(Long id, LocalDate actualReturnDate) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with id: " + id));
        rental.setActualReturnDate(actualReturnDate);
        rentalRepository.save(rental);
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: "
                        + rental.getCarId()));
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
    }

    private void sendNotificationOfNewRentalToUser(User user, Rental rental) {
        if (user.getChatId() == null) {
            return;
        }
        Car car = carRepository.findById(rental.getCarId()).get();
        String carName = car.getBrand() + " " + car.getModel();

        LocalDate returnDate = rental.getReturnDate();

        BigDecimal price = telegramBot.getRentalPrice(car, rental);
        notificationService.sendMessage(user.getChatId(),
                String.format(NOTIFICATION_NEW_RENTAL, carName, returnDate, price));
    }

    private void sendNotificationOfNewRentalToAdmins(Rental rental) {
        List<User> admins = userRepository.findAllByRole(User.UserRole.MANAGER);
        Car car = carRepository.findById(rental.getCarId()).get();
        String carName = car.getBrand() + " " + car.getModel();

        LocalDate returnDate = rental.getReturnDate();

        BigDecimal price = telegramBot.getRentalPrice(car, rental);
        for (User admin : admins) {
            if (admin.getChatId() == null) {
                continue;
            }
            notificationService.sendMessage(admin.getChatId(),
                    String.format(NOTIFICATION_NEW_RENTAL, carName, returnDate, price));
        }
    }
}
