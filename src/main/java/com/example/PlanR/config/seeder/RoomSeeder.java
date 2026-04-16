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
 * Uses the room number format from the other branch: block + floor + "0" + roomIndex
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
        for (int fl = 1; fl < 5; fl++) {
            for (int i = 1; i <= 7; i++) {
                for (char ch = 'A'; ch <= 'C'; ch++) {
                    Room r = new Room();
                    r.setFloorNumber(fl);
                    String st = String.valueOf(ch);
                    r.setBlock(st);
                    r.setRoomNumber(fl + st + "0" + i);

                    // Make some rooms Labs so the generator has places to put Lab courses
                    if (i % 3 == 0) {
                        r.setType(RoomType.LAB);
                    } else {
                        r.setType(RoomType.THEORY);
                    }

                    switch (i % 5) {
                        case 0 -> {
                            r.setDept("CSE");
                            r.setDepartment(cseDept);
                        }
                        case 1 -> {
                            r.setDept("EEE");
                            r.setDepartment(eeeDept);
                        }
                        case 2 -> {
                            r.setDept("BBA");
                            r.setDepartment(bbaDept);
                        }
                        case 3 -> {
                            r.setDept("CE");
                            r.setDepartment(ceDept);
                        }
                        default -> {
                            r.setDept("Archi");
                            r.setDepartment(ceDept);
                        } // Fallback to CE
                    }
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
