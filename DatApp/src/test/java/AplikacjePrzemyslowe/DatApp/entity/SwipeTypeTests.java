package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SwipeType enum tests")
class SwipeTypeTests {

    @Test
    @DisplayName("Powinien zawierać LIKE, DISLIKE, PASS, SUPER_LIKE")
    void values_containsAll() {
        SwipeType[] values = SwipeType.values();
        assertEquals(4, values.length);
        assertEquals(SwipeType.LIKE, values[0]);
        assertEquals(SwipeType.DISLIKE, values[1]);
        assertEquals(SwipeType.PASS, values[2]);
        assertEquals(SwipeType.SUPER_LIKE, values[3]);
    }

    @Test
    @DisplayName("valueOf powinno zwracać poprawne enumy")
    void valueOf_returnsEnum() {
        assertEquals(SwipeType.LIKE, SwipeType.valueOf("LIKE"));
        assertEquals(SwipeType.DISLIKE, SwipeType.valueOf("DISLIKE"));
        assertEquals(SwipeType.PASS, SwipeType.valueOf("PASS"));
        assertEquals(SwipeType.SUPER_LIKE, SwipeType.valueOf("SUPER_LIKE"));
    }
}

