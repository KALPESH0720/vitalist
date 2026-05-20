package com.hospital.dto;

import com.hospital.model.Inventory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryDTO {
    private Long id;
    private String medicineName;
    private String batchNumber;
    private Integer quantity;
    private String unit;
    private String supplier;
    private LocalDate expiryDate;
    private Integer reorderLevel;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private String category;
    private boolean lowStock;
    private boolean expired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InventoryDTO fromEntity(Inventory inventory) {
        if (inventory == null) return null;
        return InventoryDTO.builder()
                .id(inventory.getId())
                .medicineName(inventory.getMedicineName())
                .batchNumber(inventory.getBatchNumber())
                .quantity(inventory.getQuantity())
                .unit(inventory.getUnit())
                .supplier(inventory.getSupplier())
                .expiryDate(inventory.getExpiryDate())
                .reorderLevel(inventory.getReorderLevel())
                .purchasePrice(inventory.getPurchasePrice())
                .sellingPrice(inventory.getSellingPrice())
                .category(inventory.getCategory())
                .lowStock(inventory.isLowStock())
                .expired(inventory.isExpired())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
