package AplikacjePrzemyslowe.DatApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Encja reprezentująca zainteresowanie/hobby w systemie.
 * Współdzielona przez wielu użytkowników (relacja N:N z Profile).
 */
@Entity
@Table(name = "interests", indexes = {
        @Index(name = "idx_interest_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long id;

    @NotBlank(message = "Nazwa zainteresowania jest wymagana")
    @Size(min = 2, max = 50, message = "Nazwa zainteresowania musi mieć od 2 do 50 znaków")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 200, message = "Opis nie może przekraczać 200 znaków")
    @Column(length = 200)
    private String description;

    @Size(max = 50, message = "Kategoria nie może przekraczać 50 znaków")
    @Column(length = 50)
    private String category;

    @Size(max = 20, message = "Ikona nie może przekraczać 20 znaków")
    @Column(length = 20)
    private String icon;

    // ========== RELACJE ==========

    /**
     * Profile, które mają to zainteresowanie (relacja N:N)
     * Nie używamy cascade - Interest jest niezależny od Profile
     */
    @ManyToMany(mappedBy = "interests", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Profile> profiles = new HashSet<>();

    // ========== BUSINESS METHODS ==========

    /**
     * Zwraca liczbę użytkowników z tym zainteresowaniem
     */
    public int getUserCount() {
        return profiles.size();
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interest interest = (Interest) o;
        return Objects.equals(id, interest.id) && Objects.equals(name, interest.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Interest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", usersCount=" + profiles.size() +
                '}';
    }
}

