package cl.diego.NanoStock.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.analytics.dto.PricePointDto;
import cl.diego.NanoStock.analytics.dto.PriceTrendResponse;
import cl.diego.NanoStock.analytics.model.PriceHistoryRecord;
import cl.diego.NanoStock.analytics.repository.PriceHistoryRepository;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.purchases.model.Store;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceAnalyticsService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public PriceHistoryRecord recordPrice(Long productId, Long storeId, BigDecimal unitPrice, LocalDate recordedAt, Long receiptId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("Comercio no encontrado con ID: " + storeId));

        PriceHistoryRecord record = PriceHistoryRecord.builder()
            .product(product)
            .store(store)
            .unitPrice(unitPrice)
            .recordedAt(recordedAt != null ? recordedAt : LocalDate.now())
            .receiptId(receiptId)
            .build();

        return priceHistoryRepository.save(record);
    }

    @Transactional(readOnly = true)
    public PriceTrendResponse getPriceTrend(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        List<PriceHistoryRecord> history = priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(productId);

        if (history.isEmpty()) {
            return PriceTrendResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .currentPrice(BigDecimal.ZERO)
                .minHistoricalPrice(BigDecimal.ZERO)
                .maxHistoricalPrice(BigDecimal.ZERO)
                .cheapestStoreName("Sin registros")
                .inflationRatePercentage(0.0)
                .priceHistory(List.of())
                .build();
        }

        BigDecimal minPrice = history.stream()
            .map(PriceHistoryRecord::getUnitPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = history.stream()
            .map(PriceHistoryRecord::getUnitPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        PriceHistoryRecord latestRecord = history.get(history.size() - 1);
        BigDecimal currentPrice = latestRecord.getUnitPrice();

        // Comercio más económico histórico
        String cheapestStore = history.stream()
            .min(Comparator.comparing(PriceHistoryRecord::getUnitPrice))
            .map(r -> r.getStore().getName())
            .orElse("Desconocido");

        // Cálculo de variación / inflación doméstica entre el primer y último registro
        BigDecimal firstPrice = history.get(0).getUnitPrice();
        double inflationRate = 0.0;
        if (firstPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = currentPrice.subtract(firstPrice);
            inflationRate = diff.divide(firstPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

        List<PricePointDto> points = history.stream()
            .map(r -> PricePointDto.builder()
                .date(r.getRecordedAt())
                .storeName(r.getStore().getName())
                .unitPrice(r.getUnitPrice())
                .build())
            .toList();

        return PriceTrendResponse.builder()
            .productId(product.getId())
            .productName(product.getName())
            .currentPrice(currentPrice)
            .minHistoricalPrice(minPrice)
            .maxHistoricalPrice(maxPrice)
            .cheapestStoreName(cheapestStore)
            .inflationRatePercentage(inflationRate)
            .priceHistory(points)
            .build();
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> getLatestPrice(Long productId) {
        return priceHistoryRepository.findFirstByProductIdOrderByRecordedAtDesc(productId)
            .map(PriceHistoryRecord::getUnitPrice);
    }
}
