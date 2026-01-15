package AplikacjePrzemyslowe.DatApp.repository;

import AplikacjePrzemyslowe.DatApp.entity.Match;
import AplikacjePrzemyslowe.DatApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository dla encji Match.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje wszystkie matche dla użytkownika (jako user1 lub user2) - paginowane
     */
    Page<Match> findByUser1OrUser2(User user1, User user2, Pageable pageable);

    /**
     * Liczy wszystkie matche dla użytkownika (jako user1 lub user2)
     */
    long countByUser1OrUser2(User user1, User user2);

    /**
     * Znajduje match między dwoma użytkownikami
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1 = :user1 AND m.user2 = :user2)
        OR (m.user1 = :user2 AND m.user2 = :user1)
        """)
    Optional<Match> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Znajduje wszystkie aktywne dopasowania użytkownika
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        ORDER BY m.matchedAt DESC
        """)
    Page<Match> findActiveMatches(@Param("userId") Long userId, Pageable pageable);

    /**
     * Znajduje wszystkie dopasowania użytkownika (aktywne i nieaktywne)
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        ORDER BY m.matchedAt DESC
        """)
    Page<Match> findAllUserMatches(@Param("userId") Long userId, Pageable pageable);

    /**
     * Liczy aktywne dopasowania użytkownika
     */
    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        """)
    long countActiveMatches(@Param("userId") Long userId);

    /**
     * Liczy wszystkie dopasowania użytkownika
     */
    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        """)
    long countAllMatches(@Param("userId") Long userId);

    // ========== CUSTOM QUERIES ==========

    /**
     * Znajduje konkretne dopasowanie między dwoma użytkownikami
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1.id = :userId1 AND m.user2.id = :userId2)
        OR (m.user1.id = :userId2 AND m.user2.id = :userId1)
        """)
    Optional<Match> findMatchBetween(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2
    );

    /**
     * Sprawdza czy między dwoma użytkownikami istnieje aktywne dopasowanie
     */
    @Query("""
        SELECT COUNT(m) > 0 FROM Match m
        WHERE ((m.user1.id = :userId1 AND m.user2.id = :userId2)
        OR (m.user1.id = :userId2 AND m.user2.id = :userId1))
        AND m.isActive = true
        """)
    boolean isActiveMatch(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2
    );

    /**
     * Znajduje nowe dopasowania od wczoraj
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        AND m.matchedAt >= CURRENT_TIMESTAMP - 1 DAY
        ORDER BY m.matchedAt DESC
        """)
    List<Match> findRecentMatches(@Param("userId") Long userId);

    /**
     * Znajduje partnerów danego użytkownika (wszyscy z aktywnych matchów)
     */
    @Query("""
        SELECT CASE 
            WHEN m.user1.id = :userId THEN m.user2
            ELSE m.user1
        END
        FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        ORDER BY m.matchedAt DESC
        """)
    Page<Object> findPartners(@Param("userId") Long userId, Pageable pageable);

    /**
     * Liczy dopasowania z wiadomościami
     */
    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        AND m.messages IS NOT EMPTY
        """)
    long countMatchesWithMessages(@Param("userId") Long userId);

    /**
     * Znajduje dopasowania bez żadnych wiadomości (nie rozpooznęte rozmowy)
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        AND m.messages IS EMPTY
        ORDER BY m.matchedAt DESC
        """)
    Page<Match> findMatchesWithoutMessages(@Param("userId") Long userId, Pageable pageable);

    /**
     * Liczy nieaktywne dopasowania (unmatch'e)
     */
    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = false
        """)
    long countUnmatchedProfiles(@Param("userId") Long userId);

    /**
     * Znajduje matchów z największą liczbą wiadomości
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        ORDER BY SIZE(m.messages) DESC
        """)
    Page<Match> findActiveMatchesOrderByMessageCount(@Param("userId") Long userId, Pageable pageable);

    /**
     * Liczy wszystkie wiadomości w aktywnych matchach użytkownika
     */
    @Query("""
        SELECT COUNT(msg) FROM Match m
        JOIN m.messages msg
        WHERE (m.user1.id = :userId OR m.user2.id = :userId)
        AND m.isActive = true
        """)
    long countTotalMessages(@Param("userId") Long userId);

    /**
     * Liczy nieprzeczytane wiadomości dla użytkownika
     */
    @Query("""
        SELECT COUNT(msg) FROM Match m
        JOIN m.messages msg
        WHERE ((m.user1.id = :userId AND msg.sender.id != :userId)
            OR (m.user2.id = :userId AND msg.sender.id != :userId))
        AND m.isActive = true
        AND msg.isRead = false
        """)
    long countUnreadMessages(@Param("userId") Long userId);
}

