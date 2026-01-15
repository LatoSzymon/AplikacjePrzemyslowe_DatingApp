package AplikacjePrzemyslowe.DatApp.repository;

import AplikacjePrzemyslowe.DatApp.entity.Swipe;
import AplikacjePrzemyslowe.DatApp.entity.SwipeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository dla encji Swipe.
 */
@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Sprawdza czy użytkownik swipnął innego użytkownika
     */
    boolean existsBySwipedUserIdAndSwiperId(Long swipedUserId, Long swiperId);

    /**
     * Znajduje wszystkie swipe'y wykonane przez użytkownika
     */
    List<Swipe> findBySwiperId(Long swiperId);

    /**
     * Znajduje wszystkie swipe'y otrzymane przez użytkownika
     */
    List<Swipe> findBySwipedUserId(Long swipedUserId);

    /**
     * Liczy LIKE'i wykonane przez użytkownika
     */
    long countBySwipedUserIdAndSwipeType(Long swipedUserId, SwipeType swipeType);

    // ========== CUSTOM QUERIES ==========

    /**
     * Znajduje konkretny swipe pomiędzy dwoma użytkownikami
     */
    @Query("""
        SELECT s FROM Swipe s
        WHERE s.swiper.id = :swiperId
        AND s.swipedUser.id = :swipedUserId
        """)
    Optional<Swipe> findSwipe(
        @Param("swiperId") Long swiperId,
        @Param("swipedUserId") Long swipedUserId
    );

    /**
     * Sprawdza czy między dwoma użytkownikami istnieje wzajemny LIKE
     */
    @Query("""
        SELECT COUNT(s1) > 0 AND COUNT(s2) > 0 FROM Swipe s1, Swipe s2
        WHERE s1.swiper.id = :userId1
        AND s1.swipedUser.id = :userId2
        AND s1.swipeType = 'LIKE'
        AND s2.swiper.id = :userId2
        AND s2.swipedUser.id = :userId1
        AND s2.swipeType = 'LIKE'
        """)
    boolean isMutualLike(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2
    );

    /**
     * Znajduje wszystkie LIKE'i (polubienia) do danego użytkownika
     */
    @Query("""
        SELECT s FROM Swipe s
        WHERE s.swipedUser.id = :userId
        AND s.swipeType = 'LIKE'
        ORDER BY s.swipedAt DESC
        """)
    Page<Swipe> findAllLikesReceived(@Param("userId") Long userId, Pageable pageable);

    /**
     * Znajduje wszystkie LIKE'i (polubienia) od danego użytkownika
     */
    @Query("""
        SELECT s FROM Swipe s
        WHERE s.swiper.id = :userId
        AND s.swipeType = 'LIKE'
        ORDER BY s.swipedAt DESC
        """)
    Page<Swipe> findAllLikesMade(@Param("userId") Long userId, Pageable pageable);

    /**
     * Liczy wszystkie LIKE'i wykonane przez użytkownika
     */
    @Query("""
        SELECT COUNT(s) FROM Swipe s
        WHERE s.swiper.id = :userId
        AND s.swipeType = 'LIKE'
        """)
    long countLikesMade(@Param("userId") Long userId);

    /**
     * Liczy wszystkie LIKE'i otrzymane przez użytkownika
     */
    @Query("""
        SELECT COUNT(s) FROM Swipe s
        WHERE s.swipedUser.id = :userId
        AND s.swipeType = 'LIKE'
        """)
    long countLikesReceived(@Param("userId") Long userId);

    /**
     * Liczy wszystkie DISLIKE'i dla użytkownika
     */
    @Query("""
        SELECT COUNT(s) FROM Swipe s
        WHERE s.swiper.id = :userId
        AND s.swipeType = 'DISLIKE'
        """)
    long countDislikesMade(@Param("userId") Long userId);

    /**
     * Znajduje użytkowników których daliśmy DISLIKE
     */
    @Query("""
        SELECT s.swipedUser.id FROM Swipe s
        WHERE s.swiper.id = :userId
        AND s.swipeType = 'DISLIKE'
        """)
    List<Long> findDislikedUserIds(@Param("userId") Long userId);

    /**
     * Liczba profili już ocenionych (swipów)
     */
    @Query("""
        SELECT COUNT(DISTINCT s.swipedUser) FROM Swipe s
        WHERE s.swiper.id = :userId
        """)
    long countReviewedProfiles(@Param("userId") Long userId);

    /**
     * Znajduje ostatnie swipe'y użytkownika (dla historii)
     */
    @Query("""
        SELECT s FROM Swipe s
        WHERE s.swiper.id = :userId
        ORDER BY s.swipedAt DESC
        """)
    Page<Swipe> findUserSwipeHistory(@Param("userId") Long userId, Pageable pageable);
}

