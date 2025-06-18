package com.tarbonicar.backend.api.category.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@Table(name = "category_type")
@NoArgsConstructor
@AllArgsConstructor
public class CarType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_type_id")
    private Long id;

    @Column(length = 10)
    private String carType;
}
