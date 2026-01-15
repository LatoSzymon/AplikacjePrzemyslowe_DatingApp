package AplikacjePrzemyslowe.DatApp.repository;

import AplikacjePrzemyslowe.DatApp.entity.Photo;
import AplikacjePrzemyslowe.DatApp.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository dla encji Photo.
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje wszystkie zdjęcia profilu
     */
    List<Photo> findByProfileIdOrderByDisplayOrder(Long profileId);

    /**
     * Znajduje główne zdjęcie profilu
     */
    Optional<Photo> findByProfileIdAndIsPrimaryTrue(Long profileId);

    /**
     * Znajduje liczbę zdjęć dla danego profilu
     */
    long countByProfileId(Long profileId);

    /**
     * Sprawdza czy główne zdjęcie istnieje dla profilu
     */
    boolean existsByProfileIdAndIsPrimaryTrue(Long profileId);

    // ========== CUSTOM QUERIES ==========

    /**
     * Znajduje wszystkie zdjęcia profilu z paginacją
     */
    @Query("""
        SELECT p FROM Photo p
        WHERE p.profile.id = :profileId
        ORDER BY p.displayOrder ASC, p.uploadedAt DESC
        """)
    Page<Photo> findByProfileId(@Param("profileId") Long profileId, Pageable pageable);

    /**
     * Znajduje zdjęcia profilu uploaded w ostatnich dniach
     */
    @Query("""
        SELECT p FROM Photo p
        WHERE p.profile.id = :profileId
        AND p.uploadedAt >= CURRENT_TIMESTAMP - :days DAY
        ORDER BY p.uploadedAt DESC
        """)
    List<Photo> findRecentPhotosByProfileId(
        @Param("profileId") Long profileId,
        @Param("days") int days
    );

    /**
     * Liczy zdjęcia dla danego profilu
     */
    @Query("""
        SELECT COUNT(p) FROM Photo p
        WHERE p.profile.id = :profileId
        """)
    long countPhotosByProfileId(@Param("profileId") Long profileId);

    /**
     * Znajduje wszystkie profile które mają co najmniej 1 zdjęcie
     */
    @Query("""
        SELECT DISTINCT p.profile FROM Photo p
        WHERE p.profile IS NOT NULL
        """)
    Page<Profile> findAllProfilesWithPhotos(Pageable pageable);

    /**
     * Szuka zdjęć po URL (dla weryfikacji duplikatów)
     */
    Optional<Photo> findByPhotoUrl(String photoUrl);
}

