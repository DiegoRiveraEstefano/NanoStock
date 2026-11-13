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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "diego", roles = {"MEMBER"})
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
    @DisplayName("GET /catalog debe retornar tabla de productos, escáner y modal de categorías/marcas")
    void shouldRenderCatalogList() throws Exception {
        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/catalog/index"))
                .andExpect(model().attributeExists("products", "categories", "brands"))
                .andExpect(content().string(containsString("Catálogo Maestro")))
                .andExpect(content().string(containsString("nano-barcode-scanner")))
                .andExpect(content().string(containsString("modal-catalog-meta")));
    }

    @Test
    @DisplayName("GET /catalog/new debe retornar formulario de nuevo producto con modales rápidos de categoría y marca")
    void shouldRenderNewProductForm() throws Exception {
        mockMvc.perform(get("/catalog/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/catalog/product_form"))
                .andExpect(model().attributeExists("form", "categories", "brands", "unitsOfMeasure"))
                .andExpect(content().string(containsString("Registrar Nuevo Producto")))
                .andExpect(content().string(containsString("nano-barcode-scanner")))
                .andExpect(content().string(containsString("modal-quick-category")))
                .andExpect(content().string(containsString("modal-quick-brand")));
    }

    @Test
    @DisplayName("POST /catalog/categories debe crear categoría y respetar returnTo dentro de /catalog")
    void shouldCreateCategoryFromUi() throws Exception {
        String catName = "Condimentos Test " + System.currentTimeMillis();

        mockMvc.perform(post("/catalog/categories")
                .param("name", catName)
                .param("description", "Especias secas")
                .param("returnTo", "/catalog/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/new"))
                .andExpect(flash().attribute("flashMessage", containsString(catName)));

        assertThat(catalogService.getAllCategories())
                .anyMatch(c -> c.getName().equals(catName));
    }

    @Test
    @DisplayName("POST /catalog/brands debe crear marca y sanitizar returnTo externo")
    void shouldCreateBrandFromUi() throws Exception {
        String brandName = "Marca Casera " + System.currentTimeMillis();

        mockMvc.perform(post("/catalog/brands")
                .param("name", brandName)
                .param("returnTo", "https://evil.example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog"))
                .andExpect(flash().attribute("flashMessage", containsString(brandName)));

        assertThat(catalogService.getAllBrands())
                .anyMatch(b -> b.getName().equals(brandName));
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
