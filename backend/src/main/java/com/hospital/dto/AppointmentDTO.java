package com.hospital.dto;

import com.hospital.model.Appointment;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentDTO {
    private Long id;
    private PatientDTO patient;
    private UserDTO doctor;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String department;
    private String reason;
    private String notes;
    private String status;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppointmentDTO fromEntity(Appointment appointment) {
        if (appointment == null) return null;
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patient(PatientDTO.fromEntity(appointment.getPatient()))
                .doctor(UserDTO.fromEntity(appointment.getDoctor()))
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .department(appointment.getDepartment())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus().name())
                .createdBy(UserDTO.fromEntity(appointment.getCreatedBy()))
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
