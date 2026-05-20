package com.hospital.repository;
import com.hospital.model.AiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiLogRepository extends JpaRepository<AiLog, Long> {
    List<AiLog> findByPatientId(Long patientId);
    List<AiLog> findByPerformedById(Long userId);
    List<AiLog> findByFeature(AiLog.Feature feature);
}
