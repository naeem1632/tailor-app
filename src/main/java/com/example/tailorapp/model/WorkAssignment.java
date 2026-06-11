package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class WorkAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull(message = "Payment/Order is required")
    private Payments payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    // ===== WORK DETAILS =====
    @NotBlank(message = "Work type is required")
    private String workType;   // CUTTING, STITCHING, FINISHING, EMBELLISHMENT

    @NotBlank(message = "Item type is required")
    private String itemType;   // KAMEEZ, SHALWAR, PAJAMA, SHIRT, WAISTCOAT

    // For stitching work only - indicates if this is design work
    private Boolean isDesignWork;  // true = with design, false/null = plain

    // For embellishment work only
    private String embellishmentType; // MATEL, TICH, KANTA, JALI, KRHAI

    @NotNull(message = "Assigned count is required")
    @Min(value = 1, message = "Assigned count must be at least 1")
    private Integer assignedCount;  // How many items assigned

    @NotNull(message = "Assigned date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignedDate;

    @DateTimeFormat(pattern = "HH:mm")
    private java.time.LocalTime assignedTime;  // Time when work was assigned

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startedDate;  // When work was started (IN_PROGRESS)

    @DateTimeFormat(pattern = "HH:mm")
    private java.time.LocalTime startedTime;  // Time when work was started

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate completedDate;  // When work was finished

    @DateTimeFormat(pattern = "HH:mm")
    private java.time.LocalTime completedTime;  // Time when work was completed

    @NotBlank(message = "Status is required")
    private String status; // ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    // ===== PAYMENT DETAILS =====
    @NotNull(message = "Rate per piece is required")
    @Min(value = 0, message = "Rate per piece must be zero or positive")
    private Long ratePerPiece;  // Rate at time of assignment

    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount must be zero or positive")
    private Long totalAmount;   // assignedCount * ratePerPiece

    private Boolean isPaid;     // Has employee been paid for this work?

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate paidDate; // When employee was paid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_payment_id")
    private EmployeePayment employeePayment; // Link to payment record

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null || status.isEmpty()) {
            status = "ASSIGNED";
        }
        if (isPaid == null) {
            isPaid = false;
        }
        if (isDesignWork == null) {
            isDesignWork = false;
        }
        // Auto-calculate total amount
        if (assignedCount != null && ratePerPiece != null) {
            totalAmount = (long) assignedCount * ratePerPiece;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Recalculate total if needed
        if (assignedCount != null && ratePerPiece != null) {
            totalAmount = (long) assignedCount * ratePerPiece;
        }
    }
}