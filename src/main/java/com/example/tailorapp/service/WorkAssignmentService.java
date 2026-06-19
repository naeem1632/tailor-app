package com.example.tailorapp.service;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.PieceRateSettings;
import com.example.tailorapp.model.WorkAssignment;
import com.example.tailorapp.repository.EmployeeRepository;
import com.example.tailorapp.repository.WorkAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkAssignmentService {

    private final WorkAssignmentRepository workAssignmentRepository;
    private final PieceRateSettingsService pieceRateSettingsService;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public WorkAssignmentService(WorkAssignmentRepository workAssignmentRepository,
                                 PieceRateSettingsService pieceRateSettingsService,
                                 EmployeeService employeeService,
                                 EmployeeRepository employeeRepository) {
        this.workAssignmentRepository = workAssignmentRepository;
        this.pieceRateSettingsService = pieceRateSettingsService;
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    public List<WorkAssignment> findAll() {
        return workAssignmentRepository.findAll();
    }

    public Optional<WorkAssignment> findById(Long id) {
        return workAssignmentRepository.findById(id);
    }

    public WorkAssignment save(WorkAssignment assignment) {
        return workAssignmentRepository.save(assignment);
    }

    public void delete(Long id) {
        workAssignmentRepository.deleteById(id);
    }

    /**
     * Create a new work assignment with automatic rate calculation
     */
    public WorkAssignment createAssignment(Payments payment, Employee employee, String workType,
                                          String itemType, Integer assignedCount, Boolean isDesignWork,
                                          String embellishmentType, LocalDate assignedDate) {
        return createAssignment(payment, employee, workType, itemType, assignedCount,
                              isDesignWork, embellishmentType, assignedDate, null);
    }

    public WorkAssignment createAssignment(Payments payment, Employee employee, String workType,
                                          String itemType, Integer assignedCount, Boolean isDesignWork,
                                          String embellishmentType, LocalDate assignedDate,
                                          java.time.LocalTime assignedTime) {

        // Validate employee has required skill
        validateEmployeeSkill(employee, workType, isDesignWork);

        // Get the rate for this work
        PieceRateSettings settings = pieceRateSettingsService.getSettingsForDate(assignedDate);
        Long ratePerPiece = pieceRateSettingsService.getRate(settings, workType, itemType,
                                                              isDesignWork, embellishmentType);

        // Create assignment
        WorkAssignment assignment = new WorkAssignment();
        assignment.setPayment(payment);
        assignment.setEmployee(employee);
        assignment.setWorkType(workType);
        assignment.setItemType(itemType);
        assignment.setAssignedCount(assignedCount);
        assignment.setIsDesignWork(isDesignWork != null ? isDesignWork : false);
        assignment.setEmbellishmentType(embellishmentType);
        assignment.setAssignedDate(assignedDate);
        assignment.setAssignedTime(assignedTime);
        assignment.setRatePerPiece(ratePerPiece);
        assignment.setTotalAmount((long) assignedCount * ratePerPiece);
        assignment.setStatus("ASSIGNED");
        assignment.setIsPaid(false);

        return workAssignmentRepository.save(assignment);
    }

    /**
     * Validate employee has the required skill for the work type
     */
    private void validateEmployeeSkill(Employee employee, String workType, Boolean isDesignWork) {
        boolean hasSkill = switch (workType.toUpperCase()) {
            case "CUTTING" -> employee.getCanCut() != null && employee.getCanCut();
            case "STITCHING" -> {
                if (isDesignWork != null && isDesignWork) {
                    yield employee.getCanStitchDesign() != null && employee.getCanStitchDesign();
                } else {
                    yield employee.getCanStitchPlain() != null && employee.getCanStitchPlain();
                }
            }
            case "FINISHING" -> employee.getCanDoFinishing() != null && employee.getCanDoFinishing();
            case "EMBELLISHMENT" -> employee.getCanDoEmbellishment() != null && employee.getCanDoEmbellishment();
            default -> false;
        };

        if (!hasSkill) {
            throw new IllegalArgumentException(
                "Employee " + employee.getName() + " does not have the required skill for " + workType
            );
        }
    }

    /**
     * Update assignment status
     */
    public WorkAssignment updateStatus(Long assignmentId, String newStatus) {
        WorkAssignment assignment = workAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        assignment.setStatus(newStatus);

        if ("IN_PROGRESS".equals(newStatus) && assignment.getStartedDate() == null) {
            assignment.setStartedDate(LocalDate.now());
            assignment.setStartedTime(java.time.LocalTime.now());
        }

        if ("COMPLETED".equals(newStatus) && assignment.getCompletedDate() == null) {
            assignment.setCompletedDate(LocalDate.now());
            assignment.setCompletedTime(java.time.LocalTime.now());
        }

        return workAssignmentRepository.save(assignment);
    }

    /**
     * Mark assignment as in progress
     */
    public WorkAssignment startWork(Long assignmentId) {
        return updateStatus(assignmentId, "IN_PROGRESS");
    }

    /**
     * Mark assignment as completed
     */
    public WorkAssignment completeWork(Long assignmentId) {
        return updateStatus(assignmentId, "COMPLETED");
    }

    /**
     * Mark assignment as cancelled
     */
    public WorkAssignment cancelWork(Long assignmentId) {
        return updateStatus(assignmentId, "CANCELLED");
    }

    /**
     * Pause work (revert IN_PROGRESS back to ASSIGNED)
     * Preserves startedDate and startedTime for history
     */
    public WorkAssignment pauseWork(Long assignmentId) {
        WorkAssignment assignment = workAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!"IN_PROGRESS".equals(assignment.getStatus())) {
            throw new RuntimeException("Only in-progress work can be paused");
        }

        assignment.setStatus("ASSIGNED");

        // Add pause note to track history
        String pauseNote = "Paused on " + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        if (assignment.getNotes() == null || assignment.getNotes().isEmpty()) {
            assignment.setNotes(pauseNote);
        } else {
            assignment.setNotes(assignment.getNotes() + "; " + pauseNote);
        }

        return workAssignmentRepository.save(assignment);
    }

    /**
     * Reassign work to a different employee
     */
    public WorkAssignment reassignWork(Long assignmentId, Long newEmployeeId, String reason) {
        WorkAssignment assignment = workAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Can only reassign ASSIGNED or IN_PROGRESS work
        if ("COMPLETED".equals(assignment.getStatus()) || "CANCELLED".equals(assignment.getStatus())) {
            throw new RuntimeException("Cannot reassign completed or cancelled work");
        }

        // Cannot reassign if already paid
        if (Boolean.TRUE.equals(assignment.getIsPaid())) {
            throw new RuntimeException("Cannot reassign paid work");
        }

        // Cannot reassign to the same employee
        if (assignment.getEmployee().getId().equals(newEmployeeId)) {
            throw new RuntimeException("Cannot reassign to the same employee. Please select a different employee.");
        }

        Employee newEmployee = employeeRepository.findById(newEmployeeId)
                .orElseThrow(() -> new RuntimeException("New employee not found"));

        // Track original employee if this is first reassignment
        if (assignment.getOriginalEmployeeId() == null) {
            assignment.setOriginalEmployeeId(assignment.getEmployee().getId());
        }

        // Update reassignment tracking
        assignment.setEmployee(newEmployee);
        assignment.setReassignedDate(LocalDateTime.now());
        assignment.setReassignmentReason(reason);

        // Handle null reassignmentCount (for existing assignments created before this field was added)
        Integer currentCount = assignment.getReassignmentCount();
        if (currentCount == null) {
            currentCount = 0;
        }
        assignment.setReassignmentCount(currentCount + 1);

        // If work was in progress, reset to ASSIGNED for new employee
        if ("IN_PROGRESS".equals(assignment.getStatus())) {
            assignment.setStatus("ASSIGNED");
            assignment.setStartedDate(null);
            assignment.setStartedTime(null);
        }

        // Add reassignment note
        String reassignNote = "Reassigned to " + newEmployee.getName() + " on " +
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        if (reason != null && !reason.isEmpty()) {
            reassignNote += " - Reason: " + reason;
        }

        if (assignment.getNotes() == null || assignment.getNotes().isEmpty()) {
            assignment.setNotes(reassignNote);
        } else {
            assignment.setNotes(assignment.getNotes() + "; " + reassignNote);
        }

        return workAssignmentRepository.save(assignment);
    }

    /**
     * Bulk reassign all work from one employee to another
     */
    public int bulkReassignWork(Long fromEmployeeId, Long toEmployeeId, List<String> statuses, String reason) {
        Employee fromEmployee = employeeRepository.findById(fromEmployeeId)
                .orElseThrow(() -> new RuntimeException("Source employee not found"));
        Employee toEmployee = employeeRepository.findById(toEmployeeId)
                .orElseThrow(() -> new RuntimeException("Target employee not found"));

        List<WorkAssignment> assignments = workAssignmentRepository.findByEmployeeOrderByAssignedDateDesc(fromEmployee);

        int reassignedCount = 0;
        for (WorkAssignment assignment : assignments) {
            // Only reassign if status is in the filter list and not paid
            if (statuses.contains(assignment.getStatus()) &&
                !Boolean.TRUE.equals(assignment.getIsPaid())) {
                reassignWork(assignment.getId(), toEmployeeId, reason);
                reassignedCount++;
            }
        }

        return reassignedCount;
    }

    /**
     * Find assignments by payment/order
     */
    public List<WorkAssignment> findByPayment(Payments payment) {
        return workAssignmentRepository.findByPaymentOrderByAssignedDateDesc(payment);
    }

    /**
     * Find assignments by employee
     */
    public List<WorkAssignment> findByEmployee(Employee employee) {
        return workAssignmentRepository.findByEmployeeOrderByAssignedDateDesc(employee);
    }

    /**
     * Find assignments by employee and status
     */
    public List<WorkAssignment> findByEmployeeAndStatus(Employee employee, String status) {
        return workAssignmentRepository.findByEmployeeAndStatusOrderByAssignedDateDesc(employee, status);
    }

    /**
     * Find all active (non-completed, non-cancelled) assignments for an employee
     */
    public List<WorkAssignment> findActiveAssignments(Employee employee) {
        List<WorkAssignment> assigned = findByEmployeeAndStatus(employee, "ASSIGNED");
        List<WorkAssignment> inProgress = findByEmployeeAndStatus(employee, "IN_PROGRESS");
        assigned.addAll(inProgress);
        return assigned;
    }

    /**
     * Find all completed but unpaid work for an employee
     */
    public List<WorkAssignment> findUnpaidCompletedWork(Employee employee) {
        return workAssignmentRepository.findByEmployeeAndStatusAndIsPaidOrderByCompletedDate(
                employee, "COMPLETED", false);
    }

    /**
     * Find assignments by employee within date range
     */
    public List<WorkAssignment> findByEmployeeAndDateRange(Employee employee,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {
        return workAssignmentRepository.findByEmployeeAndAssignedDateBetweenOrderByAssignedDateDesc(
                employee, startDate, endDate);
    }

    /**
     * Find completed assignments by employee within date range
     */
    public List<WorkAssignment> findCompletedWorkInDateRange(Employee employee,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
        return workAssignmentRepository.findByEmployeeAndCompletedDateBetweenAndIsPaid(
                employee, startDate, endDate, false);
    }

    /**
     * Calculate total amount for unpaid completed work
     */
    public Long calculateUnpaidAmount(Employee employee) {
        List<WorkAssignment> unpaidWork = findUnpaidCompletedWork(employee);
        return unpaidWork.stream()
                .mapToLong(WorkAssignment::getTotalAmount)
                .sum();
    }

    /**
     * Calculate total amount for work in date range
     */
    public Long calculateAmountForDateRange(Employee employee, LocalDate startDate, LocalDate endDate) {
        List<WorkAssignment> work = findCompletedWorkInDateRange(employee, startDate, endDate);
        return work.stream()
                .mapToLong(WorkAssignment::getTotalAmount)
                .sum();
    }

    /**
     * Find all assignments by status
     */
    public List<WorkAssignment> findByStatus(String status) {
        return workAssignmentRepository.findByStatusOrderByAssignedDateDesc(status);
    }

    /**
     * Find all assignments by work type
     */
    public List<WorkAssignment> findByWorkType(String workType) {
        return workAssignmentRepository.findByWorkTypeOrderByAssignedDateDesc(workType);
    }

    /**
     * Find all unpaid completed assignments (for payroll)
     */
    public List<WorkAssignment> findAllUnpaidCompleted() {
        return workAssignmentRepository.findByIsPaidAndStatus(false, "COMPLETED");
    }

    /**
     * Get work-in-progress summary statistics
     */
    public WorkInProgressSummary getWorkInProgressSummary() {
        List<WorkAssignment> assigned = findByStatus("ASSIGNED");
        List<WorkAssignment> inProgress = findByStatus("IN_PROGRESS");
        List<WorkAssignment> completed = findByStatus("COMPLETED");

        return new WorkInProgressSummary(
                assigned.size(),
                inProgress.size(),
                completed.size(),
                assigned,
                inProgress,
                completed
        );
    }

    /**
     * Get piece rate settings for a specific date
     */
    public PieceRateSettings getSettingsForDate(LocalDate date) {
        return pieceRateSettingsService.getSettingsForDate(date);
    }

    /**
     * Get rate for specific work
     */
    public Long getRate(PieceRateSettings settings, String workType, String itemType,
                       Boolean isDesignWork, String embellishmentType) {
        return pieceRateSettingsService.getRate(settings, workType, itemType,
                                               isDesignWork, embellishmentType);
    }

    /**
     * Inner class for WIP summary
     */
    public static class WorkInProgressSummary {
        private final int assignedCount;
        private final int inProgressCount;
        private final int completedCount;
        private final List<WorkAssignment> assignedWork;
        private final List<WorkAssignment> inProgressWork;
        private final List<WorkAssignment> completedWork;

        public WorkInProgressSummary(int assignedCount, int inProgressCount, int completedCount,
                                    List<WorkAssignment> assignedWork,
                                    List<WorkAssignment> inProgressWork,
                                    List<WorkAssignment> completedWork) {
            this.assignedCount = assignedCount;
            this.inProgressCount = inProgressCount;
            this.completedCount = completedCount;
            this.assignedWork = assignedWork;
            this.inProgressWork = inProgressWork;
            this.completedWork = completedWork;
        }

        public int getAssignedCount() { return assignedCount; }
        public int getInProgressCount() { return inProgressCount; }
        public int getCompletedCount() { return completedCount; }
        public List<WorkAssignment> getAssignedWork() { return assignedWork; }
        public List<WorkAssignment> getInProgressWork() { return inProgressWork; }
        public List<WorkAssignment> getCompletedWork() { return completedWork; }
        public int getTotalCount() { return assignedCount + inProgressCount + completedCount; }
    }
}