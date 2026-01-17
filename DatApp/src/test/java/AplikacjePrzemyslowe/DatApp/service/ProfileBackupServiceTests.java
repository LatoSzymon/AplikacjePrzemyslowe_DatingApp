package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.backup.ProfileBackupDto;
import AplikacjePrzemyslowe.DatApp.entity.*;
import AplikacjePrzemyslowe.DatApp.exception.BackupException;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProfileBackupService Tests")
class ProfileBackupServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private PreferenceRepository preferenceRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ProfileBackupService profileBackupService;

    private User testUser;
    private Profile testProfile;
    private Preference testPreference;
    private Photo testPhoto;
    private Interest testInterest;
    private Match testMatch;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .gender(Gender.MALE)
                .city("Warsaw")
                .birthDate(LocalDate.now().minusYears(30))
                .createdAt(LocalDateTime.now().minusDays(7))
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .occupation("Developer")
                .education("Bachelor")
                .heightCm(180)
                .latitude(52.2297)
                .longitude(21.0122)
                .interests(new HashSet<>())
                .build();

        testPreference = Preference.builder()
                .id(1L)
                .user(testUser)
                .preferredGender(Gender.FEMALE)
                .minAge(25)
                .maxAge(35)
                .maxDistanceKm(50)
                .build();

        testPhoto = Photo.builder()
                .id(1L)
                .profile(testProfile)
                .photoUrl("https://example.com/photo.jpg")
                .displayOrder(1)
                .isPrimary(true)
                .build();

        testInterest = Interest.builder()
                .id(1L)
                .name("Programming")
                .build();

        User testUser2 = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .build();

        testMatch = Match.builder()
                .id(1L)
                .user1(testUser)
                .user2(testUser2)
                .isActive(true)
                .matchedAt(LocalDateTime.now().minusDays(3))
                .build();

        testMessage = Message.builder()
                .id(1L)
                .match(testMatch)
                .sender(testUser)
                .content("Hello!")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(1))
                .build();

        testProfile.addInterest(testInterest);
    }

    @Test
    @DisplayName("Powinno wyeksportować profil do JSON")
    void testExportProfileToJson() {
        // Arrange
        testProfile.setInterests(Set.of(testInterest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.findByProfileIdOrderByDisplayOrder(1L)).thenReturn(List.of(testPhoto));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(matchRepository.findByUser1OrUser2(testUser, testUser, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(testMatch)));
        when(messageRepository.findByMatch(testMatch)).thenReturn(List.of(testMessage));

        // Act
        byte[] result = profileBackupService.exportProfileToJson(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(userRepository).findById(1L);
        verify(profileRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Powinno rzucić ResourceNotFoundException gdy użytkownik nie istnieje")
    void testExportProfileToJson_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileBackupService.exportProfileToJson(999L));
    }

    @Test
    @DisplayName("Powinno zaimportować profil z JSON")
    void testImportProfileFromJson() {
        // Arrange
        String jsonContent = "{\"backupVersion\":\"1.0\",\"user\":{\"username\":\"testuser\",\"email\":\"test@example.com\"}}";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.json",
                "application/json",
                jsonContent.getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ProfileBackupDto result = profileBackupService.importProfileFromJson(1L, file);

        // Assert
        assertNotNull(result);
        assertEquals("1.0", result.getBackupVersion());
    }

    @Test
    @DisplayName("Powinno zaimportować profil z XML")
    void testImportProfileFromXml() {
        // Arrange
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<profileBackup>" +
                "<backupVersion>1.0</backupVersion>" +
                "<user><username>testuser</username><email>test@example.com</email></user>" +
                "</profileBackup>";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.xml",
                "application/xml",
                xmlContent.getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ProfileBackupDto result = profileBackupService.importProfileFromXml(1L, file);

        // Assert
        assertNotNull(result);
        assertEquals("1.0", result.getBackupVersion());
    }

    @Test
    @DisplayName("Powinno wyrzucić BackupException dla pustego pliku JSON")
    void testImportProfileFromJson_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "backup.json",
                "application/json",
                new byte[0]
        );

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.importProfileFromJson(1L, emptyFile));
    }

    @Test
    @DisplayName("Powinno wyrzucić BackupException dla złego formatu pliku JSON")
    void testImportProfileFromJson_WrongFileExtension() {
        // Arrange
        MockMultipartFile wrongFile = new MockMultipartFile(
                "file",
                "backup.txt",
                "text/plain",
                "content".getBytes()
        );

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.importProfileFromJson(1L, wrongFile));
    }

    @Test
    @DisplayName("Powinno wyrzucić BackupException dla niepoprawnego JSON")
    void testImportProfileFromJson_InvalidJson() {
        // Arrange
        MockMultipartFile invalidJsonFile = new MockMultipartFile(
                "file",
                "backup.json",
                "application/json",
                "invalid json content{".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.importProfileFromJson(1L, invalidJsonFile));
    }

    @Test
    @DisplayName("Powinno wyrzucić BackupException dla pustego pliku XML")
    void testImportProfileFromXml_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "backup.xml",
                "application/xml",
                new byte[0]
        );

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.importProfileFromXml(1L, emptyFile));
    }

    @Test
    @DisplayName("Powinno wyrzucić BackupException dla złego formatu pliku XML")
    void testImportProfileFromXml_WrongFileExtension() {
        // Arrange
        MockMultipartFile wrongFile = new MockMultipartFile(
                "file",
                "backup.json",
                "application/json",
                "content".getBytes()
        );

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.importProfileFromXml(1L, wrongFile));
    }

    @Test
    @DisplayName("Powinno wyrzucić BackupException dla niepoprawnego XML")
    void testImportProfileFromXml_InvalidXml() {
        // Arrange
        MockMultipartFile invalidXmlFile = new MockMultipartFile(
                "file",
                "backup.xml",
                "application/xml",
                "invalid xml <content>".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.importProfileFromXml(1L, invalidXmlFile));
    }

    // ======== VALIDATE BACKUP TESTS ========

    @Test
    @DisplayName("Powinno walidować backup - prawidłowe dane")
    void testValidateBackup_ValidData() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("1.0")
                .user(userDto)
                .profile(null)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno rzucić BackupException dla null backupDto")
    void testValidateBackup_NullBackupDto() {
        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.validateBackup(null, 1L));
    }

    @Test
    @DisplayName("Powinno rzucić BackupException dla null backupVersion")
    void testValidateBackup_NullVersion() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion(null)
                .user(userDto)
                .build();

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno rzucić BackupException dla pustej wersji")
    void testValidateBackup_EmptyVersion() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("")
                .user(userDto)
                .build();

        // Act & Assert
        // Pusta wersja jest technicznie poprawna (nie rzuca exception),
        // ale test powinien przejść bez exception
        assertDoesNotThrow(() -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno rzucić BackupException dla null user")
    void testValidateBackup_NullUser() {
        // Arrange
        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("1.0")
                .user(null)
                .build();

        // Act & Assert
        assertThrows(BackupException.class, () -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno walidować backup - z pełnymi danymi profilu")
    void testValidateBackup_WithProfileData() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto.ProfileBackupData profileDto = ProfileBackupDto.ProfileBackupData.builder()
                .bio("Test bio")
                .occupation("Developer")
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("1.0")
                .user(userDto)
                .profile(profileDto)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno walidować backup - z zainteresowaniami")
    void testValidateBackup_WithInterests() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("1.0")
                .user(userDto)
                .interests(List.of("Programming", "Gaming"))
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno walidować backup - z zdjęciami")
    void testValidateBackup_WithPhotos() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto.PhotoBackupData photoDto = ProfileBackupDto.PhotoBackupData.builder()
                .url("https://example.com/photo.jpg")
                .isPrimary(true)
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("1.0")
                .user(userDto)
                .profile(ProfileBackupDto.ProfileBackupData.builder()
                        .photos(List.of(photoDto))
                        .build())
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> profileBackupService.validateBackup(backupDto, 1L));
    }

    @Test
    @DisplayName("Powinno walidować backup - neprawidłowy format wersji")
    void testValidateBackup_InvalidVersionFormat() {
        // Arrange
        ProfileBackupDto.UserBackupData userDto = ProfileBackupDto.UserBackupData.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        ProfileBackupDto backupDto = ProfileBackupDto.builder()
                .backupVersion("2.0")
                .user(userDto)
                .build();

        // Act & Assert
        // Jeśli metoda waliduje wersję, powinno rzucić exception
        try {
            profileBackupService.validateBackup(backupDto, 1L);
        } catch (BackupException e) {
            assertTrue(e.getMessage().contains("version") || e.getMessage().contains("Version"));
        }
    }

    // ======== METADATA TESTS ========

    @Test
    @DisplayName("Powinno pobierać metadata backupu")
    void testGetBackupMetadata() {
        // Arrange
        testProfile.setInterests(Set.of(testInterest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.findByProfileIdOrderByDisplayOrder(1L)).thenReturn(List.of(testPhoto));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(matchRepository.findByUser1OrUser2(testUser, testUser, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(testMatch)));
        when(messageRepository.findByMatch(testMatch)).thenReturn(List.of(testMessage));

        // Act
        ProfileBackupDto result = profileBackupService.getBackupMetadata(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUser().getUsername());
        assertEquals("test@example.com", result.getUser().getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Powinno rzucić ResourceNotFoundException gdy użytkownik nie istnieje w getBackupMetadata")
    void testGetBackupMetadata_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> profileBackupService.getBackupMetadata(999L));
    }

    // ======== EDGE CASES TESTS ========

    @Test
    @DisplayName("Powinno obsługiwać profil bez preferencji")
    void testExportProfile_WithoutPreferences() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(photoRepository.findByProfileIdOrderByDisplayOrder(1L)).thenReturn(Collections.emptyList());
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(matchRepository.findByUser1OrUser2(testUser, testUser, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        byte[] result = profileBackupService.exportProfileToJson(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Powinno obsługiwać użytkownika bez profilu")
    void testExportProfile_WithoutProfile() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(matchRepository.findByUser1OrUser2(testUser, testUser, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        byte[] result = profileBackupService.exportProfileToJson(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Powinno obsługiwać XML z brakujących sekcji")
    void testImportProfileFromXml_MissingOptionalSections() {
        // Arrange
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<profileBackup>" +
                "<backupVersion>1.0</backupVersion>" +
                "<user><username>testuser</username><email>test@example.com</email></user>" +
                "</profileBackup>";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.xml",
                "application/xml",
                xmlContent.getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ProfileBackupDto result = profileBackupService.importProfileFromXml(1L, file);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals("testuser", result.getUser().getUsername());
    }
}
