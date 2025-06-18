package com.tarbonicar.backend.api.category.service;

import com.tarbonicar.backend.api.category.dto.CarAgeResponseDTO;
import com.tarbonicar.backend.api.category.dto.CarNameResponseDTO;
import com.tarbonicar.backend.api.category.dto.CarTypeResponseDTO;
import com.tarbonicar.backend.api.category.dto.CategoryCreateDTO;
import com.tarbonicar.backend.api.category.entity.CarAge;
import com.tarbonicar.backend.api.category.entity.CarName;
import com.tarbonicar.backend.api.category.entity.CarType;
import com.tarbonicar.backend.api.category.repository.CarAgeRepository;
import com.tarbonicar.backend.api.category.repository.CarNameRepository;
import com.tarbonicar.backend.api.category.repository.CarTypeRepository;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CarTypeRepository carTypeRepository;
    private final CarNameRepository carNameRepository;
    private final CarAgeRepository carAgeRepository;

    // 카테고리 등록 메서드
    @Transactional
    public void createCategory(CategoryCreateDTO categoryCreateDTO) {

        // 차종 조회 후 없으면 등록
        CarType type = carTypeRepository
                .findByCarType(categoryCreateDTO.getCarType())
                .orElseGet(() ->
                        carTypeRepository.save(
                                CarType.builder()
                                        .carType(categoryCreateDTO.getCarType())
                                        .build()
                        )
                );

        // 차량 조회 후 없으면 등록
        CarName name = carNameRepository
                .findByCarNameAndCarType(categoryCreateDTO.getCarName(), type)
                .orElseGet(() ->
                        carNameRepository.save(
                                CarName.builder()
                                        .carName(categoryCreateDTO.getCarName())
                                        .carType(type)
                                        .build()
                        )
                );

        // 차량 연식 저장
        int ageValue = categoryCreateDTO.getCarAge();
        CarAge age = CarAge.builder()
                .carAge(ageValue)
                .carName(name)
                .build();

        carAgeRepository.save(age);
    }

    // 차종 조회 메서드
    @Transactional(readOnly = true)
    public List<CarTypeResponseDTO> getCarTypeCategory() {

        return carTypeRepository.findAll().stream()
                .map(type -> new CarTypeResponseDTO(
                        type.getId(),
                        type.getCarType()
                ))
                .collect(Collectors.toList());
    }

    // 차량 조회 메서드
    @Transactional(readOnly = true)
    public List<CarNameResponseDTO> getCarNameCategory(String carType) {

        CarType type = carTypeRepository.findByCarType(carType)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARTYPE_EXCEPTION.getMessage()));

        return carNameRepository.findAllByCarType(type).stream()
                .map(name -> new CarNameResponseDTO(
                        name.getId(),
                        name.getCarName()
                ))
                .collect(Collectors.toList());
    }

    // 차량 연식 조회 메서드
    @Transactional(readOnly = true)
    public List<CarAgeResponseDTO> getCarAgeCategory(String carName) {

        CarName name = carNameRepository.findByCarName(carName)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARNAME_EXCEPTION.getMessage()));

        return carAgeRepository.findAllByCarName(name).stream()
                .map(age -> new CarAgeResponseDTO(
                        age.getId(),
                        age.getCarAge()
                ))
                .collect(Collectors.toList());
    }

    // 차량 연식 카테고리 삭제 메서드
    @Transactional
    public void deleteCarAge(Long ageId) {
        CarAge age = carAgeRepository.findById(ageId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARAGE_EXCEPTION.getMessage()));
        carAgeRepository.delete(age);
    }

    // 차량 카테고리 삭제 메서드
    @Transactional
    public void deleteCarName(Long nameId) {
        CarName name = carNameRepository.findById(nameId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARNAME_EXCEPTION.getMessage()));

        // 연식 먼저 삭제
        List<CarAge> ages = carAgeRepository.findAllByCarName(name);
        if (!ages.isEmpty()) {
            carAgeRepository.deleteAll(ages);
        }
        // 그 다음 이름 삭제
        carNameRepository.delete(name);
    }

    // 차량 종류 카테고리 삭제 메서드
    @Transactional
    public void deleteCarType(Long typeId) {
        CarType type = carTypeRepository.findById(typeId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARTYPE_EXCEPTION.getMessage()));

        // 해당 타입의 모든 차명 조회
        List<CarName> names = carNameRepository.findAllByCarType(type);
        // 각 이름에 속한 연식부터 삭제
        for (CarName name : names) {
            List<CarAge> ages = carAgeRepository.findAllByCarName(name);
            if (!ages.isEmpty()) {
                carAgeRepository.deleteAll(ages);
            }
        }
        // 차량 카테고리 삭제
        if (!names.isEmpty()) {
            carNameRepository.deleteAll(names);
        }
        // 마지막으로 차종 타입 삭제
        carTypeRepository.delete(type);
    }
}
