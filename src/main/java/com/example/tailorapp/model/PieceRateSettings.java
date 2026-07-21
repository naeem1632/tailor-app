package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieceRateSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== CUTTING RATES =====
    @NotNull(message = "Kameez cutting rate is required")
    @Min(value = 0, message = "Kameez cutting rate must be zero or positive")
    private Long kameezCuttingRate;

    @NotNull(message = "Shalwar cutting rate is required")
    @Min(value = 0, message = "Shalwar cutting rate must be zero or positive")
    private Long shalwarCuttingRate;

    @NotNull(message = "Pajama cutting rate is required")
    @Min(value = 0, message = "Pajama cutting rate must be zero or positive")
    private Long pajamaCuttingRate;

    @NotNull(message = "Shirt cutting rate is required")
    @Min(value = 0, message = "Shirt cutting rate must be zero or positive")
    private Long shirtCuttingRate;

    @NotNull(message = "Waistcoat cutting rate is required")
    @Min(value = 0, message = "Waistcoat cutting rate must be zero or positive")
    private Long waistcoatCuttingRate;

    @Min(value = 0, message = "Trouser cutting rate must be zero or positive")
    private Long trouserCuttingRate = 0L;

    // ===== STITCHING RATES - PLAIN =====
    @NotNull(message = "Kameez plain stitching rate is required")
    @Min(value = 0, message = "Kameez plain stitching rate must be zero or positive")
    private Long kameezPlainStitchingRate;

    @NotNull(message = "Shalwar plain stitching rate is required")
    @Min(value = 0, message = "Shalwar plain stitching rate must be zero or positive")
    private Long shalwarPlainStitchingRate;

    @NotNull(message = "Pajama stitching rate is required")
    @Min(value = 0, message = "Pajama stitching rate must be zero or positive")
    private Long pajamaStitchingRate;

    @NotNull(message = "Shirt stitching rate is required")
    @Min(value = 0, message = "Shirt stitching rate must be zero or positive")
    private Long shirtStitchingRate;

    @NotNull(message = "Waistcoat stitching rate is required")
    @Min(value = 0, message = "Waistcoat stitching rate must be zero or positive")
    private Long waistcoatStitchingRate;

    @Min(value = 0, message = "Trouser stitching rate must be zero or positive")
    private Long trouserStitchingRate = 0L;

    // ===== STITCHING RATES - WITH DESIGN =====
    @NotNull(message = "Kameez design stitching rate is required")
    @Min(value = 0, message = "Kameez design stitching rate must be zero or positive")
    private Long kameezDesignStitchingRate;

    @NotNull(message = "Shalwar design stitching rate is required")
    @Min(value = 0, message = "Shalwar design stitching rate must be zero or positive")
    private Long shalwarDesignStitchingRate;

    // ===== FINISHING RATES =====
    @NotNull(message = "Button work rate is required")
    @Min(value = 0, message = "Button work rate must be zero or positive")
    private Long buttonWorkRate;

    @NotNull(message = "Embroidery rate is required")
    @Min(value = 0, message = "Embroidery rate must be zero or positive")
    private Long embroideryRate;

    @NotNull(message = "Finishing rate is required")
    @Min(value = 0, message = "Finishing rate must be zero or positive")
    private Long finishingRate;

    // ===== EMBELLISHMENT WORK RATES =====
    @NotNull(message = "Matel work rate is required")
    @Min(value = 0, message = "Matel work rate must be zero or positive")
    private Long matelWorkRate;

    @NotNull(message = "Tich work rate is required")
    @Min(value = 0, message = "Tich work rate must be zero or positive")
    private Long tichWorkRate;

    @NotNull(message = "Kanta work rate is required")
    @Min(value = 0, message = "Kanta work rate must be zero or positive")
    private Long kantaWorkRate;

    @NotNull(message = "Jali work rate is required")
    @Min(value = 0, message = "Jali work rate must be zero or positive")
    private Long jaliWorkRate;

    @NotNull(message = "Krhai work rate is required")
    @Min(value = 0, message = "Krhai work rate must be zero or positive")
    private Long krhaiWorkRate;

    @NotNull(message = "Effective date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}