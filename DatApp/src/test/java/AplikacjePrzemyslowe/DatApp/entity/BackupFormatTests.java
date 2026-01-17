package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BackupFormat enum tests")
class BackupFormatTests {

    @Test
    @DisplayName("Powinien zawierać JSON i XML w tej kolejności")
    void values_containsJsonAndXml() {
        BackupFormat[] values = BackupFormat.values();
        assertEquals(2, values.length);
        assertEquals(BackupFormat.JSON, values[0]);
        assertEquals(BackupFormat.XML, values[1]);
    }

    @Test
    @DisplayName("valueOf powinno zwracać poprawne enumy")
    void valueOf_returnsEnum() {
        assertEquals(BackupFormat.JSON, BackupFormat.valueOf("JSON"));
        assertEquals(BackupFormat.XML, BackupFormat.valueOf("XML"));
    }
}

