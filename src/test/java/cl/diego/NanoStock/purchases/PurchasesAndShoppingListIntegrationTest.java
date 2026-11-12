package cl.diego.NanoStock.purchases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;
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

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.purchases.dto.AddShoppingItemRequest;
import cl.diego.NanoStock.purchases.dto.ReceiptItemRequest;
import cl.diego.NanoStock.purchases.dto.ReceiptResponse;
import cl.diego.NanoStock.purchases.dto.RecordReceiptRequest;
import cl.diego.NanoStock.purchases.dto.ShoppingListItemResponse;
import cl.diego.NanoStock.purchases.dto.ShoppingListResponse;
import cl.diego.NanoStock.purchases.dto.StoreRequest;
import cl.diego.NanoStock.purchases.dto.StoreResponse;
import cl.diego.NanoStock.purchases.dto.UpdateShoppingItemStatusRequest;
import cl.diego.NanoStock.purchases.event.PurchaseCompletedEvent;
import cl.diego.NanoStock.purchases.model.PaymentMethod;
import cl.diego.NanoStock.purchases.model.ShoppingItemStatus;
import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.model.ShoppingListStatus;
import cl.diego.NanoStock.purchases.model.StoreType;
import cl.diego.NanoStock.purchases.repository.ReceiptItemRepository;
import cl.diego.NanoStock.purchases.repository.ReceiptRepository;
import cl.diego.NanoStock.purchases.repository.ShoppingListItemRepository;
import cl.diego.NanoStock.purchases.repository.ShoppingListRepository;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import cl.diego.NanoStock.purchases.service.PurchaseService;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import cl.diego.NanoStock.purchases.service.StoreService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@RecordApplicationEvents
class PurchasesAndShoppingListIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StoreService storeService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

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
    @DisplayName("Debe registrar boleta de compra, calcular totales y crear automáticamente lotes en inventario vía eventos")
    void shouldRecordReceiptAndAutoCreateInventoryStock() {
        StoreResponse store = storeService.createStore(StoreRequest.builder()
            .name("Lider Express")
            .branchName("Providencia")
            .storeType(StoreType.SUPERMARKET)
            .build());

        ProductResponse cafe = catalogService.createProduct(CreateProductRequest.builder()
            .name("Café Grano 500g")
            .sku("CAF-GRA-500G")
            .barCode("7809988112233")
            .unitOfMeasure(UnitOfMeasure.PACKAGE)
            .minStockThreshold(1.0)
            .build());

        ProductResponse azucar = catalogService.createProduct(CreateProductRequest.builder()
            .name("Azúcar Blanca 1kg")
            .sku("AZU-BLA-1KG")
            .barCode("7809988112244")
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .minStockThreshold(1.0)
            .build());

        RecordReceiptRequest receiptReq = RecordReceiptRequest.builder()
            .storeId(store.getId())
            .purchaseDate(LocalDate.now())
            .paymentMethod(PaymentMethod.DEBIT_CARD)
            .notes("Compras quincenales de despensa")
            .items(List.of(
                ReceiptItemRequest.builder()
                    .productId(cafe.getId())
                    .quantity(2.0)
                    .unitPrice(new BigDecimal("7990.00"))
                    .discountAmount(new BigDecimal("1000.00")) // Total línea = (7990*2) - 1000 = 14980
                    .expirationDate(LocalDate.now().plusMonths(6))
                    .build(),
                ReceiptItemRequest.builder()
                    .productId(azucar.getId())
                    .quantity(3.0)
                    .unitPrice(new BigDecimal("1200.00"))
                    .discountAmount(BigDecimal.ZERO) // Total línea = 3600
                    .expirationDate(LocalDate.now().plusYears(1))
                    .build()
            ))
            .build();

        ReceiptResponse response = purchaseService.recordReceipt(receiptReq, "diego");

        // Verificación de boleta
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStoreName()).isEqualTo("Lider Express");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("18580.00"));
        assertThat(response.getItems()).hasSize(2);

        // Verificación del evento PurchaseCompletedEvent
        long purchaseEvents = applicationEvents.stream(PurchaseCompletedEvent.class)
            .filter(e -> e.receiptId().equals(response.getId()))
            .count();
        assertThat(purchaseEvents).isEqualTo(1);

        // Verificación de la integración desacoplada con Inventario (PurchaseEventListener):
        // El stock de Café debe ser 2.0 y el de Azúcar debe ser 3.0
        Double stockCafe = stockBatchRepository.sumQuantityByProductId(cafe.getId());
        Double stockAzucar = stockBatchRepository.sumQuantityByProductId(azucar.getId());

        assertThat(stockCafe).isEqualTo(2.0);
        assertThat(stockAzucar).isEqualTo(3.0);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe generar lista de compras sugerida basada en productos con stock crítico/mínimo")
    void shouldGenerateSuggestedShoppingListFromCriticalStock() {
        ProductResponse aceite = catalogService.createProduct(CreateProductRequest.builder()
            .name("Aceite Oliva 500ml")
            .sku("ACE-OLI-500")
            .barCode("7807766554433")
            .minStockThreshold(2.0)
            .targetStockQuantity(4.0)
            .build());

        // El producto aceite tiene 0.0 de stock disponible (crítico, <= 2.0)
        ShoppingList activeList = shoppingListService.getOrCreateActiveList();

        ShoppingListResponse listResponse = shoppingListService.generateSuggestedItems(activeList.getId());

        assertThat(listResponse.getItems()).hasSize(1);
        ShoppingListItemResponse item = listResponse.getItems().get(0);
        assertThat(item.getProductName()).isEqualTo("Aceite Oliva 500ml");
        assertThat(item.isAutoSuggested()).isTrue();
        assertThat(item.getSuggestedQuantity()).isEqualTo(4.0);
        assertThat(item.getItemStatus()).isEqualTo(ShoppingItemStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Modo supermercado: Debe actualizar el estado de los ítems en góndola (IN_CART, BOUGHT)")
    void shouldUpdateItemStatusInSupermarketMode() {
        ProductResponse galletas = catalogService.createProduct(CreateProductRequest.builder()
            .name("Galletas de Avena")
            .sku("GAL-AVE-01")
            .barCode("7803322110099")
            .build());

        ShoppingList activeList = shoppingListService.getOrCreateActiveList();

        ShoppingListItemResponse item = shoppingListService.addItem(activeList.getId(), AddShoppingItemRequest.builder()
            .productId(galletas.getId())
            .quantity(3.0)
            .build());

        assertThat(item.getItemStatus()).isEqualTo(ShoppingItemStatus.PENDING);

        // 1. Usuario echa el producto al carro físico
        ShoppingListItemResponse inCart = shoppingListService.updateItemStatus(item.getId(), UpdateShoppingItemStatusRequest.builder()
            .itemStatus(ShoppingItemStatus.IN_CART)
            .build());
        assertThat(inCart.getItemStatus()).isEqualTo(ShoppingItemStatus.IN_CART);

        // 2. Al pagar en caja, se marca como comprado
        ShoppingListItemResponse bought = shoppingListService.updateItemStatus(item.getId(), UpdateShoppingItemStatusRequest.builder()
            .itemStatus(ShoppingItemStatus.BOUGHT)
            .actualQuantity(3.0)
            .build());
        assertThat(bought.getItemStatus()).isEqualTo(ShoppingItemStatus.BOUGHT);

        // 3. Finalizar la lista de compras
        ShoppingListResponse completed = shoppingListService.completeList(activeList.getId());
        assertThat(completed.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
    }
}
