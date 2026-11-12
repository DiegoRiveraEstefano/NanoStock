package cl.diego.NanoStock.ui;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.service.InventoryService;

@SpringBootTest
@ActiveProfiles("test")
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
                .andExpect(content().string(containsString("modal-waste")));
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
    @DisplayName("GET /inventory/movements debe renderizar la bitácora inmutable de auditoría")
    void shouldRenderMovementsLog() throws Exception {
        mockMvc.perform(get("/inventory/movements"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inventory/movements"))
                .andExpect(model().attributeExists("movements", "allProducts"))
                .andExpect(content().string(containsString("Bitácora de Auditoría de Movimientos")));
    }
}
