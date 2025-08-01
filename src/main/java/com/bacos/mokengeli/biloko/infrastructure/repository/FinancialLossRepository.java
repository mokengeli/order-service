package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.FinancialLoss;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialLossRepository extends JpaRepository<FinancialLoss, Long> {
}