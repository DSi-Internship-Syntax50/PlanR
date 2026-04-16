package com.example.PlanR.config.seeder;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;

/**
 * Seeder #3: Seeds rooms if the table is empty.
 */
@Component
public class RoomSeeder implements DataSeederBase {

    private static final Logger log = LoggerFactory.getLogger(RoomSeeder.class);
    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;

    public RoomSeeder(RoomRepository roomRepository, DepartmentRepository departmentRepository) {
        this.roomRepository = roomRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void seed() {
        if (roomRepository.count() > 0) return;

        Department sysDept = departmentRepository.findByShortCode("SYS").orElse(null);
        Department cseDept = departmentRepository.findByShortCode("CSE").orElse(null);
        Department eeeDept = departmentRepository.findByShortCode("EEE").orElse(null);
        Department bbaDept = departmentRepository.findByShortCode("BBA").orElse(null);
        Department ceDept = departmentRepository.findByShortCode("CE").orElse(null);

        // Seed special rooms
        Room auditorium = new Room();
        auditorium.setRoomNumber("Main Auditorium");
        auditorium.setType(RoomType.SEMINAR);
        auditorium.setCapacity(500);
        auditorium.setBlock(" ");
        auditorium.setDepartment(sysDept);

        Room seminarHall = new Room();
        seminarHall.setRoomNumber("Seminar Hall C");
        seminarHall.setType(RoomType.LAB);
        seminarHall.setCapacity(500);
        seminarHall.setBlock(" ");
        seminarHall.setDepartment(cseDept);

        Room stadium = new Room();
        stadium.setRoomNumber("Indoor Stadium");
        stadium.setType(RoomType.THEORY);
        stadium.setCapacity(500);
        stadium.setBlock(" ");
        stadium.setDepartment(sysDept);

        roomRepository.saveAll(Arrays.asList(auditorium, seminarHall, stadium));

        // Seed grid rooms (4 floors × 7 rooms × 3 blocks)
        Department[] deptCycle = { cseDept, eeeDept, bbaDept, ceDept, cseDept };
        String[] deptCodeCycle = { "CSE", "EEE", "BBA", "CE", "CSE" };

        for (int fl = 1; fl < 5; fl++) {
            for (int i = 1; i <= 7; i++) {
                for (char ch = 'A'; ch <= 'C'; ch++) {
                    Room r = new Room();
                    r.setFloorNumber(fl);
                    r.setBlock(String.valueOf(ch));
                    r.setRoomNumber("0" + i);

                    // Make some rooms Labs so the generator has places to put Lab courses
                    r.setType(i % 3 == 0 ? RoomType.LAB : RoomType.THEORY);

                    int deptIndex = i % 5;
                    r.setDept(deptCodeCycle[deptIndex]);
                    r.setDepartment(deptCycle[deptIndex]);

                    roomRepository.save(r);
                }
            }
        }
        log.info("Seeded rooms with department assignments successfully.");
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
