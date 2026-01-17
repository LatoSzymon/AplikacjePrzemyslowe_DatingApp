package AplikacjePrzemyslowe.DatApp.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthResponse DTO Tests")
class AuthResponseTests {

    @Test
    @DisplayName("Builder powinien ustawić pola accessToken i expiresIn")
    void builder_setsFields() {
        AuthResponse resp = AuthResponse.builder()
                .accessToken("token-123")
                .expiresIn(3600L)
                .build();

        assertEquals("token-123", resp.getAccessToken());
        assertEquals(3600L, resp.getExpiresIn());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        AuthResponse resp = new AuthResponse();
        resp.setAccessToken("abc");
        resp.setExpiresIn(1800L);

        assertEquals("abc", resp.getAccessToken());
        assertEquals(1800L, resp.getExpiresIn());
    }

    @Test
    @DisplayName("Domyślne wartości powinny być null dla pól")
    void defaults_areNull() {
        AuthResponse resp = new AuthResponse();
        assertNull(resp.getAccessToken());
        assertNull(resp.getExpiresIn());
    }
}

