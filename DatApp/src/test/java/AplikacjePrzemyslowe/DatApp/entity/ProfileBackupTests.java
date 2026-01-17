package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileBackup Entity Tests")
class ProfileBackupTests {

    private ProfileBackup backup;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("alice").build();
        backup = ProfileBackup.builder()
                .id(1L)
                .user(user)
                .backupData("{\"bio\": \"Test bio\", \"interests\": []}")
                .backupFormat(BackupFormat.JSON)
                .description("Backup z dnia 2025-01-15")
                .fileSizeBytes(2048L) // 2 KB
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, backup.getId());
        assertEquals(user, backup.getUser());
        assertEquals("{\"bio\": \"Test bio\", \"interests\": []}", backup.getBackupData());
        assertEquals(BackupFormat.JSON, backup.getBackupFormat());
        assertEquals("Backup z dnia 2025-01-15", backup.getDescription());
        assertEquals(2048L, backup.getFileSizeBytes());
        // createdAt jest ustawiany automatycznie przez @CreationTimestamp podczas zapisu do bazy
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        backup.setBackupFormat(BackupFormat.XML);
        backup.setDescription("Nowy opis");
        backup.setFileSizeBytes(4096L);

        assertEquals(BackupFormat.XML, backup.getBackupFormat());
        assertEquals("Nowy opis", backup.getDescription());
        assertEquals(4096L, backup.getFileSizeBytes());
    }

    @Test
    @DisplayName("getFileSizeKB powinno konwertować bytes na KB")
    void getFileSizeKB_convertsToKilobytes() {
        assertEquals(2.0, backup.getFileSizeKB(), 0.01);

        backup.setFileSizeBytes(1024L);
        assertEquals(1.0, backup.getFileSizeKB(), 0.01);

        backup.setFileSizeBytes(null);
        assertEquals(0.0, backup.getFileSizeKB(), 0.01);
    }

    @Test
    @DisplayName("getFileSizeMB powinno konwertować bytes na MB")
    void getFileSizeMB_convertsToMegabytes() {
        backup.setFileSizeBytes(1024 * 1024L); // 1 MB
        assertEquals(1.0, backup.getFileSizeMB(), 0.01);

        backup.setFileSizeBytes(2048 * 1024L); // 2 MB
        assertEquals(2.0, backup.getFileSizeMB(), 0.01);

        backup.setFileSizeBytes(null);
        assertEquals(0.0, backup.getFileSizeMB(), 0.01);
    }

    @Test
    @DisplayName("isJson powinno zwrócić true dla formatu JSON")
    void isJson_returnsTrueForJsonFormat() {
        backup.setBackupFormat(BackupFormat.JSON);
        assertTrue(backup.isJson());

        backup.setBackupFormat(BackupFormat.XML);
        assertFalse(backup.isJson());
    }

    @Test
    @DisplayName("isXml powinno zwrócić true dla formatu XML")
    void isXml_returnsTrueForXmlFormat() {
        backup.setBackupFormat(BackupFormat.XML);
        assertTrue(backup.isXml());

        backup.setBackupFormat(BackupFormat.JSON);
        assertFalse(backup.isXml());
    }

    @Test
    @DisplayName("equals powinno porównywać id i createdAt")
    void equals_comparesIdAndCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        ProfileBackup backup1 = ProfileBackup.builder().id(1L).createdAt(now).build();
        ProfileBackup backup2 = ProfileBackup.builder().id(1L).createdAt(now).build();
        ProfileBackup backup3 = ProfileBackup.builder().id(2L).createdAt(now).build();
        ProfileBackup backup4 = ProfileBackup.builder().id(1L).createdAt(now.plusSeconds(1)).build();

        assertEquals(backup1, backup2);
        assertNotEquals(backup1, backup3);
        assertNotEquals(backup1, backup4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        LocalDateTime now = LocalDateTime.now();
        ProfileBackup backup1 = ProfileBackup.builder().id(1L).createdAt(now).build();
        ProfileBackup backup2 = ProfileBackup.builder().id(1L).createdAt(now).build();

        assertEquals(backup1.hashCode(), backup2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = backup.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("userId=1"));
        assertTrue(str.contains("format=JSON"));
        assertTrue(str.contains("KB"));
    }
}

