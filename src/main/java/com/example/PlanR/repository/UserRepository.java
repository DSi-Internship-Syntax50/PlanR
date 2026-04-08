package com.example.PlanR.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PlanR.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}