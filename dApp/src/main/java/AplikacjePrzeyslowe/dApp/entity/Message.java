package AplikacjePrzeyslowe.dApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encja reprezentująca wiadomość wymienioną między dopasowanymi użytkownikami.
 * Każda wiadomość należy do konkretnego dopasowania (Match).
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_match_id", columnList = "match_id"),
        @Index(name = "idx_sender_id", columnList = "sender_id"),
        @Index(name = "idx_sent_at", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    /**
     * Dopasowanie, do którego należy wiadomość (relacja N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @NotNull(message = "Match nie może być null")
    private Match match;

    /**
     * Nadawca wiadomości (relacja N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Nadawca nie może być null")
    private User sender;

    @NotBlank(message = "Treść wiadomości jest wymagana")
    @Size(max = 2000, message = "Wiadomość nie może przekraczać 2000 znaków")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ========== BUSINESS METHODS ==========

    /**
     * Oznacza wiadomość jako przeczytaną
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Sprawdza czy wiadomość została wysłana przez danego użytkownika
     */
    public boolean isSentBy(User user) {
        return sender.equals(user);
    }

    /**
     * Zwraca odbiorcę wiadomości (partner w konwersacji)
     */
    public User getRecipient() {
        return match.getPartner(sender);
    }

    /**
     * Sprawdza czy wiadomość jest nieprzeczytana
     */
    public boolean isUnread() {
        return !isRead;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id) &&
               Objects.equals(sentAt, message.sentAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sentAt);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", matchId=" + (match != null ? match.getId() : null) +
                ", senderId=" + (sender != null ? sender.getId() : null) +
                ", content='" + (content != null ? content.substring(0, Math.min(30, content.length())) : null) + "..." + '\'' +
                ", isRead=" + isRead +
                ", sentAt=" + sentAt +
                '}';
    }
}

