package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Leave_request;
import com.sms.gtbemployeemanagementsystem.Repository.Leave_requestRepository; // Ensure this matches your repository name
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveService {

    @Autowired
    private Leave_requestRepository leaveRequestRepository;

    // This fetches all active time-off rows directly from your database
    public List<Leave_request> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
}