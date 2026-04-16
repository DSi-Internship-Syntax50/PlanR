package com.example.PlanR.config.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.PlanR.model.Department;
import com.example.PlanR.repository.DepartmentRepository;

/**
 * Seeder #2: Seeds departments if the table is empty.
 */
@Component
public class DepartmentSeeder implements DataSeederBase {

    private static final Logger log = LoggerFactory.getLogger(DepartmentSeeder.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentSeeder(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void seed() {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(new Department("SYS", "System Administration"));
            departmentRepository.save(new Department("CSE", "Computer Science and Engineering"));
            departmentRepository.save(new Department("EEE", "Electrical and Electronic Engineering"));
            departmentRepository.save(new Department("BBA", "Business Administration"));
            departmentRepository.save(new Department("CE", "Civil Engineering"));
            log.info("Seeded 5 departments successfully.");
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
