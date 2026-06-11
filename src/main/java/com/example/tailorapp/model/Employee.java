package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 20, message = "Mobile number cannot exceed 20 characters")
    private String mobile;

    @Size(max = 20, message = "CNIC cannot exceed 20 characters")
    private String cnic;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    private String employeeStatus; // ACTIVE, INACTIVE, ON_LEAVE

    // ===== SKILLS/ROLES =====
    private Boolean canCut;              // Can do cutting work
    private Boolean canStitchPlain;      // Can do plain stitching
    private Boolean canStitchDesign;     // Can do design stitching
    private Boolean canDoFinishing;      // Can do finishing work (buttons, etc.)
    private Boolean canDoEmbellishment;  // Can do embellishment (Matel, Tich, etc.)

    // ===== PAYMENT INFO =====
    private String paymentFrequency;     // DAILY, WEEKLY, MONTHLY

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (employeeStatus == null || employeeStatus.isEmpty()) {
            employeeStatus = "ACTIVE";
        }
        if (canCut == null) canCut = false;
        if (canStitchPlain == null) canStitchPlain = false;
        if (canStitchDesign == null) canStitchDesign = false;
        if (canDoFinishing == null) canDoFinishing = false;
        if (canDoEmbellishment == null) canDoEmbellishment = false;
    }
}