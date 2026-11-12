package cl.diego.NanoStock.analytics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.analytics.dto.ConsumptionAutonomyResponse;
import cl.diego.NanoStock.analytics.model.ProductConsumptionMetrics;
import cl.diego.NanoStock.analytics.repository.ProductConsumptionMetricsRepository;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumptionAnalyticsService {

    private final ProductRepository productRepository;
    private final StockBatchRepository stockBatchRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductConsumptionMetricsRepository metricsRepository;

    private static final int DEFAULT_WINDOW_DAYS = 30;

    @Transactional
    public ConsumptionAutonomyResponse calculateAndSaveMetrics(Long productId, int windowDays) {
        int days = windowDays > 0 ? windowDays : DEFAULT_WINDOW_DAYS;

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<InventoryMovement> movements = movementRepository
            .findByProductIdAndCreatedAtBetweenOrderByCreatedAtDesc(productId, since, LocalDateTime.now());

        double totalConsumed = movements.stream()
            .filter(m -> m.getMovementType() == MovementType.CONSUMPTION)
            .mapToDouble(InventoryMovement::getQuantity)
            .sum();

        double averageDaily = totalConsumed / (double) days;
        Double currentStock = stockBatchRepository.sumQuantityByProductId(productId);

        double daysOfAutonomy;
        if (averageDaily > 0) {
            daysOfAutonomy = currentStock / averageDaily;
        } else {
            daysOfAutonomy = currentStock > 0 ? 999.0 : 0.0;
        }

        ProductConsumptionMetrics metrics = metricsRepository.findByProductId(productId)
            .orElseGet(() -> ProductConsumptionMetrics.builder()
                .product(product)
                .build());

        metrics.setAverageDailyConsumption(averageDaily);
        metrics.setEstimatedDaysOfAutonomy(daysOfAutonomy);
        metrics.setLastRecalculatedAt(LocalDateTime.now());
        metricsRepository.save(metrics);

        LocalDate stockoutDate = daysOfAutonomy < 365 ? LocalDate.now().plusDays((long) daysOfAutonomy) : null;
        boolean isRisk = daysOfAutonomy <= 3.0 && currentStock > 0;

        return ConsumptionAutonomyResponse.builder()
            .productId(product.getId())
            .productName(product.getName())
            .sku(product.getSku())
            .unitOfMeasure(product.getUnitOfMeasure())
            .currentStock(currentStock)
            .averageDailyConsumption(averageDaily)
            .estimatedDaysOfAutonomy(daysOfAutonomy)
            .projectedStockoutDate(stockoutDate)
            .isRiskOfStockout(isRisk)
            .build();
    }

    @Transactional
    public ConsumptionAutonomyResponse getAutonomy(Long productId) {
        return calculateAndSaveMetrics(productId, DEFAULT_WINDOW_DAYS);
    }

    @Transactional
    public List<ConsumptionAutonomyResponse> getAllAutonomy() {
        return productRepository.findAll().stream()
            .map(p -> calculateAndSaveMetrics(p.getId(), DEFAULT_WINDOW_DAYS))
            .toList();
    }
}
