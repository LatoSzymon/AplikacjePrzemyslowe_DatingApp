package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dao.UserJdbcDao;
import AplikacjePrzemyslowe.DatApp.dto.response.CandidateResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.InterestResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.PhotoResponse;
import AplikacjePrzemyslowe.DatApp.entity.*;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.SwipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingEngineService Tests")
class MatchingEngineServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PreferenceService preferenceService;
    @Mock
    private ProfileService profileService;
    @Mock
    private InterestService interestService;
    @Mock
    private SwipeRepository swipeRepository;
    @Mock
    private UserJdbcDao userJdbcDao;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MatchingEngineService matchingEngineService;

    private User testUser;
    private User testCandidate;
    private Profile testProfile;
    private Profile candidateProfile;
    private Preference testPreference;
    private Interest testInterest;
    private Photo testPhoto;

    @BeforeEach
    void setUp() {
        // Test user setup
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .isActive(true)
                .build();

        // Test candidate setup
        testCandidate = User.builder()
                .id(2L)
                .username("candidate")
                .email("candidate@example.com")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1996, 6, 20))
                .city("Warsaw")
                .isActive(true)
                .build();

        // Test preference setup
        testPreference = Preference.builder()
                .id(1L)
                .user(testUser)
                .preferredGender(Gender.FEMALE)
                .minAge(25)
                .maxAge(35)
                .maxDistanceKm(50)
                .build();

        // Test interest setup
        testInterest = Interest.builder()
                .id(1L)
                .name("Travel")
                .build();

        // Test profile setup BEFORE photo
        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .occupation("Developer")
                .education("Bachelor")
                .heightCm(180)
                .latitude(52.2297)
                .longitude(21.0122)
                .photos(new HashSet<>())
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        // Test photo setup (po profilu)
        testPhoto = Photo.builder()
                .id(1L)
                .profile(testProfile)
                .photoUrl("https://example.com/photo.jpg")
                .displayOrder(1)
                .isPrimary(true)
                .build();

        // Dodaj zdjęcie do profilu
        testProfile.getPhotos().add(testPhoto);

        // Candidate profile setup
        candidateProfile = Profile.builder()
                .id(2L)
                .user(testCandidate)
                .bio("Candidate bio")
                .occupation("Designer")
                .education("Master")
                .heightCm(165)
                .latitude(52.2297)
                .longitude(21.0122)
                .photos(new HashSet<>(Set.of(testPhoto)))
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();
    }


    // ========== getNextCandidate Tests ==========

    @Test
    @DisplayName("Powinno znaleźć następnego kandydata")
    void testGetNextCandidate_Success() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);

        // Mockuj modelMapper dla PhotoResponse i InterestResponse
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("candidate", result.getUsername());
        assertTrue(result.getCompatibilityScore() >= 0);
        verify(userService).getUserEntity(1L);
        verify(preferenceService).getPreferenceEntity(1L);
    }


    @Test
    @DisplayName("Powinno zwrócić null gdy brak kandydatów")
    void testGetNextCandidate_NoCandidates() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNull(result);
        verify(userService).getUserEntity(1L);
    }

    // ========== getCandidates Tests ==========

    @Test
    @DisplayName("Powinno znaleźć kandydatów z paginacją")
    void testGetCandidates_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        Page<CandidateResponse> result = matchingEngineService.getCandidates(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2L, result.getContent().getFirst().getId());
        verify(userJdbcDao).findCandidatesByPreference(anyLong(), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Powinno zwrócić pustą stronę gdy brak kandydatów")
    void testGetCandidates_Empty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        Page<CandidateResponse> result = matchingEngineService.getCandidates(1L, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinno sortować kandydatów po compatibility score")
    void testGetCandidates_SortedByScore() {
        // Arrange
        User candidate2 = User.builder()
                .id(3L)
                .username("candidate2")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1994, 1, 1))
                .city("Warsaw")
                .isActive(true)
                .build();

        Profile candidate2Profile = Profile.builder()
                .id(3L)
                .user(candidate2)
                .bio("Candidate2 bio")
                .latitude(52.2297)
                .longitude(21.0122)
                .photos(new HashSet<>(Set.of(testPhoto)))
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate, candidate2)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(profileService.getProfileEntity(3L)).thenReturn(candidate2Profile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(3L);
        when(interestService.countCommonInterests(1L, 3L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        Page<CandidateResponse> result = matchingEngineService.getCandidates(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().get(0).getCompatibilityScore()
                >= result.getContent().get(1).getCompatibilityScore());
    }

    // ========== Scoring Algorithm Tests ==========

    @Test
    @DisplayName("Powinno obliczać score na podstawie wspólnych zainteresowań")
    void testScoring_CommonInterests() {
        // Arrange - 3 wspólne zainteresowania = +30 punktów
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(3L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() >= 30); // 3 * 10
    }

    @Test
    @DisplayName("Powinno penalizować dystans")
    void testScoring_Distance() {
        // Arrange - dystans zmniejsza score o 1 punkt za każde 10km
        Profile distantProfile = Profile.builder()
                .id(2L)
                .user(testCandidate)
                .bio("Distant bio")
                .latitude(52.2297 + 0.5) // ~50km dalej
                .longitude(21.0122)
                .photos(new HashSet<>(Set.of(testPhoto)))
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(distantProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getDistanceKm() > 0);
    }

    @Test
    @DisplayName("Powinno dodawać bonus za kompletny profil")
    void testScoring_CompleteProfile() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() >= 20); // Bonus za kompletny profil
    }

    /*
    @Test
    @DisplayName("Powinno penalizować dużą różnicę wieku")
    void testScoring_AgeDifference() {
        // Arrange
        User olderCandidate = User.builder()
                .id(2L)
                .username("older_candidate")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1980, 1, 1))
                .city("Warsaw")
                .isActive(true)
                .build();

        Profile olderProfile = Profile.builder()
                .id(2L)
                .user(olderCandidate)
                .bio("Older bio")
                .latitude(52.2297)
                .longitude(21.0122)
                .photos(new HashSet<>(Set.of(testPhoto)))
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(olderCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(olderProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        // Score = 10 (zainteresowania) + 0 (dystans) + 20 (kompletny profil) - 20 (age penalty) = 10
        assertTrue(result.getCompatibilityScore() >= 0);
    }
    */

    // ========== Distance Calculation Tests ==========

    @Test
    @DisplayName("Powinno obliczać dystans między użytkownikami")
    void testCalculateDistance_SameLocation() {
        // Arrange - ta sama lokalizacja = 0km
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(0L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getDistanceKm() >= 0);
    }

    @Test
    @DisplayName("Powinno zwrócić 50km gdy brak koordynatów")
    void testCalculateDistance_NoCoordinates() {
        // Arrange
        Profile noCoordinatesProfile = Profile.builder()
                .id(2L)
                .user(testCandidate)
                .bio("No coords")
                .latitude(null) // Brak koordynatów
                .longitude(null)
                .photos(new HashSet<>(Set.of(testPhoto)))
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(noCoordinatesProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertEquals(50.0, result.getDistanceKm()); // Default: 50km
    }

    // ========== Profile Completeness Tests ==========

    @Test
    @DisplayName("Powinno rozpoznać kompletny profil")
    void testIsProfileComplete_Complete() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() >= 20); // Bonus za kompletność
    }

    @Test
    @DisplayName("Powinno rozpoznać niekompletny profil (brak bio)")
    void testIsProfileComplete_NoBio() {
        // Arrange
        Profile incompleteProfile = Profile.builder()
                .id(2L)
                .user(testCandidate)
                .bio("") // Pusta bio
                .latitude(52.2297)
                .longitude(21.0122)
                .photos(new HashSet<>(Set.of(testPhoto)))
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(incompleteProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() < 20); // Brak bonusu
    }

    /*
    @Test
    @DisplayName("Powinno rozpoznać niekompletny profil (brak zdjęć)")
    void testIsProfileComplete_NoPhotos() {
        // Arrange
        Profile noPhotosProfile = Profile.builder()
                .id(2L)
                .user(testCandidate)
                .bio("Bio")
                .latitude(52.2297)
                .longitude(21.0122)
                .photos(new HashSet<>())
                .interests(new HashSet<>(Set.of(testInterest)))
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(noPhotosProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() < 20);
    }
    */

    // ========== Age Filter Tests ==========

    @Test
    @DisplayName("Powinno filtrować kandydatów po wieku")
    void testFindEligibleCandidates_AgeFilter() {
        // Arrange
        User tooYoung = User.builder()
                .id(3L)
                .username("too_young")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2010, 1, 1)) // 14 lat
                .city("Warsaw")
                .isActive(true)
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate, tooYoung)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId()); // Powinna być zwrócona tylko testCandidate
    }

    @Test
    @DisplayName("Powinno filtrować kandydatów po aktywności")
    void testFindEligibleCandidates_ActiveFilter() {
        // Arrange
        User inactiveCandidate = User.builder()
                .id(3L)
                .username("inactive")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1996, 1, 1))
                .city("Warsaw")
                .isActive(false) // Nieaktywny
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate, inactiveCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L)).thenReturn(candidateProfile);
        when(interestService.countCommonInterests(1L, 2L)).thenReturn(1L);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        CandidateResponse result = matchingEngineService.getNextCandidate(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId()); // Powinna być zwrócona tylko testCandidate
    }

    // ========== Exception Handling Tests ==========

    @Test
    @DisplayName("Powinno obsługiwać wyjątek ResourceNotFoundException")
    void testGetNextCandidate_ProfileNotFound() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(preferenceService.getPreferenceEntity(1L)).thenReturn(testPreference);
        when(userJdbcDao.findCandidatesByPreference(
                1L, Gender.FEMALE, 25, 35, Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(testCandidate)));
        when(profileService.getProfileEntity(1L)).thenReturn(testProfile);
        when(profileService.getProfileEntity(2L))
                .thenThrow(new ResourceNotFoundException("Profile not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> matchingEngineService.getNextCandidate(1L));
    }
}
