package cl.diego.NanoStock.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.analytics.dto.WasteReportResponse;
import cl.diego.NanoStock.catalog.model.Category;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WasteAnalyticsService {

    private final InventoryMovementRepository movementRepository;
    private final PriceAnalyticsService priceAnalyticsService;

    @Transactional(readOnly = true)
    public WasteReportResponse generateWasteReport(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusDays(30);
        LocalDate end = toDate != null ? toDate : LocalDate.now();

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<InventoryMovement> wasteMovements = movementRepository
            .findByMovementTypeAndCreatedAtBetweenOrderByCreatedAtDesc(MovementType.WASTE, startDateTime, endDateTime);

        double totalQuantityWasted = 0.0;
        BigDecimal totalFinancialLoss = BigDecimal.ZERO;
        Map<String, BigDecimal> lossByCategory = new HashMap<>();
        List<WasteReportResponse.WasteIncidentDetail> incidentDetails = new ArrayList<>();

        for (InventoryMovement movement : wasteMovements) {
            Product product = movement.getProduct();
            Double qty = movement.getQuantity() != null ? movement.getQuantity() : 0.0;
            totalQuantityWasted += qty;

            BigDecimal unitPrice = priceAnalyticsService.getLatestPrice(product.getId())
                .orElse(BigDecimal.ZERO);

            BigDecimal estimatedLoss = unitPrice.multiply(BigDecimal.valueOf(qty))
                .setScale(2, RoundingMode.HALF_UP);

            totalFinancialLoss = totalFinancialLoss.add(estimatedLoss);

            Category category = product.getCategory();
            String categoryName = (category != null && category.getName() != null)
                ? category.getName()
                : "Sin Categoría";

            lossByCategory.put(categoryName, lossByCategory.getOrDefault(categoryName, BigDecimal.ZERO).add(estimatedLoss));

            incidentDetails.add(WasteReportResponse.WasteIncidentDetail.builder()
                .productId(product.getId())
                .productName(product.getName())
                .categoryName(categoryName)
                .quantity(qty)
                .reason(movement.getWasteReason() != null ? movement.getWasteReason() : movement.getNotes())
                .estimatedLoss(estimatedLoss)
                .date(movement.getCreatedAt() != null ? movement.getCreatedAt().toLocalDate() : LocalDate.now())
                .build());
        }

        return WasteReportResponse.builder()
            .fromDate(start)
            .toDate(end)
            .totalIncidents(wasteMovements.size())
            .totalQuantityWasted(totalQuantityWasted)
            .totalEstimatedFinancialLoss(totalFinancialLoss)
            .lossByCategory(lossByCategory)
            .incidentDetails(incidentDetails)
            .build();
    }
}
