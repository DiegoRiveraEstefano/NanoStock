package cl.diego.NanoStock.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.demo.service.DemoDataSeeder;
import cl.diego.NanoStock.demo.service.DemoResetService;
import cl.diego.NanoStock.iam.dto.UserRegistrationRequestDto;
import cl.diego.NanoStock.iam.repository.UserRepository;
import cl.diego.NanoStock.iam.service.UserService;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.portfolio.service.CreatorProfileService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "nanostock.demo.enabled=true",
    "nanostock.demo.seed-on-startup=true",
    "nanostock.demo.reset-interval-days=7"
})
class DemoModeIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private DemoResetService demoResetService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    @Autowired
    private CreatorProfileService creatorProfileService;

    @Autowired
    private DatabaseCleanupHelper cleanupHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Scheduler scheduler;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        cleanupHelper.cleanAll();
        userRepository.deleteAll();
        jdbcTemplate.execute("DELETE FROM nanostock_demo_state");
    }

    @AfterEach
    void tearDown() {
        cleanupHelper.cleanAll();
        userRepository.deleteAll();
        jdbcTemplate.execute("DELETE FROM nanostock_demo_state");
    }

    @Test
    @DisplayName("El Job Quartz demoCleanupJob debe estar registrado y programado")
    void quartzDemoCleanupJobShouldBeScheduled() throws Exception {
        assertThat(scheduler.checkExists(new JobKey("demoCleanupJob", "nanostock-demo"))).isTrue();
    }

    @Test
    @DisplayName("Limpieza de 7 días debe borrar datos nuevos y preservar datos semilla, perfil de creador y cuentas ADMIN")
    void sevenDayCleanupShouldDeleteNewDataAndPreserveSeedAndAdmin() {
        // 1. Sembrar datos iniciales y asegurar perfil del creador
        creatorProfileService.getOrCreateDefaultProfile();
        demoDataSeeder.ensureSeedData();
        demoResetService.getStatus();

        // Crear un usuario ADMIN (propietario del portafolio) y un usuario visitante MEMBER
        var adminUser = userService.registerUser(UserRegistrationRequestDto.builder()
            .username("diego_owner")
            .email("owner@nanostock.cl")
            .password("owner123")
            .build());
        // Como ya existía el usuario demo (seed), forzamos ROLE_ADMIN para simular al propietario
        jdbcTemplate.update("UPDATE nanostock_iam_users SET role = 'ROLE_ADMIN' WHERE id = ?", adminUser.getId());

        userService.registerUser(UserRegistrationRequestDto.builder()
            .username("visitante_temp")
            .email("visitante@temp.cl")
            .password("temp123")
            .build());

        // Crear datos nuevos por parte de visitantes (is_seed = 0 por defecto)
        var newLoc = inventoryService.createLocation(LocationRequest.builder()
            .name("Bodega Temporal Visitante")
            .storageType(StorageType.AMBIENT)
            .build());

        var newProduct = catalogService.createProduct(CreateProductRequest.builder()
            .name("Galletas Temporales")
            .sku("TMP-GAL-999")
            .barCode("7809999999999")
            .unitOfMeasure(UnitOfMeasure.PACKAGE)
            .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(newProduct.getId())
            .locationId(newLoc.getId())
            .quantity(5.0)
            .expirationDate(LocalDate.now().plusDays(10))
            .build(), "demo");

        // 2. Antes de cumplirse los 7 días -> no debe limpiar nada
        boolean executedEarly = demoResetService.cleanupIfDue();
        assertThat(executedEarly).isFalse();
        assertThat(productRepository.findBySku("TMP-GAL-999")).isPresent();
        assertThat(locationRepository.findByNameIgnoreCase("Bodega Temporal Visitante")).isPresent();
        assertThat(userRepository.findByUsername("visitante_temp")).isPresent();

        // 3. Simular que han pasado 7 días (next_reset_at en el pasado)
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        jdbcTemplate.update("UPDATE nanostock_demo_state SET next_reset_at = ? WHERE id = 1", past.toString());

        boolean executedWhenDue = demoResetService.cleanupIfDue();
        assertThat(executedWhenDue).isTrue();

        // 4. Datos nuevos deben haber sido eliminados
        assertThat(productRepository.findBySku("TMP-GAL-999")).isEmpty();
        assertThat(locationRepository.findByNameIgnoreCase("Bodega Temporal Visitante")).isEmpty();
        assertThat(userRepository.findByUsername("visitante_temp")).isEmpty();

        // 5. Datos semilla, usuario demo, ADMIN y perfil creador deben permanecer intactos
        assertThat(userRepository.findByUsername("demo")).isPresent();
        assertThat(userRepository.findByUsername("diego_owner")).isPresent();
        assertThat(productRepository.findBySku("DEMO-LEC-001")).isPresent();
        assertThat(locationRepository.findByNameIgnoreCase("Despensa Principal")).isPresent();
        assertThat(stockBatchRepository.count()).isGreaterThanOrEqualTo(6);
        assertThat(creatorProfileService.getOrCreateDefaultProfile().getFullName()).isEqualTo("Diego Rivera");

        // 6. Próxima limpieza quedó reprogramada para dentro de ~7 días
        var updatedStatus = demoResetService.getStatus();
        assertThat(updatedStatus.getLastCleanupDeletedRows()).isGreaterThanOrEqualTo(4);
        assertThat(updatedStatus.getNextResetAt()).isAfter(LocalDateTime.now().plusDays(6));
    }

    @Test
    @DisplayName("Login demo de 1 clic y endpoint público /api/demo/status deben funcionar en modo demo")
    void oneClickDemoLoginAndPublicStatusShouldWork() throws Exception {
        demoDataSeeder.ensureSeedData();
        demoResetService.getStatus();

        mockMvc.perform(get("/api/demo/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.resetIntervalDays").value(7))
            .andExpect(jsonPath("$.demoUsername").value("demo"));

        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Modo Demo Portafolio")))
            .andExpect(content().string(containsString("Entrar como Usuario Demo (1 clic)")));

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                .user("usernameOrEmail", "demo")
                .password("demo1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"))
            .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/dashboard").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Modo Demo Portafolio:")));

        // El usuario demo (ROLE_MEMBER) no puede forzar un reset manual
        mockMvc.perform(post("/api/demo/reset").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ROLE_ADMIN debe poder ejecutar POST /api/demo/reset")
    void adminShouldTriggerManualDemoReset() throws Exception {
        demoDataSeeder.ensureSeedData();

        mockMvc.perform(post("/api/demo/reset"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.resetIntervalDays").value(7));
    }
}
