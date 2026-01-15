package AplikacjePrzemyslowe.DatApp;

import AplikacjePrzemyslowe.DatApp.controller.AuthController;
import AplikacjePrzemyslowe.DatApp.dto.request.RegisterRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.UserResponse;
import AplikacjePrzemyslowe.DatApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@DisplayName("REST API Tests - Authentication")
class RestAuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private RegisterRequest registerRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setup() {
        registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .gender("MALE")
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .gender("MALE")
                .age(29)
                .city("Warsaw")
                .build();
    }

    @Test
    @DisplayName("1. POST /api/v1/auth/register - success (201 Created)")
    void testRegisterUserSuccess() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("2. POST /api/v1/auth/register - invalid email (400 Bad Request)")
    void testRegisterInvalidEmail() throws Exception {
        registerRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. POST /api/v1/auth/register - missing required field (400 Bad Request)")
    void testRegisterMissingField() throws Exception {
        registerRequest.setUsername(null);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("4. POST /api/v1/auth/login - success")
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("5. POST /api/v1/auth/logout - authenticated user")
    @WithMockUser(username = "johndoe")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("6. POST /api/v1/auth/logout - unauthenticated (401 Unauthorized)")
    void testLogoutUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. POST /api/v1/auth/register - duplicate email (409 Conflict)")
    void testRegisterDuplicateEmail() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new AplikacjePrzemyslowe.DatApp.exception.ResourceAlreadyExistsException("User", "email", "john@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }
}

