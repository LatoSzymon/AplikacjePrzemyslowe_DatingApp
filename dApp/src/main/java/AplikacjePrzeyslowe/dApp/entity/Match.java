package AplikacjePrzeyslowe.dApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Encja reprezentująca dopasowanie (match) między dwoma użytkownikami.
 * Tworzona gdy obaj użytkownicy dali sobie wzajemnie LIKE.
 */
@Entity
@Table(name = "matches",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user1_user2", columnNames = {"user1_id", "user2_id"})
       },
       indexes = {
           @Index(name = "idx_user1_id", columnList = "user1_id"),
           @Index(name = "idx_user2_id", columnList = "user2_id"),
           @Index(name = "idx_is_active", columnList = "is_active")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    /**
     * Pierwszy użytkownik w dopasowaniu (relacja N:1)
     * Zazwyczaj ten, który dał LIKE jako drugi (inicjator matcha)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    @NotNull(message = "User1 nie może być null")
    private User user1;

    /**
     * Drugi użytkownik w dopasowaniu (relacja N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    @NotNull(message = "User2 nie może być null")
    private User user2;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "matched_at", nullable = false, updatable = false)
    private LocalDateTime matchedAt;

    @Column(name = "unmatched_at")
    private LocalDateTime unmatchedAt;

    // ========== RELACJE ==========

    /**
     * Wiadomości wymienione w ramach tego dopasowania (relacja 1:N)
     * Kaskadowe usuwanie wiadomości przy unmatch
     */
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    // ========== BUSINESS METHODS ==========

    /**
     * Dodaje wiadomość do konwersacji
     */
    public void addMessage(Message message) {
        if (message != null) {
            message.setMatch(this);
            messages.add(message);
        }
    }

    /**
     * Usuwa wiadomość z konwersacji
     */
    public void removeMessage(Message message) {
        if (message != null) {
            messages.remove(message);
        }
    }

    /**
     * Sprawdza czy użytkownik jest częścią tego dopasowania
     */
    public boolean containsUser(User user) {
        return user1.equals(user) || user2.equals(user);
    }

    /**
     * Zwraca partnera w dopasowaniu (drugi użytkownik)
     */
    public User getPartner(User user) {
        if (user1.equals(user)) {
            return user2;
        } else if (user2.equals(user)) {
            return user1;
        }
        return null;
    }

    /**
     * Wykonuje unmatch - deaktywuje dopasowanie
     */
    public void unmatch() {
        this.isActive = false;
        this.unmatchedAt = LocalDateTime.now();
    }

    /**
     * Zwraca liczbę wiadomości w konwersacji
     */
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Sprawdza czy konwersacja została rozpoczęta (czy są jakieś wiadomości)
     */
    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(id, match.id) &&
               ((Objects.equals(user1, match.user1) && Objects.equals(user2, match.user2)) ||
                (Objects.equals(user1, match.user2) && Objects.equals(user2, match.user1)));
    }

    @Override
    public int hashCode() {
        // Hashcode nie może zależeć od kolejności user1/user2
        Long id1 = user1 != null ? user1.getId() : null;
        Long id2 = user2 != null ? user2.getId() : null;
        if (id1 != null && id2 != null) {
            return Objects.hash(id, Math.min(id1, id2), Math.max(id1, id2));
        }
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", user1Id=" + (user1 != null ? user1.getId() : null) +
                ", user2Id=" + (user2 != null ? user2.getId() : null) +
                ", isActive=" + isActive +
                ", matchedAt=" + matchedAt +
                ", messagesCount=" + messages.size() +
                '}';
    }
}

