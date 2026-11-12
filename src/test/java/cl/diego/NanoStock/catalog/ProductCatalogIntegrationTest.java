package cl.diego.NanoStock.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import cl.diego.NanoStock.catalog.dto.BrandRequest;
import cl.diego.NanoStock.catalog.dto.BrandResponse;
import cl.diego.NanoStock.catalog.dto.CategoryRequest;
import cl.diego.NanoStock.catalog.dto.CategoryResponse;
import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.dto.UpdateProductRequest;
import cl.diego.NanoStock.catalog.event.ProductCreatedEvent;
import cl.diego.NanoStock.catalog.event.ProductUpdatedEvent;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.BrandRepository;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@RecordApplicationEvents
class ProductCatalogIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private DatabaseCleanupHelper cleanupHelper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        cleanupHelper.cleanAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe crear categorías jerárquicas (padre e hija)")
    void shouldCreateHierarchicalCategories() {
        CategoryResponse alimentos = catalogService.createCategory(CategoryRequest.builder()
            .name("Alimentos")
            .description("Abarrotes y alimentos del hogar")
            .build());

        CategoryResponse lacteos = catalogService.createCategory(CategoryRequest.builder()
            .name("Lácteos")
            .description("Leches, quesos y yogures")
            .parentId(alimentos.getId())
            .build());

        assertThat(lacteos.getId()).isNotNull();
        assertThat(lacteos.getParentId()).isEqualTo(alimentos.getId());
        assertThat(lacteos.getParentName()).isEqualTo("Alimentos");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe crear una marca y recuperarla")
    void shouldCreateAndRetrieveBrand() {
        BrandResponse colun = catalogService.createBrand(BrandRequest.builder()
            .name("Colun")
            .build());

        assertThat(colun.getId()).isNotNull();
        assertThat(colun.getName()).isEqualTo("Colun");

        List<BrandResponse> brands = catalogService.getAllBrands();
        assertThat(brands).hasSize(1);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe registrar un producto maestro con categoría, marca y publicar ProductCreatedEvent")
    void shouldRegisterProductAndPublishEvent() {
        CategoryResponse despensa = catalogService.createCategory(CategoryRequest.builder()
            .name("Despensa")
            .build());

        BrandResponse colun = catalogService.createBrand(BrandRequest.builder()
            .name("Colun")
            .build());

        CreateProductRequest request = CreateProductRequest.builder()
            .name("Leche Semidescremada 1L")
            .description("Caja Tetra Brik 1 Litro")
            .sku("LEC-COL-SEMI-1L")
            .barCode("7801234567899")
            .categoryId(despensa.getId())
            .brandId(colun.getId())
            .unitOfMeasure(UnitOfMeasure.LITER)
            .minStockThreshold(2.0)
            .targetStockQuantity(6.0)
            .isPerishable(true)
            .build();

        ProductResponse response = catalogService.createProduct(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCategoryName()).isEqualTo("Despensa");
        assertThat(response.getBrandName()).isEqualTo("Colun");
        assertThat(response.getUnitOfMeasure()).isEqualTo(UnitOfMeasure.LITER);

        // Verificar que se publicó el evento de dominio para Spring Modulith
        long eventsCount = applicationEvents.stream(ProductCreatedEvent.class)
            .filter(e -> e.barCode().equals("7801234567899"))
            .count();
        assertThat(eventsCount).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe auto-generar SKU si no se proporciona explícitamente")
    void shouldAutoGenerateSkuWhenOmitted() {
        CreateProductRequest request = CreateProductRequest.builder()
            .name("Arroz Grado 1 1kg")
            .barCode("7809998887771")
            .build();

        ProductResponse response = catalogService.createProduct(request);

        assertThat(response.getSku()).isNotBlank();
        assertThat(response.getSku()).startsWith("ARR-");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe rechazar productos con código de barras duplicado")
    void shouldRejectDuplicateBarCode() {
        CreateProductRequest p1 = CreateProductRequest.builder()
            .name("Aceite Vegetal 900ml")
            .barCode("7805554443332")
            .build();
        catalogService.createProduct(p1);

        CreateProductRequest p2 = CreateProductRequest.builder()
            .name("Aceite de Maravilla 900ml")
            .barCode("7805554443332")
            .build();

        assertThatThrownBy(() -> catalogService.createProduct(p2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe un producto con el código de barras");
    }

    @Test
    @WithMockUser(username = "user", roles = {"MEMBER"})
    @DisplayName("Debe buscar producto por código de barras vía API REST para escaneo rápido")
    void shouldLookupByBarCodeViaRest() throws Exception {
        catalogService.createProduct(CreateProductRequest.builder()
            .name("Fideos Spaghetti N5")
            .sku("FID-SPA-05")
            .barCode("7801112223334")
            .unitOfMeasure(UnitOfMeasure.PACKAGE)
            .build());

        mockMvc.perform(get("/api/catalog/products/barcode/7801112223334"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Fideos Spaghetti N5"))
            .andExpect(jsonPath("$.sku").value("FID-SPA-05"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe actualizar un producto y emitir ProductUpdatedEvent")
    void shouldUpdateProductAndPublishEvent() {
        ProductResponse initial = catalogService.createProduct(CreateProductRequest.builder()
            .name("Detergente Ropa 3L")
            .sku("DET-ROP-3L")
            .barCode("7801212121212")
            .minStockThreshold(1.0)
            .build());

        UpdateProductRequest updateReq = UpdateProductRequest.builder()
            .name("Detergente Líquido Ropa 3L Matic")
            .description("Fórmula concentrada")
            .sku("DET-ROP-3L")
            .barCode("7801212121212")
            .minStockThreshold(2.0)
            .targetStockQuantity(4.0)
            .unitOfMeasure(UnitOfMeasure.LITER)
            .isPerishable(false)
            .build();

        ProductResponse updated = catalogService.updateProduct(initial.getId(), updateReq);

        assertThat(updated.getName()).isEqualTo("Detergente Líquido Ropa 3L Matic");
        assertThat(updated.getMinStockThreshold()).isEqualTo(2.0);
        assertThat(updated.isPerishable()).isFalse();

        long updateEvents = applicationEvents.stream(ProductUpdatedEvent.class)
            .filter(e -> e.productId().equals(initial.getId()))
            .count();
        assertThat(updateEvents).isEqualTo(1);
    }
}
