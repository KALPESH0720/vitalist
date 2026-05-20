package com.hospital.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", unique = true, nullable = false)
    private String patientId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String contact;

    @Column(name = "blood_group")
    private String bloodGroup;

    private String address;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    private String ward;

    @Column(name = "room_number")
    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_doctor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","createdAt"})
    @Fetch(FetchMode.SELECT)
    private User assignedDoctor;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ADMITTED;

    @Column(name = "emergency_notes", columnDefinition = "TEXT")
    private String emergencyNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public enum Gender { MALE, FEMALE, OTHER }
    public enum Status { ADMITTED, DISCHARGED, OUTPATIENT }
}
