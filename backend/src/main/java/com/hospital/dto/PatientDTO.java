package com.hospital.dto;

import com.hospital.model.Patient;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientDTO {
    private Long id;
    private String patientId;
    private String fullName;
    private Integer age;
    private String gender;
    private String contact;
    private String bloodGroup;
    private String address;
    private LocalDate admissionDate;
    private String ward;
    private String roomNumber;
    private UserDTO assignedDoctor;
    private String status;
    private String emergencyNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PatientDTO fromEntity(Patient patient) {
        if (patient == null) return null;
        return PatientDTO.builder()
                .id(patient.getId())
                .patientId(patient.getPatientId())
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .gender(patient.getGender().name())
                .contact(patient.getContact())
                .bloodGroup(patient.getBloodGroup())
                .address(patient.getAddress())
                .admissionDate(patient.getAdmissionDate())
                .ward(patient.getWard())
                .roomNumber(patient.getRoomNumber())
                .assignedDoctor(UserDTO.fromEntity(patient.getAssignedDoctor()))
                .status(patient.getStatus().name())
                .emergencyNotes(patient.getEmergencyNotes())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
