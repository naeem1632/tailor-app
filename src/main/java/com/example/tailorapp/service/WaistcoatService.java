package com.example.tailorapp.service;

import com.example.tailorapp.model.WaistcoatMeasurement;
import com.example.tailorapp.repository.WaistcoatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WaistcoatService {

    private final WaistcoatRepository repo;

    public WaistcoatService(WaistcoatRepository repo) { this.repo = repo; }

    public WaistcoatMeasurement save(WaistcoatMeasurement m) { return repo.save(m); }
    public List<WaistcoatMeasurement> findByClient(Long clientId) { return repo.findByClientId(clientId); }

    public Optional<WaistcoatMeasurement> findById(Long measurementId) {
        return repo.findById(measurementId);
    }

    public void deleteById(Long measurementId) {
        repo.deleteById(measurementId);
    }
}
