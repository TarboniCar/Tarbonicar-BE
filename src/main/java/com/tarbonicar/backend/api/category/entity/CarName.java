package com.tarbonicar.backend.api.category.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@Table(name = "category_name")
@NoArgsConstructor
@AllArgsConstructor
public class CarName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_name_id")
    private Long id;

    @Column(length = 30)
    private String carName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_type_id", nullable = false)
    private CarType carType;

}
