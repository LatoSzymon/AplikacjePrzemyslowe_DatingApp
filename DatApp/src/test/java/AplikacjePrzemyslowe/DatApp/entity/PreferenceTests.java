package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Preference Entity Tests")
class PreferenceTests {

    private Preference preference;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("alice").build();
        preference = Preference.builder()
                .id(1L)
                .user(user)
                .preferredGender(Gender.MALE)
                .minAge(25)
                .maxAge(35)
                .maxDistanceKm(50)
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, preference.getId());
        assertEquals(user, preference.getUser());
        assertEquals(Gender.MALE, preference.getPreferredGender());
        assertEquals(25, preference.getMinAge());
        assertEquals(35, preference.getMaxAge());
        assertEquals(50, preference.getMaxDistanceKm());
        // createdAt i updatedAt są ustawiane automatycznie przez @CreationTimestamp/@UpdateTimestamp podczas zapisu do bazy
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        preference.setPreferredGender(Gender.FEMALE);
        preference.setMinAge(20);
        preference.setMaxAge(40);
        preference.setMaxDistanceKm(100);

        assertEquals(Gender.FEMALE, preference.getPreferredGender());
        assertEquals(20, preference.getMinAge());
        assertEquals(40, preference.getMaxAge());
        assertEquals(100, preference.getMaxDistanceKm());
    }

    @Test
    @DisplayName("matchesAgePreference powinno sprawdzić czy wiek mieści się w zakresie")
    void matchesAgePreference_checksAge() {
        assertTrue(preference.matchesAgePreference(25)); // min
        assertTrue(preference.matchesAgePreference(30)); // middle
        assertTrue(preference.matchesAgePreference(35)); // max
        assertFalse(preference.matchesAgePreference(24)); // poniżej min
        assertFalse(preference.matchesAgePreference(36)); // powyżej max
    }

    @Test
    @DisplayName("matchesGenderPreference powinno sprawdzić płeć")
    void matchesGenderPreference_checksGender() {
        // preference.preferredGender = MALE
        assertTrue(preference.matchesGenderPreference(Gender.MALE));
        assertFalse(preference.matchesGenderPreference(Gender.FEMALE));

        // Gdy preferredGender = OTHER, wszystko się zgadza
        preference.setPreferredGender(Gender.OTHER);
        assertTrue(preference.matchesGenderPreference(Gender.MALE));
        assertTrue(preference.matchesGenderPreference(Gender.FEMALE));
        assertTrue(preference.matchesGenderPreference(Gender.OTHER));
    }

    @Test
    @DisplayName("equals powinno porównywać tylko id")
    void equals_comparesId() {
        Preference pref1 = Preference.builder().id(1L).build();
        Preference pref2 = Preference.builder().id(1L).build();
        Preference pref3 = Preference.builder().id(2L).build();

        assertEquals(pref1, pref2);
        assertNotEquals(pref1, pref3);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        Preference pref1 = Preference.builder().id(1L).build();
        Preference pref2 = Preference.builder().id(1L).build();

        assertEquals(pref1.hashCode(), pref2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = preference.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("userId=1"));
        assertTrue(str.contains("preferredGender=MALE"));
        assertTrue(str.contains("25-35"));
        assertTrue(str.contains("50km"));
    }
}

