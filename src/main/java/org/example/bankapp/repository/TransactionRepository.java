package org.example.bankapp.repository;

import org.example.bankapp.model.Trasaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Trasaction,Long> {
    List<Trasaction> findByAccountId(Long accountId);
}
