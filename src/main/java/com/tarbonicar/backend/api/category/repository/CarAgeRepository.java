package com.tarbonicar.backend.api.category.repository;

import com.tarbonicar.backend.api.category.entity.CarAge;
import com.tarbonicar.backend.api.category.entity.CarName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarAgeRepository extends JpaRepository<CarAge, Long> {
    List<CarAge> findAllByCarName(CarName carName);
}
