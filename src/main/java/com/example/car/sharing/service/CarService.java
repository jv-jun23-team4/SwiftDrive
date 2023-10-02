package com.example.car.sharing.service;

import com.example.car.sharing.dto.car.CarCreateDto;
import com.example.car.sharing.dto.car.CarDto;
import com.example.car.sharing.dto.car.CarUpdateDto;
import java.util.List;

public interface CarService {
    List<CarDto> findAll(int page);

    CarDto findById(Long id);

    CarDto create(CarCreateDto carCreateDto);

    CarDto update(Long id, CarUpdateDto carUpdateDto);

    void delete(Long id);
}