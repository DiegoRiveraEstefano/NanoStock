package cl.diego.NanoStock.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.MovementType;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long>, JpaSpecificationExecutor<InventoryMovement> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement> findByMovementTypeOrderByCreatedAtDesc(MovementType movementType);

    List<InventoryMovement> findByMovementTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
        MovementType movementType, LocalDateTime start, LocalDateTime end
    );

    List<InventoryMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<InventoryMovement> findByProductIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long productId, LocalDateTime start, LocalDateTime end
    );

    @Modifying
    @Query("UPDATE InventoryMovement m SET m.batch = null WHERE m.batch.id IN (SELECT b.id FROM StockBatch b WHERE b.location.id = :locationId)")
    void detachBatchesByLocationId(@Param("locationId") Long locationId);

    @Modifying
    @Query("UPDATE InventoryMovement m SET m.location = null WHERE m.location.id = :locationId")
    void detachLocationById(@Param("locationId") Long locationId);
}
