package AplikacjePrzemyslowe.DatApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encja reprezentująca kopię zapasową profilu użytkownika.
 * Używana do funkcji eksportu/importu danych profilu.
 */
@Entity
@Table(name = "profile_backups", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_id")
    private Long id;

    /**
     * Użytkownik, którego dotyczy backup (relacja N:1)
     * Nie używamy cascade - backup jest niezależny od cyklu życia User
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User nie może być null")
    private User user;

    @NotBlank(message = "Dane backupu są wymagane")
    @Column(name = "backup_data", nullable = false, columnDefinition = "LONGTEXT")
    private String backupData;

    @NotNull(message = "Format backupu jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(name = "backup_format", nullable = false, length = 10)
    private BackupFormat backupFormat;

    @Size(max = 500, message = "Opis nie może przekraczać 500 znaków")
    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    // ========== BUSINESS METHODS ==========

    /**
     * Oblicza rozmiar backupu w KB
     */
    public double getFileSizeKB() {
        return fileSizeBytes != null ? fileSizeBytes / 1024.0 : 0.0;
    }

    /**
     * Oblicza rozmiar backupu w MB
     */
    public double getFileSizeMB() {
        return fileSizeBytes != null ? fileSizeBytes / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * Sprawdza czy backup to JSON
     */
    public boolean isJson() {
        return backupFormat == BackupFormat.JSON;
    }

    /**
     * Sprawdza czy backup to XML
     */
    public boolean isXml() {
        return backupFormat == BackupFormat.XML;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileBackup that = (ProfileBackup) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt);
    }

    @Override
    public String toString() {
        return "ProfileBackup{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", format=" + backupFormat +
                ", size=" + String.format("%.2f KB", getFileSizeKB()) +
                ", createdAt=" + createdAt +
                '}';
    }
}

