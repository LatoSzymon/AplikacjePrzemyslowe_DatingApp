package AplikacjePrzemyslowe.DatApp.repository;

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
 * Repository dla encji User.
 * Zawiera finder methods i custom queries dla logiki biznesowej.
 * Nota: Złożone obliczenia (np. wiek, dystans) wykonywane są w serwisach.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje użytkownika po email
     */
    Optional<User> findByEmail(String email);

    /**
     * Znajduje użytkownika po username
     */
    Optional<User> findByUsername(String username);

    /**
     * Sprawdza czy użytkownik istnieje po email
     */
    boolean existsByEmail(String email);

    /**
     * Sprawdza czy użytkownik istnieje po username
     */
    boolean existsByUsername(String username);

    /**
     * Znajduje wszystkich aktywnych użytkowników z paginacją
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * Znajduje wszystkich nieaktywnych użytkowników z paginacją
     */
    Page<User> findByIsActiveFalse(Pageable pageable);

    /**
     * Znajduje użytkowników danego miasta
     */
    List<User> findByCity(String city);

    /**
     * Znajduje użytkowników danego miasta z paginacją
     */
    Page<User> findByCity(String city, Pageable pageable);

    // ========== CUSTOM QUERIES ==========

    /**
     * Znajduje kandydatów dla danego użytkownika.
     * Filtrowanie po preferencjach odbywa się w serwisie.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.id != :currentUserId
        AND u.isActive = true
        AND u.id NOT IN (
            SELECT s.swipedUser.id FROM Swipe s 
            WHERE s.swiper.id = :currentUserId
        )
        ORDER BY u.createdAt DESC
        """)
    Page<User> findCandidates(
        @Param("currentUserId") Long currentUserId,
        Pageable pageable
    );

    /**
     * Znajduje potencjalnych matchów (użytkowników którzy nas polubili i my ich polubiliśmy).
     * Używane do detektowania nowych matchów.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.id IN (
            SELECT s1.swiper.id FROM Swipe s1
            WHERE s1.swipedUser.id = :userId
            AND s1.swipeType = 'LIKE'
        )
        AND u.id IN (
            SELECT s2.swipedUser.id FROM Swipe s2
            WHERE s2.swiper.id = :userId
            AND s2.swipeType = 'LIKE'
        )
        AND u.id NOT IN (
            SELECT m.user2.id FROM Match m 
            WHERE m.user1.id = :userId AND m.isActive = true
            UNION
            SELECT m.user1.id FROM Match m 
            WHERE m.user2.id = :userId AND m.isActive = true
        )
        """)
    List<User> findPotentialMatches(@Param("userId") Long userId);

    /**
     * Znajduje użytkowników którzy nas polubili (LIKE).
     */
    @Query("""
        SELECT DISTINCT s.swiper FROM Swipe s
        WHERE s.swipedUser.id = :userId
        AND s.swipeType = 'LIKE'
        AND s.swiper.isActive = true
        ORDER BY s.swipedAt DESC
        """)
    Page<User> findUsersThatLikedMe(@Param("userId") Long userId, Pageable pageable);

    /**
     * Znajduje aktywnych użytkowników o najnowszych profilach.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.isActive = true
        AND u.profile IS NOT NULL
        ORDER BY u.profile.createdAt DESC
        """)
    Page<User> findActiveUsersWithProfiles(Pageable pageable);

    /**
     * Liczy aktywnych użytkowników
     */
    @Query("""
        SELECT COUNT(u) FROM User u
        WHERE u.isActive = true
        """)
    long countActiveUsers();
}

