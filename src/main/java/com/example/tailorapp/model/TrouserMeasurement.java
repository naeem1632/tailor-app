package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
public class TrouserMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate date;

    // === TROUSER MEASUREMENTS (same as Pajama) ===
    Double trouserLength;
    Double trouserAsan;
    Double trouserPayncha;
    Double upperFitting;
    Double middleFitting;
    Double lowerFitting;
    String trouserPocket; // No, 1, 2
    Boolean trouserElastic; // Yes, No

    String notes;

    Integer qty;

    @ManyToOne
    Client client;

    public TrouserMeasurement() {
    }

    @PostLoad
    private void initializeNullBooleanFields() {
        // Initialize null boolean fields to false to prevent NullPointerException
        if (this.trouserElastic == null) {
            this.trouserElastic = false;
        }
    }
}
