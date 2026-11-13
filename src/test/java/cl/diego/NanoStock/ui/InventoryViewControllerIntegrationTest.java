package cl.diego.NanoStock.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.dto.StockBatchResponse;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.service.InventoryService;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "diego", roles = {"MEMBER"})
class InventoryViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private InventoryService inventoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /inventory debe desplegar resumen de existencias, lotes FEFO y modales nano-modal")
    void shouldRenderInventoryOverview() throws Exception {
        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inventory/index"))
                .andExpect(model().attributeExists("stockOverviews", "locations", "allProducts", "expiringBatches"))
                .andExpect(content().string(containsString("Inventario Físico & Control FEFO")))
                .andExpect(content().string(containsString("modal-entry")))
                .andExpect(content().string(containsString("modal-consume")))
                .andExpect(content().string(containsString("modal-waste")))
                .andExpect(content().string(containsString("modal-reconcile")))
                .andExpect(content().string(containsString("modal-transfer")));
    }

    @Test
    @DisplayName("GET /inventory/locations debe desplegar la tabla de ubicaciones y modales de gestión")
    void shouldRenderLocationsTable() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Despensa Vista " + System.currentTimeMillis())
                .storageType(StorageType.AMBIENT)
                .description("Repisa superior")
                .build());

        mockMvc.perform(get("/inventory/locations"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inventory/locations"))
                .andExpect(model().attributeExists("locations", "storageTypes"))
                .andExpect(content().string(containsString("Ubicaciones de Almacenamiento")))
                .andExpect(content().string(containsString(loc.getName())))
                .andExpect(content().string(containsString("modal-add-location")))
                .andExpect(content().string(containsString("modal-edit-location")));
    }

    @Test
    @DisplayName("GET /inventory/locations?locationId=... debe mostrar lotes almacenados en la ubicación")
    void shouldRenderSelectedLocationBatches() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Congelador Detalle " + System.currentTimeMillis())
                .storageType(StorageType.FROZEN)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Arvejas Congeladas " + System.currentTimeMillis())
                .barCode("78088" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .minStockThreshold(1.0)
                .targetStockQuantity(2.0)
                .isPerishable(true)
                .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
                .productId(prod.getId())
                .locationId(loc.getId())
                .quantity(2.5)
                .expirationDate(LocalDate.now().plusMonths(6))
                .build(), "testuser");

        mockMvc.perform(get("/inventory/locations").param("locationId", loc.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inventory/locations"))
                .andExpect(model().attributeExists("selectedLocation", "locationBatches"))
                .andExpect(content().string(containsString("Stock Almacenado en: " + loc.getName())))
                .andExpect(content().string(containsString(prod.getName())));
    }

    @Test
    @DisplayName("POST /inventory/locations debe crear ubicación y redirigir a /inventory/locations")
    void shouldCreateLocationFromUi() throws Exception {
        String name = "Alacena Nueva " + System.currentTimeMillis();

        mockMvc.perform(post("/inventory/locations")
                .param("name", name)
                .param("storageType", "AMBIENT")
                .param("description", "Mueble junto al horno"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/locations"))
                .andExpect(flash().attribute("flashMessage", containsString(name)));

        assertThat(inventoryService.getAllLocations())
                .anyMatch(l -> l.getName().equals(name) && l.getStorageType() == StorageType.AMBIENT);
    }

    @Test
    @DisplayName("POST /inventory/locations/{id}/update debe actualizar ubicación")
    void shouldUpdateLocationFromUi() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Frío Original " + System.currentTimeMillis())
                .storageType(StorageType.REFRIGERATED)
                .build());

        String updatedName = "Freezer Actualizado " + System.currentTimeMillis();

        mockMvc.perform(post("/inventory/locations/" + loc.getId() + "/update")
                .param("name", updatedName)
                .param("storageType", "FROZEN")
                .param("description", "Cajón inferior"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/locations"))
                .andExpect(flash().attribute("flashMessage", containsString(updatedName)));

        LocationResponse reloaded = inventoryService.getLocationById(loc.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo(updatedName);
        assertThat(reloaded.getStorageType()).isEqualTo(StorageType.FROZEN);
        assertThat(reloaded.getDescription()).isEqualTo("Cajón inferior");
    }

    @Test
    @DisplayName("POST /inventory/locations/{id}/delete debe impedir borrar con stock activo y permitir si está vacía")
    void shouldDeleteEmptyLocationAndProtectNonEmptyLocation() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Repisa Temporal " + System.currentTimeMillis())
                .storageType(StorageType.AMBIENT)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Fideos " + System.currentTimeMillis())
                .barCode("78099" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .minStockThreshold(1.0)
                .targetStockQuantity(3.0)
                .isPerishable(false)
                .build());

        StockBatchResponse batch = inventoryService.addStockBatch(AddStockBatchRequest.builder()
                .productId(prod.getId())
                .locationId(loc.getId())
                .quantity(1.0)
                .build(), "testuser");

        // Intento con stock activo -> debe fallar con flashType is-danger
        mockMvc.perform(post("/inventory/locations/" + loc.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/locations"))
                .andExpect(flash().attribute("flashType", "is-danger"));

        assertThat(inventoryService.getLocationById(loc.getId())).isPresent();

        // Consumir stock del lote hasta 0
        inventoryService.consumeStock(cl.diego.NanoStock.inventory.dto.ConsumeStockRequest.builder()
                .productId(prod.getId())
                .batchId(batch.getId())
                .quantity(1.0)
                .build(), "testuser");

        // Ahora sí debe permitir eliminar
        mockMvc.perform(post("/inventory/locations/" + loc.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/locations"))
                .andExpect(flash().attribute("flashMessage", containsString("eliminada")));

        assertThat(inventoryService.getLocationById(loc.getId())).isEmpty();
    }

    @Test
    @DisplayName("POST /inventory/stock/entry debe registrar lote y redirigir a /inventory")
    void shouldAddStockBatch() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Despensa UI Test " + System.currentTimeMillis())
                .storageType(StorageType.AMBIENT)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Harina de Trigo " + System.currentTimeMillis())
                .barCode("78055" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .minStockThreshold(1.0)
                .targetStockQuantity(3.0)
                .isPerishable(true)
                .build());

        mockMvc.perform(post("/inventory/stock/entry")
                .param("productId", prod.getId().toString())
                .param("locationId", loc.getId().toString())
                .param("quantity", "4.0")
                .param("expirationDate", LocalDate.now().plusMonths(3).toString())
                .param("sealedStatus", "SEALED")
                .param("notes", "Ingreso test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"));
    }

    @Test
    @DisplayName("POST /inventory/stock/consume debe deducir existencias mediante FEFO")
    void shouldConsumeStockViaFefo() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Heladera UI Test " + System.currentTimeMillis())
                .storageType(StorageType.REFRIGERATED)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Yogurt Natural " + System.currentTimeMillis())
                .barCode("78066" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .minStockThreshold(2.0)
                .targetStockQuantity(6.0)
                .isPerishable(true)
                .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
                .productId(prod.getId())
                .locationId(loc.getId())
                .quantity(5.0)
                .expirationDate(LocalDate.now().plusDays(5))
                .build(), "testuser");

        mockMvc.perform(post("/inventory/stock/consume")
                .param("productId", prod.getId().toString())
                .param("quantity", "2.0")
                .param("notes", "Consumo desayuno"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"));
    }

    @Test
    @DisplayName("POST /inventory/stock/waste debe registrar merma y ajustar stock")
    void shouldRegisterWaste() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Frutero " + System.currentTimeMillis())
                .storageType(StorageType.AMBIENT)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Tomates " + System.currentTimeMillis())
                .barCode("78077" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .minStockThreshold(1.0)
                .targetStockQuantity(2.0)
                .isPerishable(true)
                .build());

        StockBatchResponse batch = inventoryService.addStockBatch(AddStockBatchRequest.builder()
                .productId(prod.getId())
                .locationId(loc.getId())
                .quantity(2.0)
                .expirationDate(LocalDate.now().minusDays(1))
                .build(), "testuser");

        mockMvc.perform(post("/inventory/stock/waste")
                .param("batchId", batch.getId().toString())
                .param("quantity", "1.0")
                .param("wasteReason", "Vencido")
                .param("notes", "Tomate en mal estado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"));
    }

    @Test
    @DisplayName("POST /inventory/stock/reconcile debe aplicar ajuste físico y registrar movimiento ADJUSTMENT")
    void shouldReconcileStockFromUi() throws Exception {
        LocationResponse loc = inventoryService.createLocation(LocationRequest.builder()
                .name("Despensa Conteo " + System.currentTimeMillis())
                .storageType(StorageType.AMBIENT)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Azúcar Rubia " + System.currentTimeMillis())
                .barCode("78091" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .minStockThreshold(1.0)
                .targetStockQuantity(3.0)
                .isPerishable(false)
                .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
                .productId(prod.getId())
                .locationId(loc.getId())
                .quantity(2.0)
                .build(), "testuser");

        mockMvc.perform(post("/inventory/stock/reconcile")
                .param("productId", prod.getId().toString())
                .param("locationId", loc.getId().toString())
                .param("actualQuantity", "3.5")
                .param("notes", "Conteo físico semanal"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"))
                .andExpect(flash().attribute("flashMessage", containsString("Ajuste físico")));

        assertThat(inventoryService.getProductStockOverview(prod.getId()).getTotalQuantity()).isEqualTo(3.5);
        assertThat(inventoryService.getMovements(prod.getId()))
                .anyMatch(m -> m.getMovementType() == MovementType.ADJUSTMENT && m.getQuantity() == 1.5);
    }

    @Test
    @DisplayName("POST /inventory/stock/transfer debe mover stock entre ubicaciones y registrar TRANSFER")
    void shouldTransferBatchFromUi() throws Exception {
        LocationResponse source = inventoryService.createLocation(LocationRequest.builder()
                .name("Congelador Origen " + System.currentTimeMillis())
                .storageType(StorageType.FROZEN)
                .build());

        LocationResponse target = inventoryService.createLocation(LocationRequest.builder()
                .name("Refrigerador Destino " + System.currentTimeMillis())
                .storageType(StorageType.REFRIGERATED)
                .build());

        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Pechuga de Pollo " + System.currentTimeMillis())
                .barCode("78092" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .minStockThreshold(1.0)
                .targetStockQuantity(4.0)
                .isPerishable(true)
                .build());

        StockBatchResponse sourceBatch = inventoryService.addStockBatch(AddStockBatchRequest.builder()
                .productId(prod.getId())
                .locationId(source.getId())
                .quantity(5.0)
                .expirationDate(LocalDate.now().plusMonths(2))
                .build(), "testuser");

        mockMvc.perform(post("/inventory/stock/transfer")
                .param("batchId", sourceBatch.getId().toString())
                .param("targetLocationId", target.getId().toString())
                .param("quantity", "2.0")
                .param("notes", "Descongelar para mañana"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"))
                .andExpect(flash().attribute("flashMessage", containsString("trasladado")));

        assertThat(inventoryService.getActiveBatchesByLocation(source.getId()))
                .singleElement()
                .satisfies(b -> assertThat(b.getQuantity()).isEqualTo(3.0));

        assertThat(inventoryService.getActiveBatchesByLocation(target.getId()))
                .singleElement()
                .satisfies(b -> {
                    assertThat(b.getProductId()).isEqualTo(prod.getId());
                    assertThat(b.getQuantity()).isEqualTo(2.0);
                });

        assertThat(inventoryService.getMovements(prod.getId()))
                .anyMatch(m -> m.getMovementType() == MovementType.TRANSFER && m.getQuantity() == 2.0);

        mockMvc.perform(get("/inventory/movements").param("productId", prod.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Traslado")));
    }

    @Test
    @DisplayName("GET /inventory/movements debe renderizar la bitácora inmutable de auditoría")
    void shouldRenderMovementsLog() throws Exception {
        mockMvc.perform(get("/inventory/movements"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inventory/movements"))
                .andExpect(model().attributeExists("movements", "allProducts"))
                .andExpect(content().string(containsString("Bitácora de Auditoría de Movimientos")));
    }
}
