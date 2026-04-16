package com.example.PlanR.config.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;

/**
 * Seeder #4: Seeds essential system users (Superadmin).
 * Runs in ALL environments.
 */
@Component
public class UserSeeder implements DataSeederBase {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, DepartmentRepository departmentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void seed() {
        Department sysDept = departmentRepository.findByShortCode("SYS").orElse(null);

        // Seed Superadmin
        if (userRepository.findByEmail("superadmin@planr.com").isEmpty()) {
            User superAdmin = new User("System Administrator", "superadmin@planr.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setRole(Role.SUPERADMIN);
            superAdmin.setDepartment(sysDept);
            userRepository.save(superAdmin);
            log.info("SUPERADMIN INITIALIZED: superadmin@planr.com / superadmin123");
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
