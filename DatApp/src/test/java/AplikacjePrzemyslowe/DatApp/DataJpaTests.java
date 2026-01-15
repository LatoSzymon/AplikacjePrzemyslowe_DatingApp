package AplikacjePrzemyslowe.DatApp;

import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.Interest;
import AplikacjePrzemyslowe.DatApp.entity.Profile;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.repository.InterestRepository;
import AplikacjePrzemyslowe.DatApp.repository.ProfileRepository;
import AplikacjePrzemyslowe.DatApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("JPA Repository CRUD Tests")
class DataJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private InterestRepository interestRepository;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword123")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .isActive(true)
                .build();

        testProfile = Profile.builder()
                .user(testUser)
                .bio("Test bio")
                .heightCm(180)
                .occupation("Engineer")
                .education("University")
                .latitude(52.2297)
                .longitude(21.0122)
                .build();

        testUser.setProfile(testProfile);
    }

    @Test
    @DisplayName("1. Save user and find by ID")
    void testSaveAndFindUserById() {
        User saved = userRepository.save(testUser);
        assertThat(saved.getId()).isNotNull();

        User found = userRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("2. Find user by email")
    void testFindUserByEmail() {
        userRepository.save(testUser);

        User found = userRepository.findByEmail("test@example.com").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("3. Find user by username")
    void testFindUserByUsername() {
        userRepository.save(testUser);

        User found = userRepository.findByUsername("testuser").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("4. Find all active users with pagination")
    void testFindActiveUsersWithPagination() {
        userRepository.save(testUser);

        // Create inactive user WITHOUT profile (to avoid cascade validation)
        User inactiveUser = User.builder()
                .username("inactive")
                .email("inactive@example.com")
                .password("hashed123")  // min 8 characters for validation
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1998, 3, 20))
                .city("Krakow")
                .isActive(false)
                .build();
        // Don't set profile - leave it null

        userRepository.save(inactiveUser);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> active = userRepository.findByIsActiveTrue(pageable);

        assertThat(active.getContent()).hasSize(1);
        assertThat(active.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("5. Update user email")
    void testUpdateUserEmail() {
        User saved = userRepository.save(testUser);

        saved.setEmail("newemail@example.com");
        User updated = userRepository.save(saved);

        User found = userRepository.findById(updated.getId()).orElse(null);
        assertThat(found.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    @DisplayName("6. Delete user by ID")
    void testDeleteUserById() {
        User saved = userRepository.save(testUser);
        Long id = saved.getId();

        userRepository.deleteById(id);

        assertThat(userRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("7. Check user exists by email")
    void testExistsByEmail() {
        userRepository.save(testUser);

        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("8. Check user exists by username")
    void testExistsByUsername() {
        userRepository.save(testUser);

        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("9. Save and find profile with cascade")
    void testSaveProfileWithCascade() {
        userRepository.save(testUser);

        Profile found = profileRepository.findByUserId(testUser.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getBio()).isEqualTo("Test bio");
    }

    @Test
    @DisplayName("10. Delete user cascades profile deletion")
    void testUserDeleteCascades() {
        User saved = userRepository.save(testUser);
        Long userId = saved.getId();

        assertThat(profileRepository.findByUserId(userId)).isPresent();

        userRepository.deleteById(userId);

        assertThat(profileRepository.findByUserId(userId)).isEmpty();
    }

    @Test
    @DisplayName("11. Save and find interests")
    void testSaveAndFindInterests() {
        Interest interest = Interest.builder()
                .name("Hiking")
                .category("Sports")
                .description("Mountain hiking")
                .build();

        Interest saved = interestRepository.save(interest);
        Interest found = interestRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Hiking");
    }

    @Test
    @DisplayName("12. Add interests to profile")
    void testAddInterestsToProfile() {
        Interest hiking = Interest.builder().name("Hiking").build();
        Interest reading = Interest.builder().name("Reading").build();

        interestRepository.save(hiking);
        interestRepository.save(reading);

        User saved = userRepository.save(testUser);
        Profile profile = saved.getProfile();
        profile.addInterest(hiking);
        profile.addInterest(reading);

        profileRepository.save(profile);

        Profile found = profileRepository.findById(profile.getId()).orElse(null);
        assertThat(found.getInterests()).hasSize(2);
    }
}


