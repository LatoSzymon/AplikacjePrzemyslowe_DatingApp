package AplikacjePrzemyslowe.DatApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encja reprezentująca preferencje wyszukiwania użytkownika.
 * Definiuje kryteria dla algorytmu matchingowego (płeć, wiek, odległość).
 */
@Entity
@Table(name = "preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long id;

    /**
     * Relacja 1:1 z User - każdy użytkownik ma jedną preferencję
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Poszukiwana płeć jest wymagana")
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false, length = 20)
    private Gender preferredGender;

    @NotNull(message = "Minimalny wiek jest wymagany")
    @Min(value = 18, message = "Minimalny wiek musi być co najmniej 18")
    @Max(value = 100, message = "Minimalny wiek nie może przekraczać 100")
    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @NotNull(message = "Maksymalny wiek jest wymagany")
    @Min(value = 18, message = "Maksymalny wiek musi być co najmniej 18")
    @Max(value = 100, message = "Maksymalny wiek nie może przekraczać 100")
    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @NotNull(message = "Maksymalna odległość jest wymagana")
    @Min(value = 1, message = "Maksymalna odległość musi być co najmniej 1 km")
    @Max(value = 500, message = "Maksymalna odległość nie może przekraczać 500 km")
    @Column(name = "max_distance_km", nullable = false)
    @Builder.Default
    private Integer maxDistanceKm = 50;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== BUSINESS METHODS ==========

    /**
     * Sprawdza czy dany użytkownik spełnia preferencje wieku
     */
    public boolean matchesAgePreference(int age) {
        return age >= minAge && age <= maxAge;
    }

    /**
     * Sprawdza czy dany użytkownik spełnia preferencje płci
     */
    public boolean matchesGenderPreference(Gender gender) {
        return preferredGender == gender || preferredGender == Gender.OTHER;
    }

    /**
     * Waliduje czy zakres wieku jest poprawny (min <= max)
     */
    @AssertTrue(message = "Minimalny wiek nie może być większy niż maksymalny")
    private boolean isAgeRangeValid() {
        if (minAge == null || maxAge == null) {
            return true;
        }
        return minAge <= maxAge;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Preference that = (Preference) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Preference{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", preferredGender=" + preferredGender +
                ", ageRange=" + minAge + "-" + maxAge +
                ", maxDistance=" + maxDistanceKm + "km" +
                '}';
    }
}

