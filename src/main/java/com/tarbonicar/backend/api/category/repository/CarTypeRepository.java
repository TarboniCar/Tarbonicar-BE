package com.tarbonicar.backend.api.category.repository;

import com.tarbonicar.backend.api.category.entity.CarType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarTypeRepository extends JpaRepository<CarType, Long> {
    Optional<CarType> findByCarType(String carType);

}
