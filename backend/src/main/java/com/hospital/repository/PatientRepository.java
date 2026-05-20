package com.hospital.repository;
import com.hospital.model.Patient;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientId(String patientId);

    @EntityGraph(attributePaths = {"assignedDoctor"})
    List<Patient> findByStatus(Patient.Status status);

    List<Patient> findByAssignedDoctorId(Long doctorId);

    @EntityGraph(attributePaths = {"assignedDoctor"})
    List<Patient> findByFullNameContainingIgnoreCase(String name);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = 'ADMITTED'")
    long countAdmitted();

    @Override
    @EntityGraph(attributePaths = {"assignedDoctor"})
    List<Patient> findAll();

    @Override
    @EntityGraph(attributePaths = {"assignedDoctor"})
    Optional<Patient> findById(Long id);
}
