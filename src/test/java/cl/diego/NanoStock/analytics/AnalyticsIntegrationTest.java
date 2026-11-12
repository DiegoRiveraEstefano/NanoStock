package cl.diego.NanoStock.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.analytics.dto.ConsumptionAutonomyResponse;
import cl.diego.NanoStock.analytics.dto.PriceTrendResponse;
import cl.diego.NanoStock.analytics.dto.WasteReportResponse;
import cl.diego.NanoStock.analytics.model.PriceHistoryRecord;
import cl.diego.NanoStock.analytics.repository.PriceHistoryRepository;
import cl.diego.NanoStock.analytics.repository.ProductConsumptionMetricsRepository;
import cl.diego.NanoStock.analytics.service.ConsumptionAnalyticsService;
import cl.diego.NanoStock.analytics.service.PriceAnalyticsService;
import cl.diego.NanoStock.analytics.service.WasteAnalyticsService;
import cl.diego.NanoStock.catalog.dto.CategoryRequest;
import cl.diego.NanoStock.catalog.dto.CategoryResponse;
import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.dto.ConsumeStockRequest;
import cl.diego.NanoStock.inventory.dto.RegisterWasteRequest;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.purchases.dto.ReceiptItemRequest;
import cl.diego.NanoStock.purchases.dto.RecordReceiptRequest;
import cl.diego.NanoStock.purchases.dto.StoreRequest;
import cl.diego.NanoStock.purchases.dto.StoreResponse;
import cl.diego.NanoStock.purchases.model.PaymentMethod;
import cl.diego.NanoStock.purchases.model.StoreType;
import cl.diego.NanoStock.purchases.repository.ReceiptItemRepository;
import cl.diego.NanoStock.purchases.repository.ReceiptRepository;
import cl.diego.NanoStock.purchases.repository.ShoppingListItemRepository;
import cl.diego.NanoStock.purchases.repository.ShoppingListRepository;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import cl.diego.NanoStock.purchases.service.PurchaseService;
import cl.diego.NanoStock.purchases.service.StoreService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
class AnalyticsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PriceAnalyticsService priceAnalyticsService;

    @Autowired
    private ConsumptionAnalyticsService consumptionAnalyticsService;

    @Autowired
    private WasteAnalyticsService wasteAnalyticsService;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private ProductConsumptionMetricsRepository metricsRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private DatabaseCleanupHelper cleanupHelper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        cleanupHelper.cleanAll();
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe registrar historial de precios desde compras y calcular tendencias de inflación y comercio más conveniente")
    void shouldTrackPriceHistoryAndInflationFromPurchases() throws Exception {
        CategoryResponse catLacteos = catalogService.createCategory(CategoryRequest.builder()
            .name("Lácteos")
            .build());

        ProductResponse leche = catalogService.createProduct(CreateProductRequest.builder()
            .name("Leche Entera 1L")
            .sku("LEC-ENT-1L")
            .barCode("7801122334455")
            .categoryId(catLacteos.getId())
            .unitOfMeasure(UnitOfMeasure.LITER)
            .build());

        StoreResponse lider = storeService.createStore(StoreRequest.builder()
            .name("Lider Providencia")
            .storeType(StoreType.SUPERMARKET)
            .build());

        StoreResponse jumbo = storeService.createStore(StoreRequest.builder()
            .name("Jumbo Costanera")
            .storeType(StoreType.SUPERMARKET)
            .build());

        // Compra 1: Lider a $1,000
        purchaseService.recordReceipt(RecordReceiptRequest.builder()
            .storeId(lider.getId())
            .purchaseDate(LocalDate.now().minusDays(15))
            .paymentMethod(PaymentMethod.DEBIT_CARD)
            .items(List.of(
                ReceiptItemRequest.builder()
                    .productId(leche.getId())
                    .quantity(5.0)
                    .unitPrice(new BigDecimal("1000.00"))
                    .build()
            ))
            .build(), "diego");

        // Compra 2: Jumbo a $1,250
        purchaseService.recordReceipt(RecordReceiptRequest.builder()
            .storeId(jumbo.getId())
            .purchaseDate(LocalDate.now())
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .items(List.of(
                ReceiptItemRequest.builder()
                    .productId(leche.getId())
                    .quantity(4.0)
                    .unitPrice(new BigDecimal("1250.00"))
                    .build()
            ))
            .build(), "diego");

        // Verificación de registros de precio
        List<PriceHistoryRecord> records = priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(leche.getId());
        assertThat(records).hasSize(2);

        // Verificación de tendencia y métricas de precio
        PriceTrendResponse trend = priceAnalyticsService.getPriceTrend(leche.getId());
        assertThat(trend.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(trend.getMinHistoricalPrice()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(trend.getMaxHistoricalPrice()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(trend.getCheapestStoreName()).isEqualTo("Lider Providencia");
        assertThat(trend.getInflationRatePercentage()).isEqualTo(25.0); // (1250 - 1000) / 1000 * 100 = 25%

        // Endpoint REST
        mockMvc.perform(get("/api/analytics/prices/" + leche.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(leche.getId()))
            .andExpect(jsonPath("$.currentPrice").value(1250.0))
            .andExpect(jsonPath("$.inflationRatePercentage").value(25.0))
            .andExpect(jsonPath("$.cheapestStoreName").value("Lider Providencia"));
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe calcular velocidad de consumo y días de autonomía tras consumos en inventario")
    void shouldCalculateConsumptionRateAndAutonomy() throws Exception {
        ProductResponse arroz = catalogService.createProduct(CreateProductRequest.builder()
            .name("Arroz Grado 1 1kg")
            .sku("ARR-GRA1-1KG")
            .barCode("7809988776655")
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .minStockThreshold(2.0)
            .build());

        StoreResponse store = storeService.createStore(StoreRequest.builder()
            .name("Almacén de Barrio")
            .storeType(StoreType.LOCAL_STORE)
            .build());

        // Abastecer 10 unidades
        purchaseService.recordReceipt(RecordReceiptRequest.builder()
            .storeId(store.getId())
            .purchaseDate(LocalDate.now())
            .paymentMethod(PaymentMethod.CASH)
            .items(List.of(
                ReceiptItemRequest.builder()
                    .productId(arroz.getId())
                    .quantity(10.0)
                    .unitPrice(new BigDecimal("1500.00"))
                    .build()
            ))
            .build(), "diego");

        // Consumir 3 unidades vía FEFO
        inventoryService.consumeStock(ConsumeStockRequest.builder()
            .productId(arroz.getId())
            .quantity(3.0)
            .notes("Preparación de almuerzo semanal")
            .build(), "diego");

        // Verificar autonomía: stock restante = 7.0, consumo en ventana = 3.0
        ConsumptionAutonomyResponse autonomy = consumptionAnalyticsService.calculateAndSaveMetrics(arroz.getId(), 30);
        assertThat(autonomy.getCurrentStock()).isEqualTo(7.0);
        assertThat(autonomy.getAverageDailyConsumption()).isEqualTo(0.1); // 3.0 / 30 días = 0.1 unidad/día
        assertThat(autonomy.getEstimatedDaysOfAutonomy()).isEqualTo(70.0); // 7.0 / 0.1 = 70 días

        // Endpoint REST
        mockMvc.perform(get("/api/analytics/autonomy/" + arroz.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(arroz.getId()))
            .andExpect(jsonPath("$.currentStock").value(7.0))
            .andExpect(jsonPath("$.estimatedDaysOfAutonomy").value(70.0));
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe estimar pérdida económica y agrupar mermas de alimentos por categoría")
    void shouldCalculateWasteFinancialLossAndCategoryAggregation() throws Exception {
        CategoryResponse catFrescos = catalogService.createCategory(CategoryRequest.builder()
            .name("Frescos y Vegetales")
            .build());

        ProductResponse tomates = catalogService.createProduct(CreateProductRequest.builder()
            .name("Tomate Larga Vida 1kg")
            .sku("TOM-LAR-1KG")
            .barCode("7804455667788")
            .categoryId(catFrescos.getId())
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .build());

        StoreResponse verduleria = storeService.createStore(StoreRequest.builder()
            .name("Verdulería Don Pepe")
            .storeType(StoreType.FARMERS_MARKET)
            .build());

        // Comprar 5 kg a $2,000 / kg
        purchaseService.recordReceipt(RecordReceiptRequest.builder()
            .storeId(verduleria.getId())
            .purchaseDate(LocalDate.now())
            .paymentMethod(PaymentMethod.CASH)
            .items(List.of(
                ReceiptItemRequest.builder()
                    .productId(tomates.getId())
                    .quantity(5.0)
                    .unitPrice(new BigDecimal("2000.00"))
                    .build()
            ))
            .build(), "diego");

        // Obtener el lote generado automáticamente para tomates
        List<StockBatch> batches = stockBatchRepository.findByProductId(tomates.getId());
        assertThat(batches).isNotEmpty();
        StockBatch batch = batches.get(0);

        // Registrar merma de 2 kg por descomposición
        inventoryService.registerWaste(RegisterWasteRequest.builder()
            .batchId(batch.getId())
            .quantity(2.0)
            .wasteReason("Descomposición por calor")
            .build(), "diego");

        // Generar reporte de mermas
        WasteReportResponse report = wasteAnalyticsService.generateWasteReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(report.getTotalIncidents()).isEqualTo(1);
        assertThat(report.getTotalQuantityWasted()).isEqualTo(2.0);
        // Pérdida estimada = 2 kg * $2000 = $4000
        assertThat(report.getTotalEstimatedFinancialLoss()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(report.getLossByCategory()).containsKey("Frescos y Vegetales");
        assertThat(report.getLossByCategory().get("Frescos y Vegetales")).isEqualByComparingTo(new BigDecimal("4000.00"));

        // Endpoint REST
        mockMvc.perform(get("/api/analytics/waste"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIncidents").value(1))
            .andExpect(jsonPath("$.totalQuantityWasted").value(2.0))
            .andExpect(jsonPath("$.totalEstimatedFinancialLoss").value(4000.0));
    }
}
