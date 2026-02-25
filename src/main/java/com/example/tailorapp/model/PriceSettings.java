package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PriceSettings {

    @Id
    Long id = 1L; // Always a single row with id=1

    Long dressRate;
    Long waistcoatRate;
    Long shirtRate;

    Long matelRate;
    Long tichRate;
    Long kantaRate;
    Long jaliRate;
    Long krhaiRate;

    public PriceSettings() {
        this.id = 1L;
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
