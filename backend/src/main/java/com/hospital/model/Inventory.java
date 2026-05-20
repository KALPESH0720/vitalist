package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(nullable = false)
    private Integer quantity = 0;

    private String unit;
    private String supplier;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    private String category;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public boolean isLowStock() {
        return quantity != null && reorderLevel != null && quantity <= reorderLevel;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
