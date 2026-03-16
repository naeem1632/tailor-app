package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
public class ShirtMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate date;

    // === SHIRT MEASUREMENTS (same fields & order as Kameez) ===
    Double kameezLength;
    Double arm;
    Double shoulderArm;
    Double upperArm;
    Double centerArm;
    Double lowerArm;
    Double cuffLength;
    Double cuffWidth;
    Double terra;
    Double terraDown;
    Double collarSize;
    Double bainSize;
    Double chest;
    Double chestFitting;
    Double waist;
    Double hip;

    // === DESIGN & FINISHING ===
    String collarType;       // No, 1..10
    Boolean hasBain;         // Whether bain design is selected
    String bainType;         // Round-Bain, Square-Bain, Cut-Bain
    Boolean frontPocket;
    String frontPocketType;  // Round, Cut, Square
    Boolean hasCuffDesign;   // Whether cuff design is selected
    String cuffDesign;       // Round-Cuff, Square-Cuff, Cut-Cuff
    String cuffType;         // Single, Double, Open
    String stitchType;       // Single, Double, Simple

    String notes;

    Integer qty;

    @ManyToOne
    Client client;

    public ShirtMeasurement() {
    }

    @PostLoad
    private void initializeNullBooleanFields() {
        // Initialize null boolean fields to false to prevent NullPointerException
        if (this.frontPocket == null) {
            this.frontPocket = false;
        }
        if (this.hasBain == null) {
            this.hasBain = false;
        }
        if (this.hasCuffDesign == null) {
            this.hasCuffDesign = false;
        }
    }
}