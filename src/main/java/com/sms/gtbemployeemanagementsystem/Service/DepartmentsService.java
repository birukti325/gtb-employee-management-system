package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentsService {

    @Autowired
    private DepartmentsRepository departementRepository;

    public List<Departments> findAll() {
        return departementRepository.findAll();
    }

    public Optional<Departments> findById(Long id) {
        return departementRepository.findById(id);
    }

    @Transactional
    public Departments save(Departments departement) {
        return departementRepository.save(departement);
    }

    @Transactional
    public void deleteById(Long id) {
        departementRepository.deleteById(id);
    }
}
