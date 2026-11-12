package cl.diego.NanoStock.analytics.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.analytics.dto.ConsumptionAutonomyResponse;
import cl.diego.NanoStock.analytics.dto.PriceTrendResponse;
import cl.diego.NanoStock.analytics.dto.WasteReportResponse;
import cl.diego.NanoStock.analytics.service.ConsumptionAnalyticsService;
import cl.diego.NanoStock.analytics.service.PriceAnalyticsService;
import cl.diego.NanoStock.analytics.service.WasteAnalyticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final PriceAnalyticsService priceAnalyticsService;
    private final ConsumptionAnalyticsService consumptionAnalyticsService;
    private final WasteAnalyticsService wasteAnalyticsService;

    @GetMapping("/prices/{productId}")
    public ResponseEntity<PriceTrendResponse> getPriceTrend(@PathVariable Long productId) {
        return ResponseEntity.ok(priceAnalyticsService.getPriceTrend(productId));
    }

    @GetMapping("/autonomy/{productId}")
    public ResponseEntity<ConsumptionAutonomyResponse> getProductAutonomy(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "30") int windowDays) {
        return ResponseEntity.ok(consumptionAnalyticsService.calculateAndSaveMetrics(productId, windowDays));
    }

    @GetMapping("/autonomy")
    public ResponseEntity<List<ConsumptionAutonomyResponse>> getAllAutonomy() {
        return ResponseEntity.ok(consumptionAnalyticsService.getAllAutonomy());
    }

    @GetMapping("/waste")
    public ResponseEntity<WasteReportResponse> getWasteReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(wasteAnalyticsService.generateWasteReport(fromDate, toDate));
    }
}
