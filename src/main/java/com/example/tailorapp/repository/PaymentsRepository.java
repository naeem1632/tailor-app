package com.example.tailorapp.repository;

import com.example.tailorapp.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {
    List<Payments> findByClientId(Long clientId);
}
