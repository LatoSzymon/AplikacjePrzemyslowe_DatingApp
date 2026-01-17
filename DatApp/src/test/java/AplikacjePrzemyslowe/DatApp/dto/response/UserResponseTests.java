package AplikacjePrzemyslowe.DatApp.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserResponse DTO Tests")
class UserResponseTests {

    @Test
    @DisplayName("Builder powinien ustawić id, username, email, gender")
    void builder_setsFields() {
        UserResponse resp = UserResponse.builder()
                .id(42L)
                .username("alice")
                .email("alice@example.com")
                .gender("FEMALE")
                .build();

        assertEquals(42L, resp.getId());
        assertEquals("alice", resp.getUsername());
        assertEquals("alice@example.com", resp.getEmail());
        assertEquals("FEMALE", resp.getGender());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        UserResponse resp = new UserResponse();
        resp.setId(7L);
        resp.setUsername("bob");
        resp.setEmail("bob@example.com");
        resp.setGender("MALE");

        assertEquals(7L, resp.getId());
        assertEquals("bob", resp.getUsername());
        assertEquals("bob@example.com", resp.getEmail());
        assertEquals("MALE", resp.getGender());
    }

    @Test
    @DisplayName("Domyślne wartości powinny być null dla pól")
    void defaults_areNull() {
        UserResponse resp = new UserResponse();
        assertNull(resp.getId());
        assertNull(resp.getUsername());
        assertNull(resp.getEmail());
        assertNull(resp.getGender());
    }
}

