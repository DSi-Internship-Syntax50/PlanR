package com.example.PlanR.dto;

import com.example.PlanR.model.enums.Role;

/**
 * DTO for profile update requests, replacing 10 individual @RequestParam parameters.
 */
public class ProfileUpdateDto {

    private String name;
    private String studentId;
    private String currentBatch;
    private String admittedSemester;
    private String enrollmentStatus;
    private String labClearanceStatus;
    private Integer seniorityRank;
    private Boolean isCr;
    private Role role;

    public ProfileUpdateDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCurrentBatch() { return currentBatch; }
    public void setCurrentBatch(String currentBatch) { this.currentBatch = currentBatch; }

    public String getAdmittedSemester() { return admittedSemester; }
    public void setAdmittedSemester(String admittedSemester) { this.admittedSemester = admittedSemester; }

    public String getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }

    public String getLabClearanceStatus() { return labClearanceStatus; }
    public void setLabClearanceStatus(String labClearanceStatus) { this.labClearanceStatus = labClearanceStatus; }

    public Integer getSeniorityRank() { return seniorityRank; }
    public void setSeniorityRank(Integer seniorityRank) { this.seniorityRank = seniorityRank; }

    public Boolean getIsCr() { return isCr; }
    public void setIsCr(Boolean isCr) { this.isCr = isCr; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
