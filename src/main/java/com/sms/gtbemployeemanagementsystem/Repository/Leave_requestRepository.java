package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Leave_request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Leave_requestRepository extends JpaRepository<Leave_request, Long> {
    List<Leave_request> findByEmployee_Id(Long employeeId);
    List<Leave_request> findByStatus(String status);
}
