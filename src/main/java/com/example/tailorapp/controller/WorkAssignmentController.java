package com.example.tailorapp.controller;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.WorkAssignment;
import com.example.tailorapp.service.EmployeeService;
import com.example.tailorapp.service.PaymentsService;
import com.example.tailorapp.service.WorkAssignmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/work-assignments")
public class WorkAssignmentController {

    private final WorkAssignmentService workAssignmentService;
    private final EmployeeService employeeService;
    private final PaymentsService paymentsService;

    public WorkAssignmentController(WorkAssignmentService workAssignmentService,
                                   EmployeeService employeeService,
                                   PaymentsService paymentsService) {
        this.workAssignmentService = workAssignmentService;
        this.employeeService = employeeService;
        this.paymentsService = paymentsService;
    }

    /**
     * List all work assignments
     */
    @GetMapping
    public String listAssignments(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) Long employeeId,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                  Model model) {
        List<WorkAssignment> assignments;

        // Get base assignments
        if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                if (status != null && !status.isEmpty()) {
                    assignments = workAssignmentService.findByEmployeeAndStatus(employee, status);
                } else {
                    assignments = workAssignmentService.findByEmployee(employee);
                }
                model.addAttribute("selectedEmployee", employee);
            } else {
                assignments = workAssignmentService.findAll();
            }
        } else if (status != null && !status.isEmpty()) {
            assignments = workAssignmentService.findByStatus(status);
        } else {
            assignments = workAssignmentService.findAll();
        }

        // Filter by order date range if provided
        if (startDate != null && endDate != null) {
            assignments = assignments.stream()
                    .filter(a -> {
                        LocalDate orderDate = a.getPayment().getDate();
                        return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                    })
                    .toList();
        }

        model.addAttribute("assignments", assignments);
        model.addAttribute("employees", employeeService.findActiveEmployees());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "work-assignments/index";
    }

    /**
     * Show form to create work assignment for an order
     */
    @GetMapping("/assign/{paymentId}")
    public String showAssignForm(@PathVariable Long paymentId, Model model, RedirectAttributes redirectAttributes) {
        Payments payment = paymentsService.findById(paymentId).orElse(null);

        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found!");
            return "redirect:/payments";
        }

        // Get existing assignments for this order
        List<WorkAssignment> existingAssignments = workAssignmentService.findByPayment(payment);

        model.addAttribute("payment", payment);
        model.addAttribute("existingAssignments", existingAssignments);
        model.addAttribute("employees", employeeService.findActiveEmployees());
        model.addAttribute("assignment", new WorkAssignment());

        return "work-assignments/assign";
    }

    /**
     * Create work assignment
     */
    @PostMapping("/assign/{paymentId}")
    public String createAssignment(@PathVariable Long paymentId,
                                  @RequestParam Long employeeId,
                                  @RequestParam String workType,
                                  @RequestParam(required = false) String itemType,
                                  @RequestParam(required = false) List<String> itemTypes,
                                  @RequestParam Integer assignedCount,
                                  @RequestParam(required = false) Boolean isDesignWork,
                                  @RequestParam(required = false) String embellishmentType,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate assignedDate,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") java.time.LocalTime assignedTime,
                                  RedirectAttributes redirectAttributes) {
        try {
            Payments payment = paymentsService.findById(paymentId).orElse(null);
            Employee employee = employeeService.findById(employeeId).orElse(null);

            if (payment == null || employee == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid order or employee!");
                return "redirect:/payments";
            }

            // Handle multi-select for stitching work
            if ("STITCHING".equals(workType) && itemTypes != null && !itemTypes.isEmpty()) {
                // Create separate assignment for each selected item type
                int assignmentsCreated = 0;
                for (String selectedItemType : itemTypes) {
                    workAssignmentService.createAssignment(payment, employee, workType, selectedItemType,
                            assignedCount, isDesignWork, embellishmentType, assignedDate, assignedTime);
                    assignmentsCreated++;
                }
                redirectAttributes.addFlashAttribute("success",
                    assignmentsCreated + " work assignment(s) created successfully!");
            } else {
                // Single item type assignment (for cutting, finishing, embellishment)
                if (itemType == null || itemType.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Please select an item type!");
                    return "redirect:/work-assignments/assign/" + paymentId;
                }
                workAssignmentService.createAssignment(payment, employee, workType, itemType,
                        assignedCount, isDesignWork, embellishmentType, assignedDate, assignedTime);
                redirectAttributes.addFlashAttribute("success", "Work assigned successfully!");
            }

            return "redirect:/work-assignments/assign/" + paymentId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/work-assignments/assign/" + paymentId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating assignment: " + e.getMessage());
            return "redirect:/work-assignments/assign/" + paymentId;
        }
    }

    /**
     * Create batch work assignments - assign multiple work types to one employee
     */
    @PostMapping("/assign-batch/{paymentId}")
    public String createBatchAssignment(@PathVariable Long paymentId,
                                       @RequestParam Long employeeId,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate assignedDate,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") java.time.LocalTime assignedTime,
                                       @RequestParam(required = false) List<String> workTypes,
                                       @RequestParam(required = false) List<String> itemTypes,
                                       @RequestParam(required = false) List<Integer> quantities,
                                       @RequestParam(required = false) List<String> designFlags,
                                       @RequestParam(required = false) List<String> embellishmentTypes,
                                       RedirectAttributes redirectAttributes) {
        Payments payment = paymentsService.findById(paymentId).orElse(null);
        Employee employee = employeeService.findById(employeeId).orElse(null);

        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid order!");
            return "redirect:/payments";
        }

        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid employee!");
            return "redirect:/work-assignments/assign/" + paymentId;
        }

        if (workTypes == null || workTypes.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one work type!");
            return "redirect:/work-assignments/assign/" + paymentId;
        }

        try {
            int assignmentsCreated = 0;
            for (int i = 0; i < workTypes.size(); i++) {
                String workType = workTypes.get(i);
                String itemType = itemTypes != null && i < itemTypes.size() ? itemTypes.get(i) : null;
                Integer quantity = quantities != null && i < quantities.size() ? quantities.get(i) : 1;
                Boolean isDesignWork = designFlags != null && i < designFlags.size() && "true".equals(designFlags.get(i));
                String embellishmentType = embellishmentTypes != null && i < embellishmentTypes.size() ? embellishmentTypes.get(i) : null;

                if (itemType != null && !itemType.isEmpty() && quantity > 0) {
                    workAssignmentService.createAssignment(payment, employee, workType, itemType,
                            quantity, isDesignWork, embellishmentType, assignedDate, assignedTime);
                    assignmentsCreated++;
                }
            }

            if (assignmentsCreated > 0) {
                redirectAttributes.addFlashAttribute("success",
                        assignmentsCreated + " work assignment(s) created successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "No valid assignments were created!");
            }

            return "redirect:/work-assignments/assign/" + paymentId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/work-assignments/assign/" + paymentId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating batch assignments: " + e.getMessage());
            return "redirect:/work-assignments/assign/" + paymentId;
        }
    }

    /**
     * Update assignment status
     */
    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                              @RequestParam String status,
                              RedirectAttributes redirectAttributes) {
        try {
            workAssignmentService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * Start work (mark as IN_PROGRESS)
     */
    @PostMapping("/start/{id}")
    public String startWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workAssignmentService.startWork(id);
            redirectAttributes.addFlashAttribute("success", "Work started!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error starting work: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * Complete work (mark as COMPLETED)
     */
    @PostMapping("/complete/{id}")
    public String completeWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workAssignmentService.completeWork(id);
            redirectAttributes.addFlashAttribute("success", "Work completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error completing work: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * Cancel work
     */
    @PostMapping("/cancel/{id}")
    public String cancelWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workAssignmentService.cancelWork(id);
            redirectAttributes.addFlashAttribute("success", "Work cancelled!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling work: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * Pause work (revert IN_PROGRESS back to ASSIGNED)
     */
    @PostMapping("/pause/{id}")
    public String pauseWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workAssignmentService.pauseWork(id);
            redirectAttributes.addFlashAttribute("success", "Work paused successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error pausing work: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * Reassign work to a different employee
     */
    @PostMapping("/reassign/{id}")
    public String reassignWork(@PathVariable Long id,
                              @RequestParam Long newEmployeeId,
                              @RequestParam(required = false) String reason,
                              @RequestParam(required = false) Long paymentId,
                              RedirectAttributes redirectAttributes) {
        try {
            WorkAssignment assignment = workAssignmentService.findById(id).orElse(null);
            if (assignment == null) {
                redirectAttributes.addFlashAttribute("error", "Assignment not found!");
                return "redirect:/work-assignments";
            }

            Long assignmentPaymentId = assignment.getPayment().getId();
            workAssignmentService.reassignWork(id, newEmployeeId, reason);
            redirectAttributes.addFlashAttribute("success", "Work reassigned successfully!");

            // If paymentId is provided, redirect to assign page, otherwise to work-assignments index
            if (paymentId != null) {
                return "redirect:/work-assignments/assign/" + paymentId;
            } else {
                return "redirect:/work-assignments/assign/" + assignmentPaymentId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error reassigning work: " + e.getMessage());

            // Try to redirect back to the assign page if paymentId is available
            if (paymentId != null) {
                return "redirect:/work-assignments/assign/" + paymentId;
            }
            return "redirect:/work-assignments";
        }
    }

    /**
     * Bulk transfer all work from one employee to another
     */
    @PostMapping("/bulk-transfer")
    public String bulkTransferWork(@RequestParam Long fromEmployeeId,
                                  @RequestParam Long toEmployeeId,
                                  @RequestParam List<String> statuses,
                                  @RequestParam(required = false) String reason,
                                  RedirectAttributes redirectAttributes) {
        try {
            int count = workAssignmentService.bulkReassignWork(fromEmployeeId, toEmployeeId, statuses, reason);
            redirectAttributes.addFlashAttribute("success",
                "Successfully transferred " + count + " assignment(s) to new employee!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error transferring work: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * Show edit form for assignment (uses same assign page)
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        WorkAssignment assignment = workAssignmentService.findById(id).orElse(null);

        if (assignment == null) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found!");
            return "redirect:/work-assignments";
        }

        if (assignment.getIsPaid()) {
            redirectAttributes.addFlashAttribute("error", "Cannot edit paid assignment!");
            return "redirect:/work-assignments/assign/" + assignment.getPayment().getId();
        }

        Payments payment = assignment.getPayment();
        List<WorkAssignment> existingAssignments = workAssignmentService.findByPayment(payment);

        model.addAttribute("payment", payment);
        model.addAttribute("existingAssignments", existingAssignments);
        model.addAttribute("employees", employeeService.findActiveEmployees());
        model.addAttribute("assignment", assignment);
        model.addAttribute("editMode", true);

        return "work-assignments/assign";
    }

    /**
     * Update assignment
     */
    @PostMapping("/update/{id}")
    public String updateAssignment(@PathVariable Long id,
                                   @RequestParam Long employeeId,
                                   @RequestParam String workType,
                                   @RequestParam String itemType,
                                   @RequestParam Integer assignedCount,
                                   @RequestParam(required = false) Boolean isDesignWork,
                                   @RequestParam(required = false) String embellishmentType,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate assignedDate,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") java.time.LocalTime assignedTime,
                                   RedirectAttributes redirectAttributes) {
        try {
            WorkAssignment assignment = workAssignmentService.findById(id).orElse(null);
            if (assignment == null) {
                redirectAttributes.addFlashAttribute("error", "Assignment not found!");
                return "redirect:/work-assignments";
            }

            if (assignment.getIsPaid()) {
                redirectAttributes.addFlashAttribute("error", "Cannot update paid assignment!");
                return "redirect:/work-assignments/assign/" + assignment.getPayment().getId();
            }

            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid employee!");
                return "redirect:/work-assignments/assign/" + assignment.getPayment().getId();
            }

            // Update assignment fields
            assignment.setEmployee(employee);
            assignment.setWorkType(workType);
            assignment.setItemType(itemType);
            assignment.setAssignedCount(assignedCount);
            assignment.setIsDesignWork(isDesignWork != null ? isDesignWork : false);
            assignment.setEmbellishmentType(embellishmentType);
            assignment.setAssignedDate(assignedDate);
            assignment.setAssignedTime(assignedTime);

            // Recalculate rate and amount
            var settings = workAssignmentService.getSettingsForDate(assignedDate);
            Long newRate = workAssignmentService.getRate(settings, workType, itemType, isDesignWork, embellishmentType);
            assignment.setRatePerPiece(newRate);
            assignment.setTotalAmount((long) assignedCount * newRate);

            workAssignmentService.save(assignment);

            redirectAttributes.addFlashAttribute("success", "Work assignment updated successfully!");
            return "redirect:/work-assignments/assign/" + assignment.getPayment().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating assignment: " + e.getMessage());
            return "redirect:/work-assignments/edit/" + id;
        }
    }

    /**
     * Delete assignment
     */
    @PostMapping("/delete/{id}")
    public String deleteAssignment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            WorkAssignment assignment = workAssignmentService.findById(id).orElse(null);
            if (assignment != null && assignment.getIsPaid()) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot delete paid assignment. Please contact administrator.");
                return "redirect:/work-assignments";
            }

            Long paymentId = assignment != null ? assignment.getPayment().getId() : null;
            workAssignmentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Assignment deleted successfully!");

            if (paymentId != null) {
                return "redirect:/work-assignments/assign/" + paymentId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting assignment: " + e.getMessage());
        }
        return "redirect:/work-assignments";
    }

    /**
     * View work-in-progress summary
     */
    @GetMapping("/wip-summary")
    public String viewWipSummary(Model model) {
        WorkAssignmentService.WorkInProgressSummary summary = workAssignmentService.getWorkInProgressSummary();
        model.addAttribute("summary", summary);
        return "work-assignments/wip-summary";
    }

    /**
     * Get assignments by employee (AJAX endpoint)
     */
    @GetMapping("/by-employee/{employeeId}")
    @ResponseBody
    public List<WorkAssignment> getAssignmentsByEmployee(@PathVariable Long employeeId) {
        Employee employee = employeeService.findById(employeeId).orElse(null);
        if (employee != null) {
            return workAssignmentService.findByEmployee(employee);
        }
        return List.of();
    }

    /**
     * Get assignments by payment/order (AJAX endpoint)
     */
    @GetMapping("/by-payment/{paymentId}")
    @ResponseBody
    public List<com.example.tailorapp.dto.WorkAssignmentDTO> getAssignmentsByPayment(@PathVariable Long paymentId) {
        Payments payment = paymentsService.findById(paymentId).orElse(null);
        if (payment != null) {
            return workAssignmentService.findByPayment(payment).stream()
                    .map(com.example.tailorapp.dto.WorkAssignmentDTO::fromEntity)
                    .toList();
        }
        return List.of();
    }
}