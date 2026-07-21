package com.example.tailorapp.service;

import com.example.tailorapp.model.AdvancePayment;
import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.EmployeePayment;
import com.example.tailorapp.model.WorkAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayrollReportService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private WorkAssignmentService workAssignmentService;

    @Autowired
    private EmployeePaymentService employeePaymentService;

    @Autowired
    private AdvancePaymentService advancePaymentService;

    /**
     * Generate payroll report for all employees within a date range
     */
    public PayrollReport generatePayrollReport(LocalDate startDate, LocalDate endDate, Long employeeId) {
        PayrollReport report = new PayrollReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        List<Employee> employees;
        if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                employees = List.of(employee);
                report.setFilteredEmployee(employee);
            } else {
                employees = employeeService.findActiveEmployees();
            }
        } else {
            employees = employeeService.findActiveEmployees();
        }

        List<EmployeePayrollSummary> summaries = new ArrayList<>();
        Long grandTotalEarned = 0L;
        Long grandTotalAdvances = 0L;
        Long grandTotalPaid = 0L;
        Long grandTotalAdvanceDeducted = 0L;
        Long grandTotalActualCashPaid = 0L;
        Long grandTotalBalance = 0L;

        for (Employee employee : employees) {
            EmployeePayrollSummary summary = generateEmployeeSummary(employee, startDate, endDate);
            summaries.add(summary);

            grandTotalEarned += summary.getTotalEarned();
            grandTotalAdvances += summary.getTotalAdvances();
            grandTotalPaid += summary.getAmountPaid();
            grandTotalAdvanceDeducted += summary.getAdvanceDeducted();
            grandTotalActualCashPaid += summary.getActualCashPaid();
            grandTotalBalance += summary.getBalance();
        }

        report.setEmployeeSummaries(summaries);
        report.setGrandTotalEarned(grandTotalEarned);
        report.setGrandTotalAdvances(grandTotalAdvances);
        report.setGrandTotalPaid(grandTotalPaid);
        report.setGrandTotalAdvanceDeducted(grandTotalAdvanceDeducted);
        report.setGrandTotalActualCashPaid(grandTotalActualCashPaid);
        report.setGrandTotalBalance(grandTotalBalance);

        return report;
    }

    /**
     * Generate payroll summary for a single employee
     */
    private EmployeePayrollSummary generateEmployeeSummary(Employee employee, LocalDate startDate, LocalDate endDate) {
        EmployeePayrollSummary summary = new EmployeePayrollSummary();
        summary.setEmployee(employee);
        summary.setPeriodStart(startDate);
        summary.setPeriodEnd(endDate);

        // Get payment records in date range (by work period)
        List<EmployeePayment> paymentRecords = employeePaymentService.findByEmployee(employee).stream()
                .filter(p -> !p.getWorkPeriodStart().isBefore(startDate) && !p.getWorkPeriodEnd().isAfter(endDate))
                .toList();
        summary.setPaymentRecords(paymentRecords);

        // Calculate total earned from payment records (not individual work assignments)
        Long totalEarned = paymentRecords.stream()
                .mapToLong(EmployeePayment::getTotalAmount)
                .sum();
        summary.setTotalEarned(totalEarned);

        // Count work items from payment records
        int workItemCount = paymentRecords.stream()
                .mapToInt(p -> p.getWorkAssignments() != null ? p.getWorkAssignments().size() : 0)
                .sum();
        summary.setWorkItems(workItemCount);

        // Get advances given in date range
        List<AdvancePayment> advances = advancePaymentService.findByEmployee(employee).stream()
                .filter(a -> !a.getAdvanceDate().isBefore(startDate) && !a.getAdvanceDate().isAfter(endDate))
                .toList();

        Long totalAdvances = advances.stream()
                .mapToLong(AdvancePayment::getAmount)
                .sum();
        summary.setTotalAdvances(totalAdvances);

        // Calculate total amount paid from payment records
        Long amountPaid = paymentRecords.stream()
                .mapToLong(EmployeePayment::getAmountPaid)
                .sum();
        summary.setAmountPaid(amountPaid);

        // Calculate total advance deductions from payment records
        Long advanceDeducted = paymentRecords.stream()
                .mapToLong(EmployeePayment::getTotalAdvanceDeduction)
                .sum();
        summary.setAdvanceDeducted(advanceDeducted);

        // Calculate actual cash/transfer paid (excluding advance deductions)
        Long actualCashPaid = amountPaid - advanceDeducted;
        summary.setActualCashPaid(actualCashPaid);

        // Calculate balance
        // Total cash outflow = Advances Given + Actual Cash Paid
        // Balance = Total Earned - (Advances Given + Actual Cash Paid)
        // Simplified: Total Earned - Advances Given - (Amount Paid - Advance Deducted)
        Long balance = totalEarned - totalAdvances - actualCashPaid;
        summary.setBalance(balance);

        // Determine status
        if (balance == 0) {
            summary.setStatus("SETTLED");
        } else if (balance > 0) {
            summary.setStatus("PENDING");
        } else {
            summary.setStatus("OVERPAID");
        }

        return summary;
    }

    /**
     * Payroll Report model
     */
    public static class PayrollReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private Employee filteredEmployee;
        private List<EmployeePayrollSummary> employeeSummaries;
        private Long grandTotalEarned;
        private Long grandTotalAdvances;
        private Long grandTotalPaid;
        private Long grandTotalAdvanceDeducted;
        private Long grandTotalActualCashPaid;
        private Long grandTotalBalance;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Employee getFilteredEmployee() {
            return filteredEmployee;
        }

        public void setFilteredEmployee(Employee filteredEmployee) {
            this.filteredEmployee = filteredEmployee;
        }

        public List<EmployeePayrollSummary> getEmployeeSummaries() {
            return employeeSummaries;
        }

        public void setEmployeeSummaries(List<EmployeePayrollSummary> employeeSummaries) {
            this.employeeSummaries = employeeSummaries;
        }

        public Long getGrandTotalEarned() {
            return grandTotalEarned;
        }

        public void setGrandTotalEarned(Long grandTotalEarned) {
            this.grandTotalEarned = grandTotalEarned;
        }

        public Long getGrandTotalAdvances() {
            return grandTotalAdvances;
        }

        public void setGrandTotalAdvances(Long grandTotalAdvances) {
            this.grandTotalAdvances = grandTotalAdvances;
        }

        public Long getGrandTotalPaid() {
            return grandTotalPaid;
        }

        public void setGrandTotalPaid(Long grandTotalPaid) {
            this.grandTotalPaid = grandTotalPaid;
        }

        public Long getGrandTotalAdvanceDeducted() {
            return grandTotalAdvanceDeducted;
        }

        public void setGrandTotalAdvanceDeducted(Long grandTotalAdvanceDeducted) {
            this.grandTotalAdvanceDeducted = grandTotalAdvanceDeducted;
        }

        public Long getGrandTotalActualCashPaid() {
            return grandTotalActualCashPaid;
        }

        public void setGrandTotalActualCashPaid(Long grandTotalActualCashPaid) {
            this.grandTotalActualCashPaid = grandTotalActualCashPaid;
        }

        public Long getGrandTotalBalance() {
            return grandTotalBalance;
        }

        public void setGrandTotalBalance(Long grandTotalBalance) {
            this.grandTotalBalance = grandTotalBalance;
        }
    }

    /**
     * Employee Payroll Summary model
     */
    public static class EmployeePayrollSummary {
        private Employee employee;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Integer workItems;
        private Long totalEarned;
        private Long totalAdvances;
        private Long amountPaid;
        private Long advanceDeducted;
        private Long actualCashPaid;
        private Long balance;
        private String status; // PENDING, SETTLED, OVERPAID
        private List<EmployeePayment> paymentRecords;

        public Employee getEmployee() {
            return employee;
        }

        public void setEmployee(Employee employee) {
            this.employee = employee;
        }

        public LocalDate getPeriodStart() {
            return periodStart;
        }

        public void setPeriodStart(LocalDate periodStart) {
            this.periodStart = periodStart;
        }

        public LocalDate getPeriodEnd() {
            return periodEnd;
        }

        public void setPeriodEnd(LocalDate periodEnd) {
            this.periodEnd = periodEnd;
        }

        public Integer getWorkItems() {
            return workItems;
        }

        public void setWorkItems(Integer workItems) {
            this.workItems = workItems;
        }

        public Long getTotalEarned() {
            return totalEarned;
        }

        public void setTotalEarned(Long totalEarned) {
            this.totalEarned = totalEarned;
        }

        public Long getTotalAdvances() {
            return totalAdvances;
        }

        public void setTotalAdvances(Long totalAdvances) {
            this.totalAdvances = totalAdvances;
        }

        public Long getAmountPaid() {
            return amountPaid;
        }

        public void setAmountPaid(Long amountPaid) {
            this.amountPaid = amountPaid;
        }

        public Long getAdvanceDeducted() {
            return advanceDeducted;
        }

        public void setAdvanceDeducted(Long advanceDeducted) {
            this.advanceDeducted = advanceDeducted;
        }

        public Long getActualCashPaid() {
            return actualCashPaid;
        }

        public void setActualCashPaid(Long actualCashPaid) {
            this.actualCashPaid = actualCashPaid;
        }

        public Long getBalance() {
            return balance;
        }

        public void setBalance(Long balance) {
            this.balance = balance;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<EmployeePayment> getPaymentRecords() {
            return paymentRecords;
        }

        public void setPaymentRecords(List<EmployeePayment> paymentRecords) {
            this.paymentRecords = paymentRecords;
        }
    }
}
