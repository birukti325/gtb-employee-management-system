package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.PayrollCustomValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollCustomValueRepository extends JpaRepository<PayrollCustomValue, Long> {
    Optional<PayrollCustomValue> findByPayrollIdAndColumnId(Integer payrollId, Long columnId);
}