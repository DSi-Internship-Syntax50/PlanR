package com.example.PlanR.repository;

import com.example.PlanR.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByBatch(String batch);

    // Dynamic filter: returns all if parameters are null
    @Query("SELECT c FROM Course c WHERE " +
           "(:deptId IS NULL OR c.department.id = :deptId) AND " +
           "(:year IS NULL OR c.year = :year) AND " +
           "(:semester IS NULL OR c.semester = :semester) " +
           "ORDER BY c.year ASC, c.semester ASC, c.courseCode ASC")
    List<Course> findWithFilters(@Param("deptId") Long deptId, 
                                 @Param("year") Integer year, 
                                 @Param("semester") Integer semester);
}