package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class WaistcoatMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String type; // shwalwar kameez, waistCoat
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

    @ManyToOne
    Client client;

    public WaistcoatMeasurement() {
    }

}
