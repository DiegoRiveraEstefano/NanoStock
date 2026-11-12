package cl.diego.NanoStock.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.MovementType;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement> findByMovementTypeOrderByCreatedAtDesc(MovementType movementType);

    List<InventoryMovement> findByMovementTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
        MovementType movementType, LocalDateTime start, LocalDateTime end
    );

    List<InventoryMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<InventoryMovement> findByProductIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long productId, LocalDateTime start, LocalDateTime end
    );
}
