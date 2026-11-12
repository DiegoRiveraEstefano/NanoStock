package cl.diego.NanoStock.ui;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.notifications.model.NotificationSeverity;
import cl.diego.NanoStock.notifications.model.NotificationType;
import cl.diego.NanoStock.notifications.service.NotificationService;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsAndDataViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private NotificationService notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /analytics debe renderizar tablero de finanzas y autonomía")
    void shouldRenderAnalytics() throws Exception {
        mockMvc.perform(get("/analytics"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/analytics/index"))
                .andExpect(model().attributeExists("autonomies", "wasteReport"))
                .andExpect(content().string(containsString("Analíticas & Finanzas del Hogar")))
                .andExpect(content().string(containsString("Pérdida por Desperdicio")));
    }

    @Test
    @DisplayName("GET /notifications debe mostrar el centro de alertas familiares")
    void shouldRenderNotifications() throws Exception {
        notificationService.createNotification(
                "Alerta de Vencimiento Test",
                "Leche vence mañana",
                NotificationType.EXPIRATION_ALERT,
                NotificationSeverity.WARNING,
                100L);

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/notifications/index"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(content().string(containsString("Centro de Alertas & Notificaciones")))
                .andExpect(content().string(containsString("Alerta de Vencimiento Test")));
    }

    @Test
    @DisplayName("POST /notifications/mark-all-read debe marcar leídas y redirigir")
    void shouldMarkAllNotificationsRead() throws Exception {
        mockMvc.perform(post("/notifications/mark-all-read"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    @DisplayName("GET /data/import y POST /data/import deben permitir carga masiva con reporte")
    void shouldHandleDataImport() throws Exception {
        mockMvc.perform(get("/data/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/data/import"))
                .andExpect(content().string(containsString("Importación Masiva de Catálogo e Inventario")));

        String csvData = "name,barCode,unitOfMeasure,minStockThreshold\nGalletas Avena,78099881122,PACKAGE,2.0";
        MockMultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv", csvData.getBytes());

        mockMvc.perform(multipart("/data/import")
                .file(file)
                .param("targetType", "PRODUCTS")
                .param("conflictStrategy", "UPDATE")
                .param("dryRun", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/data/import"))
                .andExpect(model().attributeExists("importResult"))
                .andExpect(content().string(containsString("Resultado de la Operación")));
    }

    @Test
    @DisplayName("GET /data/export debe mostrar las opciones de descarga de datos")
    void shouldRenderExportPage() throws Exception {
        mockMvc.perform(get("/data/export"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/data/export"))
                .andExpect(content().string(containsString("Exportación de Datos del Hogar")))
                .andExpect(content().string(containsString("Descargar Excel (.xlsx)")));
    }
}
