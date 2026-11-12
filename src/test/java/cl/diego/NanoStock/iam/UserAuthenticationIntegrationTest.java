package cl.diego.NanoStock.iam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.iam.dto.ChangePasswordRequestDto;
import cl.diego.NanoStock.iam.dto.UserLoginRequestDto;
import cl.diego.NanoStock.iam.dto.UserRegistrationRequestDto;
import cl.diego.NanoStock.iam.dto.UserResponseDto;
import cl.diego.NanoStock.iam.model.Role;
import cl.diego.NanoStock.iam.repository.UserRepository;
import cl.diego.NanoStock.iam.service.UserService;

@SpringBootTest
class UserAuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("El primer usuario registrado debe recibir automáticamente el rol ADMIN")
    void firstUserShouldBeAdmin() throws Exception {
        UserRegistrationRequestDto dto = UserRegistrationRequestDto.builder()
            .username("papa_admin")
            .email("papa@familia.cl")
            .password("claveSecreta123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("papa_admin"))
            .andExpect(jsonPath("$.email").value("papa@familia.cl"))
            .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("El segundo usuario registrado debe recibir por defecto el rol MEMBER")
    void secondUserShouldBeMember() throws Exception {
        // Registro del primer usuario (Admin)
        userService.registerUser(UserRegistrationRequestDto.builder()
            .username("papa_admin")
            .email("papa@familia.cl")
            .password("clave123")
            .build());

        // Registro del segundo usuario
        UserRegistrationRequestDto hijoDto = UserRegistrationRequestDto.builder()
            .username("hijo_juan")
            .email("juan@familia.cl")
            .password("clave123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hijoDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("hijo_juan"))
            .andExpect(jsonPath("$.role").value("ROLE_MEMBER"));
    }

    @Test
    @DisplayName("No debe permitir registrar un usuario con username duplicado")
    void shouldRejectDuplicateUsername() throws Exception {
        userService.registerUser(UserRegistrationRequestDto.builder()
            .username("mama_admin")
            .email("mama@familia.cl")
            .password("clave123")
            .build());

        UserRegistrationRequestDto duplicate = UserRegistrationRequestDto.builder()
            .username("mama_admin")
            .email("otro_correo@familia.cl")
            .password("clave123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El nombre de usuario 'mama_admin' ya está en uso"));
    }

    @Test
    @DisplayName("Debe permitir login exitoso tanto por username como por email")
    void shouldLoginWithUsernameOrEmail() throws Exception {
        userService.registerUser(UserRegistrationRequestDto.builder()
            .username("diego")
            .email("diego@nanostock.cl")
            .password("miContrasenaSegura")
            .build());

        // 1. Login con username
        UserLoginRequestDto loginByUsername = UserLoginRequestDto.builder()
            .usernameOrEmail("diego")
            .password("miContrasenaSegura")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginByUsername)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("diego"));

        // 2. Login con email
        UserLoginRequestDto loginByEmail = UserLoginRequestDto.builder()
            .usernameOrEmail("diego@nanostock.cl")
            .password("miContrasenaSegura")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginByEmail)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("diego"));
    }

    @Test
    @DisplayName("Debe rechazar login con contraseña incorrecta")
    void shouldRejectWrongPassword() throws Exception {
        userService.registerUser(UserRegistrationRequestDto.builder()
            .username("diego")
            .email("diego@nanostock.cl")
            .password("correcta123")
            .build());

        UserLoginRequestDto loginWrong = UserLoginRequestDto.builder()
            .usernameOrEmail("diego")
            .password("incorrecta")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginWrong)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoint protegido debe rechazar peticiones anónimas")
    void protectedEndpointShouldRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "diego", roles = {"ADMIN"})
    @DisplayName("Endpoint /api/auth/me debe retornar datos del usuario autenticado")
    void meEndpointShouldReturnCurrentUser() throws Exception {
        userService.registerUser(UserRegistrationRequestDto.builder()
            .username("diego")
            .email("diego@nanostock.cl")
            .password("password123")
            .build());

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("diego"))
            .andExpect(jsonPath("$.email").value("diego@nanostock.cl"));
    }
}
