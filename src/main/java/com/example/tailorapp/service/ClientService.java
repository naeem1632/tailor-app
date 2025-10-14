package com.example.tailorapp.service;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return repo.findByNameContainingIgnoreCaseOrMobileContaining(q, q);
    }
}
