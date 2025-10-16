package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
public class WaistcoatMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    LocalDate date;
    Double length;
    Double shoulder;
    Double neck;
    Double chest;
    Double chestFitting;
    Double hip;
    Double bainSize;
    String bainType; //round, square, cut
    String damanType; // round, square
    String notes; // round, square

    String qty;

    @ManyToOne
    Client client;

    public WaistcoatMeasurement() {
    }

}
