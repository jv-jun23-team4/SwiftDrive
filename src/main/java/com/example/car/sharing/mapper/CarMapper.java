package com.example.car.sharing.mapper;

import com.example.car.sharing.config.MapperConfig;
import com.example.car.sharing.dto.car.CarDto;
import com.example.car.sharing.dto.car.CreateCarDto;
import com.example.car.sharing.model.Car;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toEntity(CreateCarDto createCarDto);
}
