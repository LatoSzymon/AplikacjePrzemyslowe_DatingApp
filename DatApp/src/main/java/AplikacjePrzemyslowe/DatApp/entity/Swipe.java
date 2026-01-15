package AplikacjePrzemyslowe.DatApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encja reprezentująca akcję "swipe" (przesunięcie) użytkownika.
 * Rejestruje decyzję użytkownika: LIKE (akceptacja) lub DISLIKE (odrzucenie).
 */
@Entity
@Table(name = "swipes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_swiper_swiped", columnNames = {"swiper_id", "swiped_user_id"})
       },
       indexes = {
           @Index(name = "idx_swiper_id", columnList = "swiper_id"),
           @Index(name = "idx_swiped_user_id", columnList = "swiped_user_id"),
           @Index(name = "idx_swipe_type", columnList = "swipe_type")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "swipe_id")
    private Long id;

    /**
     * Użytkownik wykonujący swipe (relacja N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiper_id", nullable = false)
    @NotNull(message = "Swiper nie może być null")
    private User swiper;

    /**
     * Użytkownik będący obiektem swipe'a (relacja N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiped_user_id", nullable = false)
    @NotNull(message = "Swiped user nie może być null")
    private User swipedUser;

    @NotNull(message = "Typ swipe'a jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(name = "swipe_type", nullable = false, length = 20)
    private SwipeType swipeType;

    @CreationTimestamp
    @Column(name = "swiped_at", nullable = false, updatable = false)
    private LocalDateTime swipedAt;

    // ========== BUSINESS METHODS ==========

    /**
     * Sprawdza czy swipe to LIKE
     */
    public boolean isLike() {
        return swipeType == SwipeType.LIKE;
    }

    /**
     * Sprawdza czy swipe to DISLIKE
     */
    public boolean isDislike() {
        return swipeType == SwipeType.DISLIKE;
    }

    /**
     * Sprawdza czy swipe to SUPER_LIKE
     */
    public boolean isSuperLike() {
        return swipeType == SwipeType.SUPER_LIKE;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Swipe swipe = (Swipe) o;
        return Objects.equals(id, swipe.id) &&
               Objects.equals(swiper, swipe.swiper) &&
               Objects.equals(swipedUser, swipe.swipedUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, swiper != null ? swiper.getId() : null,
                           swipedUser != null ? swipedUser.getId() : null);
    }

    @Override
    public String toString() {
        return "Swipe{" +
                "id=" + id +
                ", swiperId=" + (swiper != null ? swiper.getId() : null) +
                ", swipedUserId=" + (swipedUser != null ? swipedUser.getId() : null) +
                ", swipeType=" + swipeType +
                ", swipedAt=" + swipedAt +
                '}';
    }
}

