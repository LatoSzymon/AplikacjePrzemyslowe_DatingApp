package AplikacjePrzeyslowe.dApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encja reprezentująca zdjęcie w profilu użytkownika.
 * Każde zdjęcie jest powiązane z jednym profilem.
 */
@Entity
@Table(name = "photos", indexes = {
        @Index(name = "idx_profile_id", columnList = "profile_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id;

    /**
     * Relacja N:1 z Profile - wiele zdjęć należy do jednego profilu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @NotBlank(message = "URL zdjęcia jest wymagany")
    @Size(max = 500, message = "URL zdjęcia nie może przekraczać 500 znaków")
    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Min(value = 0, message = "Kolejność nie może być ujemna")
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // ========== BUSINESS METHODS ==========

    /**
     * Ustawia to zdjęcie jako główne
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Usuwa status głównego zdjęcia
     */
    public void removeAsPrimary() {
        this.isPrimary = false;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(id, photo.id) && Objects.equals(photoUrl, photo.photoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, photoUrl);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", profileId=" + (profile != null ? profile.getId() : null) +
                ", photoUrl='" + photoUrl + '\'' +
                ", isPrimary=" + isPrimary +
                ", displayOrder=" + displayOrder +
                '}';
    }
}

