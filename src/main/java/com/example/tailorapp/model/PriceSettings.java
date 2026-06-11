package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class PriceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate effectiveDate;
    String notes;

    Long dressRate;
    Long waistcoatRate;
    Long shirtRate;

    Long matelRate;
    Long tichRate;
    Long kantaRate;
    Long jaliRate;
    Long krhaiRate;

    public PriceSettings() {
        this.effectiveDate = LocalDate.now();
        this.dressRate = 0L;
        this.waistcoatRate = 0L;
        this.shirtRate = 0L;
        this.matelRate = 0L;
        this.tichRate = 0L;
        this.kantaRate = 0L;
        this.jaliRate = 0L;
        this.krhaiRate = 0L;
    }
}
