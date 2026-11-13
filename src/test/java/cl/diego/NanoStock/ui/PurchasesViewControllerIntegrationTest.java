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
import cl.diego.NanoStock.purchases.dto.AddShoppingItemRequest;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import cl.diego.NanoStock.purchases.service.StoreService;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "diego", roles = {"MEMBER"})
class PurchasesViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private ShoppingListService shoppingListService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /purchases/shopping-list debe renderizar la lista de compras y modal para agregar ítem")
    void shouldRenderShoppingList() throws Exception {
        mockMvc.perform(get("/purchases/shopping-list"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/purchases/shopping_list"))
                .andExpect(model().attributeExists("shoppingList", "allProducts"))
                .andExpect(content().string(containsString("Lista de Compras Activa")))
                .andExpect(content().string(containsString("modal-add-item")));
    }

    @Test
    @DisplayName("GET /purchases/supermarket debe renderizar la interfaz táctil con web component nano-shopping-item")
    void shouldRenderSupermarketMode() throws Exception {
        mockMvc.perform(get("/purchases/supermarket"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/purchases/supermarket_mode"))
                .andExpect(content().string(containsString("Modo \"En el Supermercado\"")))
                .andExpect(content().string(containsString("nano-shopping-item")));
    }

    @Test
    @DisplayName("POST /purchases/shopping-lists/items debe agregar un ítem manual y redirigir")
    void shouldAddShoppingListItem() throws Exception {
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Aceite de Oliva " + System.currentTimeMillis())
                .barCode("78044" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.LITER)
                .minStockThreshold(1.0)
                .targetStockQuantity(2.0)
                .isPerishable(false)
                .build());

        mockMvc.perform(post("/purchases/shopping-lists/items")
                .param("productId", prod.getId().toString())
                .param("quantity", "2.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchases/shopping-list"));
    }

    @Test
    @DisplayName("POST /purchases/shopping-lists/items/{id}/status debe actualizar estado mediante API")
    void shouldUpdateItemStatusAjax() throws Exception {
        var activeList = shoppingListService.getOrCreateActiveList();
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Azúcar Rubia " + System.currentTimeMillis())
                .barCode("78033" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .minStockThreshold(1.0)
                .targetStockQuantity(2.0)
                .isPerishable(false)
                .build());

        var item = shoppingListService.addItem(activeList.getId(), AddShoppingItemRequest.builder()
                .productId(prod.getId())
                .quantity(1.0)
                .build());

        mockMvc.perform(post("/purchases/shopping-lists/items/" + item.getId() + "/status")
                .param("status", "IN_CART"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("IN_CART")));
    }

    @Test
    @DisplayName("GET /purchases/receipts/new debe desplegar el constructor interactivo nano-receipt-builder")
    void shouldRenderNewReceiptForm() throws Exception {
        mockMvc.perform(get("/purchases/receipts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/purchases/receipt_form"))
                .andExpect(model().attributeExists("stores", "products", "locations", "productsJson", "locationsJson"))
                .andExpect(content().string(containsString("Registrar Boleta / Factura de Compra")))
                .andExpect(content().string(containsString("nano-receipt-builder")));
    }

    @Test
    @DisplayName("GET /purchases/stores y POST /purchases/stores deben listar y crear comercios")
    void shouldManageStores() throws Exception {
        mockMvc.perform(post("/purchases/stores")
                .param("name", "Supermercado Líder Express")
                .param("branchName", "Sucursal Central")
                .param("storeType", "SUPERMARKET")
                .param("address", "Av. Libertad 450"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchases/stores"));

        mockMvc.perform(get("/purchases/stores"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/purchases/stores"))
                .andExpect(content().string(containsString("Supermercado Líder Express")));
    }
}
