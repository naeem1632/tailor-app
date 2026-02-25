package com.example.tailorapp.model;

import jakarta.persistence.*;
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

    public String getJali() {
        if (jali != null && (jali.endsWith(".0"))) {
            return jali.substring(0, jali.length() - 2);
        }
        return jali;
    }

}
