package com.example.tailorapp.repository;

import com.example.tailorapp.model.AdvancePayment;
import com.example.tailorapp.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvancePaymentRepository extends JpaRepository<AdvancePayment, Long> {

    List<AdvancePayment> findByEmployeeOrderByAdvanceDateDesc(Employee employee);

    List<AdvancePayment> findByStatusOrderByAdvanceDateDesc(String status);

    List<AdvancePayment> findByEmployeeAndStatusOrderByAdvanceDateDesc(Employee employee, String status);

    @Query("SELECT a FROM AdvancePayment a WHERE a.employee = :employee AND a.balanceRemaining > 0 ORDER BY a.advanceDate ASC")
    List<AdvancePayment> findOutstandingByEmployee(@Param("employee") Employee employee);

    @Query("SELECT SUM(a.balanceRemaining) FROM AdvancePayment a WHERE a.employee = :employee AND a.balanceRemaining > 0")
    Long getTotalOutstandingByEmployee(@Param("employee") Employee employee);
}
