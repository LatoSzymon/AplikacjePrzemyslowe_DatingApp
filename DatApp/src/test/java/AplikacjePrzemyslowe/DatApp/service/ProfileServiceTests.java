package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.UpdateProfileRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.InterestResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.PhotoResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.ProfileResponse;
import AplikacjePrzemyslowe.DatApp.entity.*;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.exception.ValidationException;
import AplikacjePrzemyslowe.DatApp.repository.InterestRepository;
import AplikacjePrzemyslowe.DatApp.repository.PhotoRepository;
import AplikacjePrzemyslowe.DatApp.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTests {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private Profile testProfile;
    private Photo testPhoto;
    private Interest testInterest;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .isActive(true)
                .build();

        // Test interest
        testInterest = Interest.builder()
                .id(1L)
                .name("Travel")
                .build();

        // Test profile
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

        // Test photo
        testPhoto = Photo.builder()
                .id(1L)
                .profile(testProfile)
                .photoUrl("https://example.com/photo.jpg")
                .displayOrder(1)
                .isPrimary(true)
                .build();

        testProfile.getPhotos().add(testPhoto);

        // Update request
        updateRequest = UpdateProfileRequest.builder()
                .bio("Updated bio")
                .occupation("Senior Developer")
                .education("Master")
                .heightCm(180)
                .latitude(52.2297)
                .longitude(21.0122)
                .interestIds(Set.of(1L))
                .build();
    }

    // ========== READ OPERATIONS ==========

    @Test
    @DisplayName("Powinno znaleźć profil po userId")
    void testGetProfileByUserId_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(new ProfileResponse());
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.getProfileByUserId(1L);

        // Assert
        assertNotNull(result);
        verify(profileRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy profil nie istnieje")
    void testGetProfileByUserId_NotFound() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfileByUserId(1L));
    }

    @Test
    @DisplayName("Powinno sprawdzić czy profil istnieje")
    void testExistsByUserId_True() {
        // Arrange
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        // Act
        boolean result = profileService.existsByUserId(1L);

        // Assert
        assertTrue(result);
        verify(profileRepository).existsByUserId(1L);
    }

    @Test
    @DisplayName("Powinno sprawdzić czy profil nie istnieje")
    void testExistsByUserId_False() {
        // Arrange
        when(profileRepository.existsByUserId(1L)).thenReturn(false);

        // Act
        boolean result = profileService.existsByUserId(1L);

        // Assert
        assertFalse(result);
    }

    // ========== CREATE OPERATIONS ==========

    @Test
    @DisplayName("Powinno utworzyć profil")
    void testCreateProfile_Success() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(new ProfileResponse());
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.createProfile(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy profil już istnieje")
    void testCreateProfile_AlreadyExists() {
        // Arrange
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> profileService.createProfile(1L, updateRequest));
    }

    @Test
    @DisplayName("Powinno utworzyć profil bez zainteresowań")
    void testCreateProfile_WithoutInterests() {
        // Arrange
        UpdateProfileRequest requestWithoutInterests = UpdateProfileRequest.builder()
                .bio("Test bio")
                .occupation("Developer")
                .build();

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(new ProfileResponse());
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.createProfile(1L, requestWithoutInterests);

        // Assert
        assertNotNull(result);
        verify(profileRepository).save(any(Profile.class));
        verify(interestRepository, never()).findById(anyLong());
    }

    // ========== UPDATE OPERATIONS ==========

    @Test
    @DisplayName("Powinno zaktualizować profil")
    void testUpdateProfile_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(new ProfileResponse());
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.updateProfile(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Powinno zaktualizować tylko podane pola")
    void testUpdateProfile_PartialUpdate() {
        // Arrange
        UpdateProfileRequest partialRequest = UpdateProfileRequest.builder()
                .bio("New bio only")
                .build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(new ProfileResponse());
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.updateProfile(1L, partialRequest);

        // Assert
        assertNotNull(result);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy profil do aktualizacji nie istnieje")
    void testUpdateProfile_NotFound() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileService.updateProfile(1L, updateRequest));
    }

    // ========== PHOTO OPERATIONS ==========

    @Test
    @DisplayName("Powinno dodać zdjęcie do profilu")
    void testAddPhoto_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);
        when(modelMapper.map(testPhoto, PhotoResponse.class)).thenReturn(new PhotoResponse());

        // Act
        PhotoResponse result = profileService.addPhoto(1L, "https://example.com/photo.jpg", true, 1);

        // Assert
        assertNotNull(result);
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    @DisplayName("Powinno ustawić zdjęcie jako główne")
    void testAddPhoto_SetPrimary() {
        // Arrange
        Photo otherPhoto = Photo.builder()
                .id(2L)
                .profile(testProfile)
                .photoUrl("https://example.com/photo2.jpg")
                .isPrimary(true)
                .build();
        testProfile.getPhotos().add(otherPhoto);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);
        when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());

        // Act
        PhotoResponse result = profileService.addPhoto(1L, "https://example.com/new.jpg", true, 2);

        // Assert
        assertNotNull(result);
        assertFalse(otherPhoto.getIsPrimary()); // Poprzednie główne zdjęcie powinno być odznaczone
    }

    @Test
    @DisplayName("Powinno dodać zdjęcie z domyślnymi wartościami")
    void testAddPhoto_WithDefaults() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);
        when(modelMapper.map(testPhoto, PhotoResponse.class)).thenReturn(new PhotoResponse());

        // Act
        PhotoResponse result = profileService.addPhoto(1L, "https://example.com/photo.jpg", null, null);

        // Assert
        assertNotNull(result);
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    @DisplayName("Powinno usunąć zdjęcie z profilu")
    void testRemovePhoto_Success() {
        // Arrange
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        // Act
        profileService.removePhoto(1L, 1L);

        // Assert
        verify(photoRepository).delete(testPhoto);
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy zdjęcie nie należy do użytkownika")
    void testRemovePhoto_NotOwner() {
        // Arrange
        User otherUser = User.builder().id(2L).build();
        Profile otherProfile = Profile.builder().id(2L).user(otherUser).build();
        Photo otherPhoto = Photo.builder().id(1L).profile(otherProfile).build();

        when(photoRepository.findById(1L)).thenReturn(Optional.of(otherPhoto));

        // Act & Assert
        assertThrows(ValidationException.class, () -> profileService.removePhoto(1L, 1L));
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy zdjęcie nie istnieje")
    void testRemovePhoto_NotFound() {
        // Arrange
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileService.removePhoto(1L, 1L));
    }

    @Test
    @DisplayName("Powinno ustawić zdjęcie jako główne")
    void testSetPrimaryPhoto_Success() {
        // Arrange
        Photo otherPhoto = Photo.builder()
                .id(2L)
                .profile(testProfile)
                .isPrimary(true)
                .build();
        testProfile.getPhotos().add(otherPhoto);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(testPhoto)).thenReturn(testPhoto);

        // Act
        profileService.setPrimaryPhoto(1L, 1L);

        // Assert
        verify(photoRepository).save(testPhoto);
        assertFalse(otherPhoto.getIsPrimary());
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy zdjęcie nie należy do użytkownika przy ustawianiu głównego")
    void testSetPrimaryPhoto_NotOwner() {
        // Arrange
        Profile otherProfile = Profile.builder().id(2L).build();
        Photo otherPhoto = Photo.builder().id(1L).profile(otherProfile).build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.findById(1L)).thenReturn(Optional.of(otherPhoto));

        // Act & Assert
        assertThrows(ValidationException.class, () -> profileService.setPrimaryPhoto(1L, 1L));
    }

    // ========== INTEREST OPERATIONS ==========

    @Test
    @DisplayName("Powinno dodać zainteresowania do profilu")
    void testAddInterests_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(new ProfileResponse());
        lenient().when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class))).thenReturn(new PhotoResponse());
        lenient().when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.addInterests(1L, Set.of(1L));

        // Assert
        assertNotNull(result);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Powinno usunąć zainteresowanie z profilu")
    void testRemoveInterest_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        ProfileResponse mockResponse = new ProfileResponse();
        when(modelMapper.map(testProfile, ProfileResponse.class)).thenReturn(mockResponse);

        // Profil ma zdjęcia, więc mockuj mapowanie zdjęć
        lenient().when(modelMapper.map(any(Photo.class), eq(PhotoResponse.class)))
                .thenReturn(new PhotoResponse());

        // Profil może nie mieć zainteresowań po usunięciu, więc użyj lenient()
        lenient().when(modelMapper.map(any(Interest.class), eq(InterestResponse.class)))
                .thenReturn(new InterestResponse());

        // Act
        ProfileResponse result = profileService.removeInterest(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy zainteresowanie nie istnieje")
    void testAddInterests_InterestNotFound() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(interestRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileService.addInterests(1L, Set.of(1L)));
    }


    @Test
    @DisplayName("Powinno wyczyścić wszystkie zainteresowania")
    void testClearInterests_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        profileService.clearInterests(1L);

        // Assert
        verify(profileRepository).save(any(Profile.class));
        assertTrue(testProfile.getInterests().isEmpty());
    }

    // ========== HELPER METHODS ==========

    @Test
    @DisplayName("Powinno zwrócić encję Profile")
    void testGetProfileEntity_Success() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        // Act
        Profile result = profileService.getProfileEntity(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProfile.getId(), result.getId());
        verify(profileRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek gdy encja Profile nie istnieje")
    void testGetProfileEntity_NotFound() {
        // Arrange
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfileEntity(1L));
    }
}
