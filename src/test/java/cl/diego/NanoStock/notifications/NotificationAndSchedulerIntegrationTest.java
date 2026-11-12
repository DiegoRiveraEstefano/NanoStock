package cl.diego.NanoStock.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.notifications.dto.NotificationResponse;
import cl.diego.NanoStock.notifications.event.StockExpiringSoonEvent;
import cl.diego.NanoStock.notifications.event.StockReplenishmentNeededEvent;
import cl.diego.NanoStock.notifications.model.NotificationSeverity;
import cl.diego.NanoStock.notifications.model.NotificationType;
import cl.diego.NanoStock.notifications.repository.NotificationRepository;
import cl.diego.NanoStock.notifications.service.NotificationService;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@RecordApplicationEvents
class NotificationAndSchedulerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private ShoppingListService shoppingListService;

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe auditar caducidad de lotes, crear notificaciones críticas y publicar StockExpiringSoonEvent")
    void shouldAuditExpiringBatchesAndGenerateCriticalNotifications() {
        LocationResponse refri = inventoryService.createLocation(LocationRequest.builder()
            .name("Refrigerador")
            .storageType(StorageType.REFRIGERATED)
            .build());

        ProductResponse yogurt = catalogService.createProduct(CreateProductRequest.builder()
            .name("Yogurt Griego 150g")
            .sku("YOG-GRI-150")
            .barCode("7809988112255")
            .unitOfMeasure(UnitOfMeasure.UNIT)
            .build());

        // Lote que vence en 2 días
        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(yogurt.getId())
            .locationId(refri.getId())
            .quantity(3.0)
            .expirationDate(LocalDate.now().plusDays(2))
            .batchCode("LOTE-YOG-002")
            .build(), "admin");

        // Ejecutar chequeo de caducidad para los próximos 7 días
        int detected = notificationService.runExpirationCheck(7);
        assertThat(detected).isEqualTo(1);

        // Verificar notificación creada
        List<NotificationResponse> unread = notificationService.getUnreadNotifications();
        assertThat(unread).hasSize(1);
        NotificationResponse notification = unread.get(0);
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.EXPIRATION_ALERT);
        assertThat(notification.getSeverity()).isEqualTo(NotificationSeverity.CRITICAL);
        assertThat(notification.getTitle()).contains("Yogurt Griego 150g").contains("vence en 2 días");
        assertThat(notification.getMessage()).contains("caduca el").contains("Refrigerador");

        // Verificar evento publicado
        long eventsCount = applicationEvents.stream(StockExpiringSoonEvent.class)
            .filter(e -> e.productId().equals(yogurt.getId()) && e.daysRemaining() == 2)
            .count();
        assertThat(eventsCount).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe auditar stock crítico, sugerir compras, generar alerta y emitir StockReplenishmentNeededEvent")
    void shouldAuditReplenishmentAndGenerateLowStockAlert() {
        catalogService.createProduct(CreateProductRequest.builder()
            .name("Huevos Extra 12un")
            .sku("HUE-EXT-12")
            .barCode("7805566112233")
            .minStockThreshold(2.0)
            .targetStockQuantity(4.0)
            .build());

        // Stock disponible es 0 (crítico)
        int suggestedCount = notificationService.runReplenishmentCheck();
        assertThat(suggestedCount).isGreaterThanOrEqualTo(1);

        // Validar notificación de reposición
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications();
        assertThat(notifications).anyMatch(n ->
            n.getNotificationType() == NotificationType.STOCK_LOW_ALERT &&
            n.getTitle().contains("Reposición sugerida"));

        // Validar evento emitido
        long eventsCount = applicationEvents.stream(StockReplenishmentNeededEvent.class)
            .filter(e -> e.productName().equals("Huevos Extra 12un") && e.currentStock() == 0.0)
            .count();
        assertThat(eventsCount).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe consultar notificaciones, contar no leídas y marcarlas como leídas vía API REST")
    void shouldManageNotificationsViaRest() throws Exception {
        NotificationResponse n1 = notificationService.createNotification(
            "Alerta 1", "Mensaje 1", NotificationType.SYSTEM, NotificationSeverity.INFO, null);
        NotificationResponse n2 = notificationService.createNotification(
            "Alerta 2", "Mensaje 2", NotificationType.EXPIRATION_ALERT, NotificationSeverity.WARNING, null);

        // 1. Contador de no leídas
        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(2));

        // 2. Marcar una como leída
        mockMvc.perform(patch("/api/notifications/" + n1.getId() + "/read"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(n1.getId()))
            .andExpect(jsonPath("$.read").value(true));

        // 3. Verificar que queda 1 no leída
        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(1));

        // 4. Marcar todas como leídas
        mockMvc.perform(post("/api/notifications/mark-all-read"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(0));
    }
}
