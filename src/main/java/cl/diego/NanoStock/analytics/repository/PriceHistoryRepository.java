package cl.diego.NanoStock.analytics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.analytics.model.PriceHistoryRecord;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryRecord, Long> {

    List<PriceHistoryRecord> findByProductIdOrderByRecordedAtAsc(Long productId);

    List<PriceHistoryRecord> findByProductIdOrderByRecordedAtDesc(Long productId);

    Optional<PriceHistoryRecord> findFirstByProductIdOrderByRecordedAtDesc(Long productId);

    List<PriceHistoryRecord> findByProductIdAndStoreIdOrderByRecordedAtDesc(Long productId, Long storeId);
}
