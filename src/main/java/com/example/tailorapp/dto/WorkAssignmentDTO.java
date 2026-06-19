package com.example.tailorapp.dto;

import com.example.tailorapp.model.WorkAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkAssignmentDTO {
    private Long id;
    private String employeeName;
    private String workType;
    private String itemType;
    private Boolean isDesignWork;
    private String embellishmentType;
    private Integer assignedCount;
    private LocalDate assignedDate;
    private LocalTime assignedTime;
    private Long ratePerPiece;
    private Long totalAmount;
    private String status;
    private LocalDate completedDate;
    private LocalTime completedTime;

    public static WorkAssignmentDTO fromEntity(WorkAssignment assignment) {
        WorkAssignmentDTO dto = new WorkAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setEmployeeName(assignment.getEmployee().getName());
        dto.setWorkType(assignment.getWorkType());
        dto.setItemType(assignment.getItemType());
        dto.setIsDesignWork(assignment.getIsDesignWork());
        dto.setEmbellishmentType(assignment.getEmbellishmentType());
        dto.setAssignedCount(assignment.getAssignedCount());
        dto.setAssignedDate(assignment.getAssignedDate());
        dto.setAssignedTime(assignment.getAssignedTime());
        dto.setRatePerPiece(assignment.getRatePerPiece());
        dto.setTotalAmount(assignment.getTotalAmount());
        dto.setStatus(assignment.getStatus());
        dto.setCompletedDate(assignment.getCompletedDate());
        dto.setCompletedTime(assignment.getCompletedTime());
        return dto;
    }
}
