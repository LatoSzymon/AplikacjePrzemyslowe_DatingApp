package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTests {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("hashedPassword123")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warszawa")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPassword());
        assertEquals(Gender.FEMALE, user.getGender());
        assertEquals(LocalDate.of(1995, 5, 15), user.getBirthDate());
        assertEquals("Warszawa", user.getCity());
        assertTrue(user.getIsActive());
        // createdAt i updatedAt są ustawiane automatycznie przez @CreationTimestamp/@UpdateTimestamp podczas zapisu do bazy
        // Kolekcje relacji są testowane w osobnym teście (relations_initialized)
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        user.setUsername("bob");
        user.setEmail("bob@example.com");
        user.setCity("Kraków");
        user.setIsActive(false);

        assertEquals("bob", user.getUsername());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("Kraków", user.getCity());
        assertFalse(user.getIsActive());
    }

    @Test
    @DisplayName("getAge powinno obliczyć wiek na podstawie daty urodzenia")
    void getAge_calculatesCorrectly() {
        // Data urodzenia 1995-05-15, dzisiaj 2026 - powinno być ~30 lat (lub 29 jeśli przed 15 maja)
        int age = user.getAge();
        assertTrue(age >= 29 && age <= 31, "Wiek powinien być około 30 lat");

        // Test z inną datą
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        age = user.getAge();
        assertTrue(age >= 25 && age <= 26, "Wiek powinien być około 25-26 lat");

        // Test z null
        user.setBirthDate(null);
        assertEquals(0, user.getAge());
    }

    @Test
    @DisplayName("equals powinno porównywać id i email")
    void equals_comparesIdAndEmail() {
        User user1 = User.builder().id(1L).email("alice@example.com").build();
        User user2 = User.builder().id(1L).email("alice@example.com").build();
        User user3 = User.builder().id(2L).email("alice@example.com").build();
        User user4 = User.builder().id(1L).email("bob@example.com").build();

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user1, user4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        User user1 = User.builder().id(1L).email("alice@example.com").build();
        User user2 = User.builder().id(1L).email("alice@example.com").build();

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = user.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("alice"));
        assertTrue(str.contains("alice@example.com"));
        assertTrue(str.contains("FEMALE"));
        assertTrue(str.contains("Warszawa"));
        assertTrue(str.contains("isActive=true"));
    }

    @Test
    @DisplayName("Relacje (one-to-many) powinny być inicjalizowane")
    void relations_initialized() {
        assertTrue(user.getSwipesMade().isEmpty());
        assertTrue(user.getSwipesReceived().isEmpty());
        assertTrue(user.getMatchesAsUser1().isEmpty());
        assertTrue(user.getMatchesAsUser2().isEmpty());
        assertTrue(user.getMessagesSent().isEmpty());
    }

    @Test
    @DisplayName("Można dodawać do kolekcji relacji")
    void canAddToRelations() {
        User otherUser = User.builder().id(2L).username("bob").build();

        Swipe swipe = Swipe.builder().swiper(user).swipedUser(otherUser).swipeType(SwipeType.LIKE).build();
        user.getSwipesMade().add(swipe);
        assertEquals(1, user.getSwipesMade().size());

        Match match = Match.builder().user1(user).user2(otherUser).build();
        user.getMatchesAsUser1().add(match);
        assertEquals(1, user.getMatchesAsUser1().size());
    }
}

