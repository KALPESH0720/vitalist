package com.hospital.dto;

import com.hospital.model.AiLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiLogDTO {
    private Long id;
    private String feature;
    private Long patientId;
    private Long performedById;
    private String inputSummary;
    private String result;
    private LocalDateTime createdAt;

    public static AiLogDTO fromEntity(AiLog aiLog) {
        if (aiLog == null) return null;
        return AiLogDTO.builder()
                .id(aiLog.getId())
                .feature(aiLog.getFeature().name())
                .patientId(aiLog.getPatient() != null ? aiLog.getPatient().getId() : null)
                .performedById(aiLog.getPerformedBy() != null ? aiLog.getPerformedBy().getId() : null)
                .inputSummary(aiLog.getInputSummary())
                .result(aiLog.getResult())
                .createdAt(aiLog.getCreatedAt())
                .build();
    }
}

