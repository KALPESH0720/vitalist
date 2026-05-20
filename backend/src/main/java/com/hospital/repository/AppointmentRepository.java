package com.hospital.repository;
import com.hospital.model.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = {"patient", "patient.assignedDoctor", "doctor", "createdBy"})
    List<Appointment> findByPatientId(Long patientId);

    @EntityGraph(attributePaths = {"patient", "patient.assignedDoctor", "doctor", "createdBy"})
    List<Appointment> findByDoctorId(Long doctorId);

    @EntityGraph(attributePaths = {"patient", "patient.assignedDoctor", "doctor", "createdBy"})
    List<Appointment> findByAppointmentDate(LocalDate date);

    @EntityGraph(attributePaths = {"patient", "patient.assignedDoctor", "doctor", "createdBy"})
    List<Appointment> findByStatus(Appointment.Status status);

    List<Appointment> findByAppointmentDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.status = 'SCHEDULED'")
    long countScheduledForDate(LocalDate date);

    @Override
    @EntityGraph(attributePaths = {"patient", "patient.assignedDoctor", "doctor", "createdBy"})
    List<Appointment> findAll();

    @Override
    @EntityGraph(attributePaths = {"patient", "patient.assignedDoctor", "doctor", "createdBy"})
    Optional<Appointment> findById(Long id);
}
