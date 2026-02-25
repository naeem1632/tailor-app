package com.example.tailorapp.service;

import com.example.tailorapp.model.ShirtMeasurement;
import com.example.tailorapp.repository.ShirtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShirtService {

    private final ShirtRepository repo;

    public ShirtService(ShirtRepository repo) { this.repo = repo; }

    public ShirtMeasurement save(ShirtMeasurement m) { return repo.save(m); }
    public List<ShirtMeasurement> findByClient(Long clientId) { return repo.findByClientId(clientId); }

    public Optional<ShirtMeasurement> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}