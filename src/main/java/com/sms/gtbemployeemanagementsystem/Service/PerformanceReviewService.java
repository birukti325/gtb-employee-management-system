package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Performance_review;
import com.sms.gtbemployeemanagementsystem.Repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PerformanceReviewService {

    @Autowired
    private PerformanceReviewRepository repository;

    public List<Performance_review> getAllReviews() {
        return repository.findAll();
    }

    public List<Performance_review> getReviewsByEmployeeId(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    public Performance_review saveReview(Performance_review review) {
        return repository.save(review);
    }

    public void deleteReview(Long id) {
        repository.deleteById(id);
    }
}