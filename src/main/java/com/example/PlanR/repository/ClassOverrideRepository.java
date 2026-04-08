package com.example.PlanR.repository;

import com.example.PlanR.model.ClassOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassOverrideRepository extends JpaRepository<ClassOverride, Long> {
}
