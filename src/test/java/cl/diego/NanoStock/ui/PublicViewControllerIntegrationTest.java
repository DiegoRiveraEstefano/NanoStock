package cl.diego.NanoStock.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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

import cl.diego.NanoStock.portfolio.repository.CreatorProfileRepository;

@SpringBootTest
@ActiveProfiles("test")
class PublicViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CreatorProfileRepository creatorProfileRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        creatorProfileRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /, /about y /creator deben ser accesibles de forma anónima")
    void publicPagesShouldRenderAnonymously() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/portfolio/landing"))
                .andExpect(content().string(containsString("Controla tu despensa")))
                .andExpect(content().string(containsString("Iniciar Sesión")))
                .andExpect(content().string(containsString("Crear Cuenta")));

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/portfolio/about"))
                .andExpect(content().string(containsString("Spring Modulith")))
                .andExpect(content().string(containsString("FEFO")));

        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/portfolio/creator"))
                .andExpect(content().string(containsString("Diego Rivera")))
                .andExpect(content().string(containsString("https://github.com/DiegoRiveraEstefano")))
                .andExpect(content().string(not(containsString("Editar Mi Perfil y Redes"))));
    }

    @Test
    @DisplayName("POST /creator debe redirigir a /login si es anónimo")
    void postCreatorShouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(post("/creator")
                .param("fullName", "Intruso")
                .param("headline", "Hacker")
                .param("bio", "Bio falsa"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "miembro", roles = {"MEMBER"})
    @DisplayName("POST /creator debe retornar 403 Forbidden para usuarios con ROLE_MEMBER")
    void postCreatorShouldReturnForbiddenForMember() throws Exception {
        mockMvc.perform(post("/creator")
                .param("fullName", "Miembro")
                .param("headline", "Member")
                .param("bio", "No autorizado"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "diego", roles = {"ADMIN"})
    @DisplayName("ROLE_ADMIN debe poder actualizar perfil y redes del creador y visualizarlas en /creator")
    void adminShouldUpdateCreatorProfileAndSocialLinks() throws Exception {
        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar Mi Perfil y Redes")));

        mockMvc.perform(post("/creator")
                .param("fullName", "Diego Rivera Estefano")
                .param("headline", "Senior Backend Engineer")
                .param("bio", "Construyendo sistemas resilientes con Java y Spring Boot.")
                .param("location", "Valparaíso, Chile")
                .param("techStack", "Java 21, Spring Boot 4, Docker")
                .param("githubUrl", "https://github.com/DiegoRiveraEstefano")
                .param("linkedinUrl", "https://linkedin.com/in/diegorivera")
                .param("websiteUrl", "https://diegorivera.dev")
                .param("xUrl", "https://x.com/diegodev")
                .param("contactEmail", "diego@nanostock.cl"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creator"))
                .andExpect(flash().attribute("flashType", "is-success"));

        var saved = creatorProfileRepository.findFirstByOrderByIdAsc().orElseThrow();
        assertThat(saved.getFullName()).isEqualTo("Diego Rivera Estefano");
        assertThat(saved.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/diegorivera");
        assertThat(saved.getXUrl()).isEqualTo("https://x.com/diegodev");

        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Diego Rivera Estefano")))
                .andExpect(content().string(containsString("Senior Backend Engineer")))
                .andExpect(content().string(containsString("Valparaíso, Chile")))
                .andExpect(content().string(containsString("https://linkedin.com/in/diegorivera")))
                .andExpect(content().string(containsString("https://diegorivera.dev")))
                .andExpect(content().string(containsString("https://x.com/diegodev")))
                .andExpect(content().string(containsString("diego@nanostock.cl")));
    }
}
