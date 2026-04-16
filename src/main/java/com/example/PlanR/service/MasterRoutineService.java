package com.example.PlanR.service;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.repository.MasterRoutineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MasterRoutineService {

    private final MasterRoutineRepository masterRoutineRepository;

    public MasterRoutineService(MasterRoutineRepository masterRoutineRepository) {
        this.masterRoutineRepository = masterRoutineRepository;
    }

    public List<MasterRoutine> findAllRoutines() {
        return masterRoutineRepository.findAll();
    }

    public Optional<MasterRoutine> findRoutineById(Long id) {
        return masterRoutineRepository.findById(id);
    }

    public MasterRoutine saveRoutine(MasterRoutine routine) {
        return masterRoutineRepository.save(routine);
    }

    public void deleteRoutineById(Long id) {
        masterRoutineRepository.deleteById(id);
    }
}
