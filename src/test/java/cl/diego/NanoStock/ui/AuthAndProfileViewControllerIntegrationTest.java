package cl.diego.NanoStock.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.iam.dto.UserRegistrationRequestDto;
import cl.diego.NanoStock.iam.model.Role;
import cl.diego.NanoStock.iam.repository.UserRepository;
import cl.diego.NanoStock.iam.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
class AuthAndProfileViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Rutas privadas deben redirigir a /login cuando no hay sesión autenticada")
    void privateRoutesShouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/inventory/locations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /login y GET /register deben renderizar las vistas de autenticación")
    void shouldRenderLoginAndRegisterPages() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"))
                .andExpect(content().string(containsString("Iniciar Sesión")))
                .andExpect(content().string(containsString("usernameOrEmail")));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/register"))
                .andExpect(content().string(containsString("Crear Cuenta")))
                .andExpect(content().string(containsString("confirmPassword")));
    }

    @Test
    @DisplayName("POST /register debe validar contraseñas y crear usuario correctamente")
    void shouldRegisterViaWebForm() throws Exception {
        // Contraseñas no coinciden -> re-renderiza con error
        mockMvc.perform(post("/register")
                .param("username", "maria")
                .param("email", "maria@familia.cl")
                .param("password", "clave123")
                .param("confirmPassword", "otra123"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/register"))
                .andExpect(content().string(containsString("Las contraseñas no coinciden")));

        // Registro exitoso -> redirige a /login?registered=true
        mockMvc.perform(post("/register")
                .param("username", "maria")
                .param("email", "maria@familia.cl")
                .param("password", "clave123")
                .param("confirmPassword", "clave123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        assertThat(userRepository.findByUsername("maria")).isPresent();
    }

    @Test
    @DisplayName("Form login y logout deben crear y cerrar sesión web y permitir acceso a /profile")
    void shouldLoginAccessProfileChangePasswordAndLogout() throws Exception {
        userService.registerUser(UserRegistrationRequestDto.builder()
                .username("diego_admin")
                .email("diego@nanostock.cl")
                .password("secreto123")
                .build());

        userService.registerUser(UserRegistrationRequestDto.builder()
                .username("invitado")
                .email("invitado@nanostock.cl")
                .password("secreto123")
                .build());

        MvcResult loginRes = mockMvc.perform(formLogin("/login")
                .user("usernameOrEmail", "diego@nanostock.cl")
                .password("secreto123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginRes.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // /profile muestra datos del usuario, gestión de miembros (por ser ADMIN) y navbar con username real
        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/profile/index"))
                .andExpect(content().string(containsString("diego_admin")))
                .andExpect(content().string(containsString("diego@nanostock.cl")))
                .andExpect(content().string(containsString("Miembros del Hogar")));

        // Cambio de contraseña
        mockMvc.perform(post("/profile/change-password").session(session)
                .param("currentPassword", "secreto123")
                .param("newPassword", "nuevaClave456")
                .param("confirmPassword", "nuevaClave456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("flashMessage", containsString("actualizada")));

        // Admin actualiza rol de otro miembro
        var member = userRepository.findByUsername("invitado").orElseThrow();
        mockMvc.perform(post("/profile/users/" + member.getId() + "/role").session(session)
                .param("role", Role.ROLE_GUEST.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        assertThat(userRepository.findById(member.getId()).orElseThrow().getRole()).isEqualTo(Role.ROLE_GUEST);

        // Logout invalida la sesión y redirige a /login?logout=true
        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
