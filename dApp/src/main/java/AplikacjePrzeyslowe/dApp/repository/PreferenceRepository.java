package AplikacjePrzeyslowe.dApp.repository;

import AplikacjePrzeyslowe.dApp.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository dla encji Preference.
 */
@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje preferencje użytkownika
     */
    Optional<Preference> findByUserId(Long userId);

    /**
     * Sprawdza czy użytkownik ma preferencje
     */
    boolean existsByUserId(Long userId);

    // ========== CUSTOM QUERIES ==========

    /**
     * Znajduje użytkowników szukających określonej płci
     */
    @Query("""
        SELECT COUNT(p) FROM Preference p
        WHERE p.preferredGender = :gender
        """)
    long countUsersByPreferredGender(@Param("gender") String gender);

    /**
     * Oblicza średni preferowany wiek
     */
    @Query("""
        SELECT AVG((p.minAge + p.maxAge) / 2) FROM Preference p
        """)
    Double getAveragePreferredAge();

    /**
     * Oblicza średnią maksymalną odległość
     */
    @Query("""
        SELECT AVG(p.maxDistanceKm) FROM Preference p
        """)
    Double getAverageMaxDistance();

    /**
     * Liczy użytkowników szukających w określonym zakresie wieku
     */
    @Query("""
        SELECT COUNT(p) FROM Preference p
        WHERE p.minAge <= :maxAge
        AND p.maxAge >= :minAge
        """)
    long countUsersInAgeRange(
        @Param("minAge") int minAge,
        @Param("maxAge") int maxAge
    );

    /**
     * Liczy użytkowników szukających w określonym promieniu
     */
    @Query("""
        SELECT COUNT(p) FROM Preference p
        WHERE p.maxDistanceKm >= :minDistance
        """)
    long countUsersWithinDistance(@Param("minDistance") int minDistance);
}

