package AplikacjePrzemyslowe.DatApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Encja reprezentująca profil użytkownika w systemie randkowym.
 * Zawiera rozszerzone informacje: bio, zdjęcia, zainteresowania.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    /**
     * Relacja 1:1 z User - każdy profil należy do jednego użytkownika
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 1000, message = "Bio nie może przekraczać 1000 znaków")
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Min(value = 150, message = "Wzrost musi być co najmniej 150 cm")
    @Max(value = 250, message = "Wzrost nie może przekraczać 250 cm")
    @Column(name = "height_cm")
    private Integer heightCm;

    @Size(max = 100, message = "Zawód nie może przekraczać 100 znaków")
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Size(max = 100, message = "Wykształcenie nie może przekraczać 100 znaków")
    @Column(name = "education", length = 100)
    private String education;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== RELACJE ==========

    /**
     * Zdjęcia profilu (relacja 1:N)
     * Kaskadowe usuwanie zdjęć przy usunięciu profilu
     */
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Photo> photos = new HashSet<>();

    /**
     * Zainteresowania użytkownika (relacja N:N)
     * Nie kaskadujemy usuwania Interest - są współdzielone między użytkownikami
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profile_interests",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    @Builder.Default
    private Set<Interest> interests = new HashSet<>();

    // ========== BUSINESS METHODS ==========

    /**
     * Dodaje zdjęcie do profilu
     */
    public void addPhoto(Photo photo) {
        if (photo != null) {
            photo.setProfile(this);
            photos.add(photo);
        }
    }

    /**
     * Usuwa zdjęcie z profilu
     */
    public void removePhoto(Photo photo) {
        if (photo != null) {
            photos.remove(photo);
        }
    }

    /**
     * Dodaje zainteresowanie do profilu
     */
    public void addInterest(Interest interest) {
        if (interest != null) {
            interests.add(interest);
        }
    }

    /**
     * Usuwa zainteresowanie z profilu
     */
    public void removeInterest(Interest interest) {
        if (interest != null) {
            interests.remove(interest);
        }
    }

    /**
     * Zwraca główne zdjęcie profilu (pierwsze w kolejności)
     */
    public Photo getMainPhoto() {
        return photos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsPrimary()))
                .findFirst()
                .orElse(photos.stream().findFirst().orElse(null));
    }

    /**
     * Sprawdza czy profil jest kompletny (ma bio i przynajmniej jedno zdjęcie)
     */
    public boolean isComplete() {
        return bio != null && !bio.isBlank() && !photos.isEmpty();
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(id, profile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", bio='" + (bio != null ? bio.substring(0, Math.min(50, bio.length())) : null) + "..." +
                ", photosCount=" + photos.size() +
                ", interestsCount=" + interests.size() +
                '}';
    }
}

