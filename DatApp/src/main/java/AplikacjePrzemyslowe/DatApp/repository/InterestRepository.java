package AplikacjePrzemyslowe.DatApp.repository;

import AplikacjePrzemyslowe.DatApp.entity.Interest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository dla encji Interest.
 */
@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje zainteresowanie po nazwie
     */
    Optional<Interest> findByName(String name);

    /**
     * Sprawdza czy zainteresowanie istnieje po nazwie
     */
    boolean existsByName(String name);

    /**
     * Znajduje zainteresowania po kategorii
     */
    Page<Interest> findByCategory(String category, Pageable pageable);

    /**
     * Znajduje wszystkie zainteresowania danego użytkownika
     */
    @Query("""
        SELECT DISTINCT i FROM Interest i
        WHERE i IN (
            SELECT i2 FROM Profile p
            JOIN p.interests i2
            WHERE p.user.id = :userId
        )
        """)
    List<Interest> findUserInterests(@Param("userId") Long userId);

    /**
     * Znajduje zainteresowania wspólne dla dwóch użytkowników
     */
    @Query("""
        SELECT DISTINCT i FROM Interest i
        WHERE i IN (
            SELECT i2 FROM Profile p1
            JOIN p1.interests i2
            WHERE p1.user.id = :userId1
        )
        AND i IN (
            SELECT i3 FROM Profile p2
            JOIN p2.interests i3
            WHERE p2.user.id = :userId2
        )
        """)
    List<Interest> findCommonInterests(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2
    );

    /**
     * Liczy wspólne zainteresowania dla dwóch użytkowników
     */
    @Query("""
        SELECT COUNT(DISTINCT i) FROM Interest i
        WHERE i IN (
            SELECT i2 FROM Profile p1
            JOIN p1.interests i2
            WHERE p1.user.id = :userId1
        )
        AND i IN (
            SELECT i3 FROM Profile p2
            JOIN p2.interests i3
            WHERE p2.user.id = :userId2
        )
        """)
    long countCommonInterests(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2
    );

    /**
     * Znajduje najpopularniejsze zainteresowania (po liczbie użytkowników)
     */
    @Query("""
        SELECT i FROM Interest i
        ORDER BY SIZE(i.profiles) DESC
        """)
    Page<Interest> findPopularInterests(Pageable pageable);

    /**
     * Szuka zainteresowań po nazwie (LIKE)
     */
    @Query("""
        SELECT i FROM Interest i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
        ORDER BY i.name ASC
        """)
    List<Interest> searchByName(@Param("searchText") String searchText);

    /**
     * Szuka zainteresowań po opisie
     */
    @Query("""
        SELECT i FROM Interest i
        WHERE LOWER(i.description) LIKE LOWER(CONCAT('%', :searchText, '%'))
        ORDER BY i.name ASC
        """)
    List<Interest> searchByDescription(@Param("searchText") String searchText);

    /**
     * Liczy wszystkie zainteresowania
     */
    @Query("SELECT COUNT(i) FROM Interest i")
    long countAllInterests();
}

