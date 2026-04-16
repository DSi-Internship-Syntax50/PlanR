package com.example.PlanR.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;

/**
 * Handles room allocation and seat plan pages.
 * Extracted from the monolithic DashboardController.
 */
@Controller
public class AllocationController {

    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;

    public AllocationController(RoomRepository roomRepository, DepartmentRepository departmentRepository) {
        this.roomRepository = roomRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/allocation")
    public String showAllocationHub(Model model) {
        List<Room> rooms = roomRepository.findAll();
        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("rooms", rooms);
        model.addAttribute("departments", departments);
        return "allocation";
    }

    @GetMapping("/operations")
    public String showSeatPlanTool(Model model) {
        List<Room> rooms = roomRepository.findAll();
        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("rooms", rooms);
        model.addAttribute("departments", departments);
        return "seatplan";
    }
}
