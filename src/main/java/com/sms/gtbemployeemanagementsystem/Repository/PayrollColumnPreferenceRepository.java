package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.PayrollColumnPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollColumnPreferenceRepository extends JpaRepository<PayrollColumnPreference, Long> {
    Optional<PayrollColumnPreference> findByColumnKey(String columnKey);
}