package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
public class DressMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "Measurement date is required")
    @PastOrPresent(message = "Measurement date cannot be in the future")
    LocalDate date;
    Double kameezLength;
    Double arm;
    Double upperArm;
    Double centerArm;
    Double lowerArm;
    Double terra;
    Double terraDown;
    Double shoulderArm;
    Double chest;
    Double chestFitting;
    Double waist;
    Double hip;
    Double round;
    Double collarSize;
    String collarType; // 1,2,3,4
    Double bainSize;
    Boolean hasBain; // Whether bain design is selected
    String bainType; //round, square, cut
    String damanType; // round, square
    String damanStitching; // single, double
    String sidePocket; //no, 1, 2
    Boolean frontPocket;
    String frontPocketType; //square, cut, round
    Boolean hasCuffDesign; // Whether cuff design is selected
    String cuffDesign; // round, square, cut
    Double cuffLength;
    Double cuffWidth;
    String cuffType; // single, double
    String wristType; // cuff, open

    Double shalwarLength;
    Double shalwarFitting;
    Double asan;
    Double payncha;
    String jali; // no, 1, 2
    Boolean kanta; // yes, no
    Boolean shalwarPocket;

    // Pajama Measurements
    Double pajamaLength;
    Double pajamaAsan;
    Double pajamaPayncha;
    Double upperFitting;
    Double middleFitting;
    Double lowerFitting;
    String pajamaPocket; // No, 1, 2
    Boolean pajamaElastic; // Yes, No

    String stitchType; // single, double, simple
    Boolean designStitch;
    String buttonType; // plan, metal, touch
    Integer frontPattiKaj; // 4 or 5
    String frontPattiType; // round, square, nock
    String notes;

    Integer DressQty;
    Integer withCollar;
    Integer withBain;
    Integer withDesign;
    Integer withPajama;

    @ManyToOne(fetch = FetchType.LAZY)
    Client client;

    public DressMeasurement() {
    }

    @PostLoad
    private void initializeNullBooleanFields() {
        // Initialize null boolean fields to false to prevent NullPointerException
        if (this.frontPocket == null) {
            this.frontPocket = false;
        }
        if (this.kanta == null) {
            this.kanta = false;
        }
        if (this.shalwarPocket == null) {
            this.shalwarPocket = false;
        }
        if (this.designStitch == null) {
            this.designStitch = false;
        }
        if (this.hasBain == null) {
            this.hasBain = false;
        }
        if (this.hasCuffDesign == null) {
            this.hasCuffDesign = false;
        }
    }

    public String getJali() {
        if (jali != null && (jali.endsWith(".0"))) {
            return jali.substring(0, jali.length() - 2);
        }
        return jali;
    }

}
