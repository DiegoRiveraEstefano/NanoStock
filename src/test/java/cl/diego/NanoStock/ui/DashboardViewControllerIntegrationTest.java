package cl.diego.NanoStock.ui;

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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "diego", roles = {"MEMBER"})
class DashboardViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /dashboard debe retornar vista JTE con Bulma CSS y Web Components cargados")
    void shouldRenderDashboardWithJteAndBulma() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/dashboard/index"))
                .andExpect(model().attributeExists("totalProducts", "criticalStockCount", "expiringSoonCount", "shoppingListCount", "unreadNotifications"))
                .andExpect(content().string(containsString("NanoStock")))
                .andExpect(content().string(containsString("bulma.min.css")))
                .andExpect(content().string(containsString("app.css")))
                .andExpect(content().string(containsString("app.js")))
                .andExpect(content().string(containsString("nano-modal.js")));
    }

    @Test
    @DisplayName("GET / debe renderizar la Landing Page pública incluso con sesión iniciada")
    void shouldRenderPublicLandingOnRootPath() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/portfolio/landing"))
                .andExpect(content().string(containsString("Ir a mi Dashboard")));
    }
}
