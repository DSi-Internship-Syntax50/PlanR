package com.example.PlanR.model;

import com.example.PlanR.model.enums.OverrideStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "class_overrides")
public class ClassOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "routine_id")
    private MasterRoutine routine;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @Enumerated(EnumType.STRING)
    private OverrideStatus status;

    // Constructors
    public ClassOverride() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MasterRoutine getRoutine() {
        return routine;
    }

    public void setRoutine(MasterRoutine routine) {
        this.routine = routine;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
    }

    public OverrideStatus getStatus() {
        return status;
    }

    public void setStatus(OverrideStatus status) {
        this.status = status;
    }
}
