package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interest Entity Tests")
class InterestTests {

    private Interest interest;

    @BeforeEach
    void setUp() {
        interest = Interest.builder()
                .id(1L)
                .name("Gry wideo")
                .description("Lubię grać w gry")
                .category("Hobby")
                .icon("gamepad")
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, interest.getId());
        assertEquals("Gry wideo", interest.getName());
        assertEquals("Lubię grać w gry", interest.getDescription());
        assertEquals("Hobby", interest.getCategory());
        assertEquals("gamepad", interest.getIcon());
        assertNotNull(interest.getProfiles());
        assertTrue(interest.getProfiles().isEmpty());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        interest.setName("Czytanie");
        interest.setDescription("Lubię książki");
        interest.setCategory("Edukacja");
        interest.setIcon("book");

        assertEquals("Czytanie", interest.getName());
        assertEquals("Lubię książki", interest.getDescription());
        assertEquals("Edukacja", interest.getCategory());
        assertEquals("book", interest.getIcon());
    }

    @Test
    @DisplayName("getUserCount powinno zwracać ilość profilei")
    void getUserCount_returnsProfilesSize() {
        assertEquals(0, interest.getUserCount());

        Profile profile1 = Profile.builder().id(1L).build();
        Profile profile2 = Profile.builder().id(2L).build();
        interest.getProfiles().add(profile1);
        interest.getProfiles().add(profile2);

        assertEquals(2, interest.getUserCount());
    }

    @Test
    @DisplayName("equals powinno porównywać id i name")
    void equals_comparesIdAndName() {
        Interest interest1 = Interest.builder().id(1L).name("Gry").build();
        Interest interest2 = Interest.builder().id(1L).name("Gry").build();
        Interest interest3 = Interest.builder().id(2L).name("Gry").build();
        Interest interest4 = Interest.builder().id(1L).name("Sport").build();

        assertEquals(interest1, interest2);
        assertNotEquals(interest1, interest3);
        assertNotEquals(interest1, interest4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        Interest interest1 = Interest.builder().id(1L).name("Gry").build();
        Interest interest2 = Interest.builder().id(1L).name("Gry").build();

        assertEquals(interest1.hashCode(), interest2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać ID, name i usersCount")
    void toString_containsKeyFields() {
        String str = interest.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("Gry wideo"));
        assertTrue(str.contains("usersCount=0"));
    }
}

