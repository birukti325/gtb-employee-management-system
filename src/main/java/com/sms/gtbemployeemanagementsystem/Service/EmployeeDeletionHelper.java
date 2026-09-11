package com.sms.gtbemployeemanagementsystem.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmployeeDeletionHelper {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Runs a single cleanup delete in its own independent transaction.
     * If this table doesn't exist or uses a different column name, only
     * this one transaction rolls back — it can't poison the caller's
     * transaction or any other table's cleanup.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteFromTable(String table, Long employeeId) {
        try {
            entityManager.createNativeQuery(
                            "DELETE FROM " + table + " WHERE employee_id = :id")
                    .setParameter("id", employeeId)
                    .executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Skipped cleanup for table " + table + ": " + ex.getMessage());
            return false;
        }
    }
}