package AplikacjePrzemyslowe.DatApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Encja reprezentująca użytkownika systemu randkowego.
 * Zawiera dane logowania, podstawowe informacje demograficzne oraz relacje do profilu.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "Username jest wymagany")
    @Size(min = 3, max = 50, message = "Username musi mieć od 3 do 50 znaków")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Email musi być poprawny")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć minimum 8 znaków")
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull(message = "Płeć jest wymagana")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotBlank(message = "Miasto jest wymagane")
    @Size(max = 100, message = "Nazwa miasta nie może przekraczać 100 znaków")
    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== RELACJE ==========

    /**
     * Profil użytkownika (relacja 1:1)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Profile profile;

    /**
     * Preferencje użytkownika (relacja 1:1)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Preference preference;

    /**
     * Swipe'y wykonane przez użytkownika (relacja 1:N)
     * Kaskadowe usuwanie przy usunięciu konta
     */
    @OneToMany(mappedBy = "swiper", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Swipe> swipesMade = new HashSet<>();

    /**
     * Swipe'y otrzymane przez użytkownika (relacja 1:N)
     * Kaskadowe usuwanie przy usunięciu konta
     */
    @OneToMany(mappedBy = "swipedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Swipe> swipesReceived = new HashSet<>();

    /**
     * Dopasowania gdzie użytkownik jest inicjatorem (relacja 1:N)
     * Kaskadowe usuwanie przy usunięciu konta
     */
    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Match> matchesAsUser1 = new HashSet<>();

    /**
     * Dopasowania gdzie użytkownik jest odbiorcą (relacja 1:N)
     * Kaskadowe usuwanie przy usunięciu konta
     */
    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Match> matchesAsUser2 = new HashSet<>();

    /**
     * Wiadomości wysłane przez użytkownika (relacja 1:N)
     * Kaskadowe usuwanie przy usunięciu konta
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messagesSent = new HashSet<>();

    // ========== BUSINESS METHODS ==========

    /**
     * Oblicza wiek użytkownika na podstawie daty urodzenia
     */
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }


    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", age=" + getAge() +
                ", city='" + city + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

