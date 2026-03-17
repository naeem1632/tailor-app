package com.example.tailorapp.service;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientService {

    private final ClientRepository repo;

    public ClientService(ClientRepository repo) {
        this.repo = repo;
    }

    public Client save(Client c) { return repo.save(c); }
    public List<Client> findAll() { return repo.findAll(); }
    public Optional<Client> findById(Long id) { return repo.findById(id); }
    public void deleteById(Long id) { repo.deleteById(id); }

    public List<Client> search(String q) {
        if (q == null || q.isBlank()) return repo.findAll();

        // Use ArrayList to ensure we can modify the list
        List<Client> results = new ArrayList<>();

        // Try to search by ID first if query is numeric
        try {
            Long id = Long.parseLong(q.trim());
            Optional<Client> clientById = repo.findById(id);
            if (clientById.isPresent()) {
                results.add(clientById.get());
                return results; // Return immediately if exact ID match found
            }
        } catch (NumberFormatException e) {
            // Query is not a number, continue with name/mobile search
        }

        // Search by name and mobile
        results.addAll(repo.findByNameContainingIgnoreCaseOrMobileContaining(q, q));

        return results;
    }

    // Dashboard helper method
    public long count() {
        return repo.count();
    }
}
