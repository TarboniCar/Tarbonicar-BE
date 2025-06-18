package com.tarbonicar.backend.api.category.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryCreateDTO {

    private String carType;
    private String carName;
    private int carAge;
}
