package cl.diego.NanoStock.ui;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

@SpringBootTest
@ActiveProfiles("test")
class CatalogViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductCatalogService catalogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /catalog debe retornar tabla de productos y escáner")
    void shouldRenderCatalogList() throws Exception {
        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/catalog/index"))
                .andExpect(model().attributeExists("products", "categories"))
                .andExpect(content().string(containsString("Catálogo Maestro")))
                .andExpect(content().string(containsString("nano-barcode-scanner")));
    }

    @Test
    @DisplayName("GET /catalog/new debe retornar formulario de nuevo producto con web component")
    void shouldRenderNewProductForm() throws Exception {
        mockMvc.perform(get("/catalog/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/catalog/product_form"))
                .andExpect(model().attributeExists("form", "categories", "brands", "unitsOfMeasure"))
                .andExpect(content().string(containsString("Registrar Nuevo Producto")))
                .andExpect(content().string(containsString("nano-barcode-scanner")));
    }

    @Test
    @DisplayName("POST /catalog debe crear producto y redirigir al detalle")
    void shouldCreateProductViaForm() throws Exception {
        String testBarcode = "78099" + System.currentTimeMillis();

        mockMvc.perform(post("/catalog")
                .param("name", "Arroz Grado 1")
                .param("barCode", testBarcode)
                .param("description", "Bolsa de 1kg")
                .param("unitOfMeasure", "KILOGRAM")
                .param("minStockThreshold", "2.0")
                .param("targetStockQuantity", "5.0")
                .param("isPerishable", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/catalog/*"));
    }

    @Test
    @DisplayName("GET /catalog/{id} debe renderizar ficha de producto y existencias")
    void shouldRenderProductDetail() throws Exception {
        String uniqueBarcode = "78088" + System.currentTimeMillis();
        ProductResponse created = catalogService.createProduct(CreateProductRequest.builder()
                .name("Café Espresso Molido")
                .barCode(uniqueBarcode)
                .unitOfMeasure(UnitOfMeasure.GRAM)
                .minStockThreshold(250.0)
                .targetStockQuantity(1000.0)
                .isPerishable(true)
                .build());

        mockMvc.perform(get("/catalog/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/catalog/product_detail"))
                .andExpect(model().attributeExists("product", "stockOverview"))
                .andExpect(content().string(containsString("Café Espresso Molido")))
                .andExpect(content().string(containsString(uniqueBarcode)));
    }
}
