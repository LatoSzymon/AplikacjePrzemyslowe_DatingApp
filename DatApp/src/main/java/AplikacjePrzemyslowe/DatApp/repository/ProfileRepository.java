package AplikacjePrzemyslowe.DatApp.repository;

import AplikacjePrzemyslowe.DatApp.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository dla encji Profile.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje profil po ID użytkownika
     */
    Optional<Profile> findByUserId(Long userId);

    /**
     * Sprawdza czy profil użytkownika istnieje
     */
    boolean existsByUserId(Long userId);

    /**
     * Liczy profile dla danego użytkownika
     */
    long countByUserId(Long userId);

    /**
     * Znajduje profile które mają bio
     */
    Page<Profile> findByBioNotNull(Pageable pageable);

    /**
     * Znajduje profile które nie mają zdjęć
     */
    @Query("""
        SELECT p FROM Profile p
        WHERE p.photos IS EMPTY
        """)
    Page<Profile> findProfilesWithoutPhotos(Pageable pageable);

    /**
     * Znajduje profile z kompletną informacją
     */
    @Query("""
        SELECT p FROM Profile p
        WHERE p.bio IS NOT NULL
        AND p.photos IS NOT EMPTY
        """)
    Page<Profile> findCompleteProfiles(Pageable pageable);

    /**
     * Znajduje profile o określonym zawodzie
     */
    Page<Profile> findByOccupation(String occupation, Pageable pageable);

    /**
     * Znajduje profile o określonym wykształceniu
     */
    Page<Profile> findByEducation(String education, Pageable pageable);

    /**
     * Znajduje profile które mają zdjęcia
     */
    @Query("""
        SELECT DISTINCT p FROM Profile p
        WHERE p.photos IS NOT EMPTY
        """)
    Page<Profile> findProfilesWithPhotos(Pageable pageable);

    /**
     * Liczy profile ze zdjęciami
     */
    @Query("""
        SELECT COUNT(DISTINCT p) FROM Profile p
        WHERE p.photos IS NOT EMPTY
        """)
    long countProfilesWithPhotos();

    /**
     * Znajduje profile z zainteresowaniami
     */
    @Query("""
        SELECT p FROM Profile p
        WHERE p.interests IS NOT EMPTY
        """)
    Page<Profile> findProfilesWithInterests(Pageable pageable);

    /**
     * Szuka profile po zawartości bio (zawieraniu tekstu)
     */
    @Query("""
        SELECT p FROM Profile p
        WHERE LOWER(p.bio) LIKE LOWER(CONCAT('%', :searchText, '%'))
        """)
    Page<Profile> searchByBio(@Param("searchText") String searchText, Pageable pageable);
}

