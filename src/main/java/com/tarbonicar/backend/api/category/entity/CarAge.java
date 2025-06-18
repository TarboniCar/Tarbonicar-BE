package com.tarbonicar.backend.api.category.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@Table(name = "category_age")
@NoArgsConstructor
@AllArgsConstructor
public class CarAge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_age_id")
    private Long id;

    private int carAge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_name_id", nullable = false)
    private CarName carName;
}
