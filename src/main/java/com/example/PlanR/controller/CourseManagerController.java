package com.example.PlanR.controller;

import com.example.PlanR.model.enums.Role;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.service.CourseService;
import com.example.PlanR.service.DepartmentService;
import com.example.PlanR.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/courses")
public class CourseManagerController {

    private final CourseService courseService;
    private final DepartmentService departmentService;
    private final UserService userService;

    public CourseManagerController(CourseService courseService, DepartmentService departmentService,
            UserService userService) {
        this.courseService = courseService;
        this.departmentService = departmentService;
        this.userService = userService;
    }

    // 1. READ: Show the Manager Page with Filters
    @GetMapping
    public String viewCourseManager(
            @RequestParam(required = false) Long filterDept,
            @RequestParam(required = false) Integer filterYear,
            @RequestParam(required = false) Integer filterSemester,
            Model model) {

        model.addAttribute("courses", courseService.findWithFilters(filterDept, filterYear, filterSemester));
        model.addAttribute("departments", departmentService.findAllDepartments());
        model.addAttribute("teachers", userService.findUsersByRole(Role.TEACHER));

        // Pass back selected filters so the dropdowns stay selected
        model.addAttribute("filterDept", filterDept);
        model.addAttribute("filterYear", filterYear);
        model.addAttribute("filterSemester", filterSemester);

        return "course-manager";
    }

    // 2. CREATE / UPDATE: Save Course
    @PostMapping("/save")
    public String saveCourse(@ModelAttribute Course course,
            @RequestParam Long departmentId,
            @RequestParam(required = false) Long teacherId) { // Add teacherId

        Department dept = departmentService.findDepartmentById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Dept ID"));
        course.setDepartment(dept);

        // Handle teacher assignment
        if (teacherId != null) {
            User teacher = userService.findUserById(teacherId).orElse(null);
            course.setTeacher(teacher);
        } else {
            course.setTeacher(null); // Unassign if empty
        }

        course.setBatch("L" + course.getYear() + "T" + course.getSemester());
        courseService.saveCourse(course);
        return "redirect:/admin/courses?success=Course Saved";
    }

    // 3. DELETE
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourseById(id);
        return "redirect:/admin/courses?success=Course Deleted";
    }
}