package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.utils.DateUtils;
import com.bacos.mokengeli.biloko.infrastructure.model.DailyOrderSequence;
import com.bacos.mokengeli.biloko.infrastructure.repository.DailyOrderSequenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class OrderNumberService {

    private final DailyOrderSequenceRepository sequenceRepository;

    @Autowired
    public OrderNumberService(DailyOrderSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Génère le prochain numéro de commande pour un tenant donné
     * Reset quotidien à 6h du matin
     */
    @Transactional
    public String generateOrderNumber(String tenantCode) throws ServiceException {
        try {
            LocalDate businessDate = DateUtils.getCurrentBusinessDate();
            int nextSequence = getNextSequence(tenantCode, businessDate);
            
            // Format à 5 chiffres : 00001, 00002, etc.
            String orderNumber = String.format("%05d", nextSequence);
            
            log.info("Generated order number {} for tenant {} on business date {}", 
                    orderNumber, tenantCode, businessDate);
            
            return orderNumber;
            
        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error generating order number for tenant {}: {}", 
                    errorId, tenantCode, e.getMessage(), e);
            throw new ServiceException(errorId, "Failed to generate order number");
        }
    }


    /**
     * Obtient et incrémente la séquence pour un tenant et une date donnés
     * Thread-safe grâce au verrou pessimiste
     */
    @Transactional
    public int getNextSequence(String tenantCode, LocalDate businessDate) throws ServiceException {
        // Tentative de récupération avec verrou
        Optional<DailyOrderSequence> existingSequence = 
                sequenceRepository.findByTenantCodeAndBusinessDateWithLock(tenantCode, businessDate);
        
        if (existingSequence.isPresent()) {
            // Séquence existante : incrémente et sauvegarde
            DailyOrderSequence sequence = existingSequence.get();
            sequence.incrementSequence();
            sequenceRepository.save(sequence);
            return sequence.getCurrentSequence();
            
        } else {
            // Race condition safety: Nouvelle séquence avec gestion des collisions
            try {
                DailyOrderSequence newSequence = DailyOrderSequence.builder()
                        .tenantCode(tenantCode)
                        .businessDate(businessDate)
                        .currentSequence(1)
                        .build();
                
                sequenceRepository.save(newSequence);
                log.info("Created new daily sequence for tenant {} on date {}", tenantCode, businessDate);
                return 1;
                
            } catch (Exception e) {
                // Collision détectée : un autre thread a créé la séquence entre temps
                // Retry avec verrou pour récupérer la séquence maintenant existante
                log.warn("Race condition detected for tenant {} on date {}, retrying with lock", tenantCode, businessDate);
                
                Optional<DailyOrderSequence> retrySequence = 
                        sequenceRepository.findByTenantCodeAndBusinessDateWithLock(tenantCode, businessDate);
                
                if (retrySequence.isPresent()) {
                    DailyOrderSequence sequence = retrySequence.get();
                    sequence.incrementSequence();
                    sequenceRepository.save(sequence);
                    return sequence.getCurrentSequence();
                } else {
                    // Cas très rare : relancer l'exception originale
                    throw new ServiceException(UUID.randomUUID().toString(), 
                            "Failed to create or retrieve sequence after retry: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Obtient la séquence actuelle pour un tenant (sans l'incrémenter)
     */
    public int getCurrentSequence(String tenantCode, LocalDate businessDate) {
        return sequenceRepository.findByTenantCodeAndBusinessDate(tenantCode, businessDate)
                .map(DailyOrderSequence::getCurrentSequence)
                .orElse(0);
    }

    /**
     * Obtient la séquence actuelle pour aujourd'hui
     */
    public int getCurrentSequence(String tenantCode) {
        return getCurrentSequence(tenantCode, DateUtils.getCurrentBusinessDate());
    }

    /**
     * Méthode utilitaire pour obtenir la date métier actuelle (publique)
     */
    public LocalDate getBusinessDate() {
        return DateUtils.getCurrentBusinessDate();
    }
}