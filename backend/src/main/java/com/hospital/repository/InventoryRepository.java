package com.hospital.repository;
import com.hospital.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByMedicineNameContainingIgnoreCase(String name);
    List<Inventory> findByCategory(String category);
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderLevel")
    List<Inventory> findLowStock();
    @Query("SELECT i FROM Inventory i WHERE i.expiryDate < CURRENT_DATE")
    List<Inventory> findExpired();
    @Query("SELECT i FROM Inventory i WHERE i.expiryDate BETWEEN CURRENT_DATE AND :soon")
    List<Inventory> findExpiringSoon(LocalDate soon);
}
