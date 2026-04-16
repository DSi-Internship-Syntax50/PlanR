package com.example.PlanR.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.service.DepartmentService;
import com.example.PlanR.service.RoomService;

/**
 * Handles room allocation and seat plan pages.
 * Extracted from the monolithic DashboardController.
 */
@Controller
public class AllocationController {

    private final RoomService roomService;
    private final DepartmentService departmentService;

    public AllocationController(RoomService roomService, DepartmentService departmentService) {
        this.roomService = roomService;
        this.departmentService = departmentService;
    }

    @GetMapping("/allocation")
    public String showAllocationHub(Model model) {
        List<Room> rooms = roomService.findAllRooms();
        List<Department> departments = departmentService.findAllDepartments();
        model.addAttribute("rooms", rooms);
        model.addAttribute("departments", departments);
        return "allocation";
    }

    @GetMapping("/operations")
    public String showSeatPlanTool(Model model) {
        List<Room> rooms = roomService.findAllRooms();
        List<Department> departments = departmentService.findAllDepartments();
        model.addAttribute("rooms", rooms);
        model.addAttribute("departments", departments);
        return "seatplan";
    }
}
