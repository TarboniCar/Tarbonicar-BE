package com.tarbonicar.backend.api.category.repository;

import com.tarbonicar.backend.api.category.entity.CarName;
import com.tarbonicar.backend.api.category.entity.CarType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarNameRepository extends JpaRepository<CarName, Long> {
    Optional<CarName> findByCarNameAndCarType(String carName, CarType carType);
    List<CarName> findAllByCarType(CarType carType);
    Optional<CarName> findByCarName(String carName);
}
