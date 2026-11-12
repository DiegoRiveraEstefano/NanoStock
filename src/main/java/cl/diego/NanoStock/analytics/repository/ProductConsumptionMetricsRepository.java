package cl.diego.NanoStock.analytics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.analytics.model.ProductConsumptionMetrics;

@Repository
public interface ProductConsumptionMetricsRepository extends JpaRepository<ProductConsumptionMetrics, Long> {

    Optional<ProductConsumptionMetrics> findByProductId(Long productId);
}
