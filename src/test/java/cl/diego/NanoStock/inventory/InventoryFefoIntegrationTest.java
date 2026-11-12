package cl.diego.NanoStock.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.ConsumeStockRequest;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.dto.ProductStockOverview;
import cl.diego.NanoStock.inventory.dto.RegisterWasteRequest;
import cl.diego.NanoStock.inventory.dto.StockAdjustmentRequest;
import cl.diego.NanoStock.inventory.dto.StockBatchResponse;
import cl.diego.NanoStock.inventory.event.StockConsumedEvent;
import cl.diego.NanoStock.inventory.event.StockLevelChangedEvent;
import cl.diego.NanoStock.inventory.event.StockWastedEvent;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@RecordApplicationEvents
class InventoryFefoIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private DatabaseCleanupHelper cleanupHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();
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
    @WithMockUser(username = "mama", roles = {"MEMBER"})
    @DisplayName("Debe crear una ubicación y registrar una entrada de stock correctamente")
    void shouldCreateLocationAndAddStock() {
        LocationResponse despensa = inventoryService.createLocation(LocationRequest.builder()
            .name("Despensa Principal")
            .storageType(StorageType.AMBIENT)
            .description("Repisa 2 de la cocina")
            .build());

        ProductResponse leche = catalogService.createProduct(CreateProductRequest.builder()
            .name("Leche Descremada")
            .sku("LEC-DESC-1L")
            .barCode("7809988776655")
            .unitOfMeasure(UnitOfMeasure.LITER)
            .minStockThreshold(2.0)
            .build());

        StockBatchResponse batch = inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(leche.getId())
            .locationId(despensa.getId())
            .quantity(4.0)
            .expirationDate(LocalDate.now().plusDays(15))
            .sealedStatus(SealedStatus.SEALED)
            .batchCode("LOTE-001")
            .build(), "mama");

        assertThat(batch.getId()).isNotNull();
        assertThat(batch.getQuantity()).isEqualTo(4.0);
        assertThat(batch.getLocationName()).isEqualTo("Despensa Principal");

        // Validar registro del movimiento
        assertThat(movementRepository.findAll()).hasSize(1);
        assertThat(movementRepository.findAll().get(0).getMovementType()).isEqualTo(MovementType.ENTRY);

        // Validar publicación del evento StockLevelChangedEvent
        long levelEvents = applicationEvents.stream(StockLevelChangedEvent.class)
            .filter(e -> e.productId().equals(leche.getId()) && e.newTotalQuantity().equals(4.0))
            .count();
        assertThat(levelEvents).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Algoritmo FEFO: Debe descontar primero del lote con fecha de caducidad más próxima")
    void shouldDeductUsingFefoOrder() {
        LocationResponse refrigerador = inventoryService.createLocation(LocationRequest.builder()
            .name("Refrigerador")
            .storageType(StorageType.REFRIGERATED)
            .build());

        ProductResponse yogur = catalogService.createProduct(CreateProductRequest.builder()
            .name("Yogur Frutilla")
            .sku("YOG-FRU-01")
            .barCode("7801122334455")
            .unitOfMeasure(UnitOfMeasure.UNIT)
            .minStockThreshold(2.0)
            .build());

        LocalDate hoy = LocalDate.now();

        // Lote 1: Vence en 3 días (Cantidad: 2)
        StockBatchResponse loteUrgente = inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(yogur.getId())
            .locationId(refrigerador.getId())
            .quantity(2.0)
            .expirationDate(hoy.plusDays(3))
            .batchCode("LOTE-URGENTE-3DIAS")
            .build(), "diego");

        // Lote 2: Vence en 10 días (Cantidad: 5)
        StockBatchResponse loteLejano = inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(yogur.getId())
            .locationId(refrigerador.getId())
            .quantity(5.0)
            .expirationDate(hoy.plusDays(10))
            .batchCode("LOTE-LEJANO-10DIAS")
            .build(), "diego");

        // Consumimos 3 unidades: debe agotar completamente el Lote 1 (2 unidades) y sacar 1 unidad del Lote 2
        ProductStockOverview overview = inventoryService.consumeStock(ConsumeStockRequest.builder()
            .productId(yogur.getId())
            .quantity(3.0)
            .notes("Consumo colación familiar")
            .build(), "diego");

        assertThat(overview.getTotalQuantity()).isEqualTo(4.0);

        // Verificar el estado de los lotes individuales
        var batch1 = stockBatchRepository.findById(loteUrgente.getId()).orElseThrow();
        var batch2 = stockBatchRepository.findById(loteLejano.getId()).orElseThrow();

        assertThat(batch1.getQuantity()).isEqualTo(0.0); // Completamente consumido por FEFO
        assertThat(batch2.getQuantity()).isEqualTo(4.0); // 5 - 1 = 4 restantes

        // Validar eventos publicados
        long consumedEvents = applicationEvents.stream(StockConsumedEvent.class)
            .filter(e -> e.productId().equals(yogur.getId()))
            .count();
        assertThat(consumedEvents).isEqualTo(2); // 1 evento por cada lote afectado
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe rechazar consumo si la cantidad solicitada supera el stock total disponible")
    void shouldRejectConsumptionWhenStockIsInsufficient() {
        LocationResponse despensa = inventoryService.createLocation(LocationRequest.builder()
            .name("Despensa")
            .build());

        ProductResponse arroz = catalogService.createProduct(CreateProductRequest.builder()
            .name("Arroz 1kg")
            .sku("ARR-1KG")
            .barCode("7803332221110")
            .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(arroz.getId())
            .locationId(despensa.getId())
            .quantity(1.0)
            .build(), "diego");

        assertThatThrownBy(() -> inventoryService.consumeStock(ConsumeStockRequest.builder()
                .productId(arroz.getId())
                .quantity(3.0)
                .build(), "diego"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe registrar mermas o desperdicio y publicar StockWastedEvent")
    void shouldRegisterWasteAndPublishEvent() {
        LocationResponse despensa = inventoryService.createLocation(LocationRequest.builder()
            .name("Frutero")
            .build());

        ProductResponse tomates = catalogService.createProduct(CreateProductRequest.builder()
            .name("Tomates Granel")
            .sku("TOM-GRA-KG")
            .barCode("7808889990001")
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .build());

        StockBatchResponse batch = inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(tomates.getId())
            .locationId(despensa.getId())
            .quantity(2.5)
            .expirationDate(LocalDate.now().minusDays(1))
            .build(), "diego");

        StockBatchResponse updatedBatch = inventoryService.registerWaste(RegisterWasteRequest.builder()
            .batchId(batch.getId())
            .quantity(1.0)
            .wasteReason("Descompuesto / Hongos")
            .notes("Se pudrieron 2 tomates del fondo")
            .build(), "diego");

        assertThat(updatedBatch.getQuantity()).isEqualTo(1.5);

        long wasteEvents = applicationEvents.stream(StockWastedEvent.class)
            .filter(e -> e.productId().equals(tomates.getId()) && e.quantity().equals(1.0))
            .count();
        assertThat(wasteEvents).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe ajustar stock ante discrepancias en la auditoría física")
    void shouldReconcileStockPhysicalDiscrepancy() {
        LocationResponse bodega = inventoryService.createLocation(LocationRequest.builder()
            .name("Bodega")
            .build());

        ProductResponse detergente = catalogService.createProduct(CreateProductRequest.builder()
            .name("Detergente Polvo")
            .sku("DET-POL-1KG")
            .barCode("7807776665554")
            .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(detergente.getId())
            .locationId(bodega.getId())
            .quantity(2.0)
            .build(), "admin");

        // En conteo físico real hay 5 bolsas (faltaban registrar 3)
        ProductStockOverview overview = inventoryService.reconcileStock(StockAdjustmentRequest.builder()
            .productId(detergente.getId())
            .locationId(bodega.getId())
            .actualQuantity(5.0)
            .notes("Conteo físico de fin de semana")
            .build(), "admin");

        assertThat(overview.getTotalQuantity()).isEqualTo(5.0);

        var movements = inventoryService.getMovements(detergente.getId());
        assertThat(movements).isNotEmpty();
        assertThat(movements.get(0).getMovementType()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(movements.get(0).getQuantity()).isEqualTo(3.0); // +3 de ajuste
    }

    @Test
    @WithMockUser(username = "user", roles = {"MEMBER"})
    @DisplayName("Debe detectar lotes que vencen dentro de los próximos N días")
    void shouldDetectExpiringBatches() {
        LocationResponse heladera = inventoryService.createLocation(LocationRequest.builder()
            .name("Heladera")
            .build());

        ProductResponse crema = catalogService.createProduct(CreateProductRequest.builder()
            .name("Crema de Leche")
            .sku("CREM-200ML")
            .barCode("7804445556667")
            .build());

        // Lote 1: Vence en 2 días
        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(crema.getId())
            .locationId(heladera.getId())
            .quantity(2.0)
            .expirationDate(LocalDate.now().plusDays(2))
            .build(), "user");

        // Lote 2: Vence en 20 días
        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(crema.getId())
            .locationId(heladera.getId())
            .quantity(3.0)
            .expirationDate(LocalDate.now().plusDays(20))
            .build(), "user");

        List<StockBatchResponse> expiringSoon = inventoryService.getExpiringBatches(5);
        assertThat(expiringSoon).hasSize(1);
        assertThat(expiringSoon.get(0).getProductName()).isEqualTo("Crema de Leche");
        assertThat(expiringSoon.get(0).getDaysUntilExpiration()).isEqualTo(2);
    }
}
