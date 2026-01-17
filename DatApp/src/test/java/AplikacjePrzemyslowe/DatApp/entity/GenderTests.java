package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gender enum tests")
class GenderTests {

    @Test
    @DisplayName("Powinien zawierać MALE, FEMALE, OTHER w tej kolejności")
    void values_containsAll() {
        Gender[] values = Gender.values();
        assertEquals(3, values.length);
        assertEquals(Gender.MALE, values[0]);
        assertEquals(Gender.FEMALE, values[1]);
        assertEquals(Gender.OTHER, values[2]);
    }

    @Test
    @DisplayName("valueOf powinno zwracać poprawne enumy")
    void valueOf_returnsEnum() {
        assertEquals(Gender.MALE, Gender.valueOf("MALE"));
        assertEquals(Gender.FEMALE, Gender.valueOf("FEMALE"));
        assertEquals(Gender.OTHER, Gender.valueOf("OTHER"));
    }
}

