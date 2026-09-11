package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Performance_review;
import com.sms.gtbemployeemanagementsystem.Service.PerformanceReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerformanceReviewController {

    @Autowired
    private PerformanceReviewService service;

    public List<Performance_review> getAll() {
        return service.getAllReviews();
    }

    public List<Performance_review> getByEmployee(Long employeeId) {
        return service.getReviewsByEmployeeId(employeeId);
    }

    public Performance_review create(Performance_review review) {
        return service.saveReview(review);
    }

    public void delete(Long id) {
        service.deleteReview(id);
    }
}