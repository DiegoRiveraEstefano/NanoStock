package cl.diego.NanoStock.inventory.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.inventory.model.StockBatch;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, Long>, JpaSpecificationExecutor<StockBatch> {

    List<StockBatch> findByProductId(Long productId);

    List<StockBatch> findByProductIdAndQuantityGreaterThan(Long productId, Double minQuantity);

    @Query("SELECT b FROM StockBatch b WHERE b.product.id = :productId AND b.quantity > :minQuantity " +
           "ORDER BY CASE WHEN b.expirationDate IS NULL THEN 1 ELSE 0 END, b.expirationDate ASC, b.createdAt ASC")
    List<StockBatch> findActiveBatchesFefoOrder(@Param("productId") Long productId, @Param("minQuantity") Double minQuantity);

    List<StockBatch> findByLocationId(Long locationId);

    List<StockBatch> findByLocationIdAndQuantityGreaterThan(Long locationId, Double minQuantity);

    List<StockBatch> findByQuantityGreaterThanAndExpirationDateLessThanEqualOrderByExpirationDateAsc(
        Double minQuantity, LocalDate maxDate
    );

    @Query("SELECT COALESCE(SUM(b.quantity), 0.0) FROM StockBatch b WHERE b.product.id = :productId AND b.quantity > 0")
    Double sumQuantityByProductId(@Param("productId") Long productId);
}
