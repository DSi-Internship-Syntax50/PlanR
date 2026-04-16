package com.example.PlanR.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.exception.EntityNotFoundException;
import com.example.PlanR.exception.ValidationException;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;

/**
 * Service encapsulating user registration logic.
 * Eliminates duplication between AuthController and RegistrationController.
 */
@Service
public class UserRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository,
                                   DepartmentRepository departmentRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user from the given DTO.
     * @return the saved User entity
     * @throws ValidationException if email already exists
     * @throws EntityNotFoundException if department ID is invalid
     */
    public User registerUser(UserRegistrationDto dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("There is already an account registered with email: " + email);
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department", dto.getDepartmentId()));
        user.setDepartment(department);

        userRepository.save(user);
        logger.info("User registered successfully: {} with role {}", email, user.getRole());

        return user;
    }

    /**
     * Returns roles that can be assigned during registration (excludes SUPERADMIN).
     */
    public List<Role> getAssignableRoles() {
        return Arrays.stream(Role.values())
                .filter(role -> role != Role.SUPERADMIN)
                .collect(Collectors.toList());
    }

    /**
     * Returns departments that can be assigned during registration (excludes SYS).
     */
    public List<Department> getAssignableDepartments() {
        return departmentRepository.findAll().stream()
                .filter(dept -> !"SYS".equals(dept.getShortCode()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).isPresent();
    }
}
