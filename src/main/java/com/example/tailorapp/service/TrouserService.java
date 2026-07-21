package com.example.tailorapp.service;

import com.example.tailorapp.model.TrouserMeasurement;
import com.example.tailorapp.repository.TrouserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrouserService {

    private final TrouserRepository repo;

    public TrouserService(TrouserRepository repo) { this.repo = repo; }

    public TrouserMeasurement save(TrouserMeasurement m) { return repo.save(m); }
    public List<TrouserMeasurement> findByClient(Long clientId) { return repo.findByClientId(clientId); }

    public Optional<TrouserMeasurement> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
