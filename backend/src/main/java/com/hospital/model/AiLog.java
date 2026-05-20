package com.hospital.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AiLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","assignedDoctor"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password"})
    private User performedBy;

    @Column(name = "input_summary", columnDefinition = "TEXT")
    private String inputSummary;

    @Column(columnDefinition = "LONGTEXT")
    private String result;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Feature { CASE_TWIN, XRAY_ANALYSIS }
}
