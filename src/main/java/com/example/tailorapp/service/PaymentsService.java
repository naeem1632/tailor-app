package com.example.tailorapp.service;

import com.example.tailorapp.model.Payments;
import com.example.tailorapp.repository.PaymentsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;

    public PaymentsService(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    public List<Payments> findByClient(Long clientId) {
        return paymentsRepository.findByClientId(clientId);
    }

    public Optional<Payments> findById(Long id) {
        return paymentsRepository.findById(id);
    }

    public Payments save(Payments payments) {
        return paymentsRepository.save(payments);
    }

    public void delete(Long id) {
        paymentsRepository.deleteById(id);
    }
}
