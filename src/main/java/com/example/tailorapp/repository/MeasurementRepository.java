package com.example.tailorapp.repository;

import com.example.tailorapp.model.DressMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeasurementRepository extends JpaRepository<DressMeasurement, Long> {
    List<DressMeasurement> findByClientId(Long clientId);
}
