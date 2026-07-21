package com.example.tailorapp.repository;

import com.example.tailorapp.model.TrouserMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrouserRepository extends JpaRepository<TrouserMeasurement, Long> {
    List<TrouserMeasurement> findByClientId(Long clientId);
}
