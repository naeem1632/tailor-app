package com.example.tailorapp.service;

import com.example.tailorapp.model.DressMeasurement;
import com.example.tailorapp.repository.MeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MeasurementService {

    private final MeasurementRepository repo;

    public MeasurementService(MeasurementRepository repo) { this.repo = repo; }

    public void save(DressMeasurement m) {
        repo.save(m);
    }
    public List<DressMeasurement> findByClient(Long clientId) { return repo.findByClientId(clientId); }

    public Optional<DressMeasurement> findById(Long measurementId) {
        return repo.findById(measurementId);
    }

    public void deleteById(Long measurementId) {
        repo.deleteById(measurementId);
    }
}
