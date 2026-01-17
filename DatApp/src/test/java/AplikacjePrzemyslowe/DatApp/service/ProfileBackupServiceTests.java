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
    private InterestRepository interestRepository;
    @Mock
    private PreferenceRepository preferenceRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ObjectMapper jsonMapper;
    @Mock
    private XmlMapper xmlMapper;

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
                .user(testUser)  // zmiana z .userId(1L)
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
                .user(testUser)  // zmiana z .userId(1L)
                .preferredGender(Gender.FEMALE)
                .minAge(25)
                .maxAge(35)
                .maxDistanceKm(50)
                .build();

        testPhoto = Photo.builder()
                .id(1L)
                .profile(testProfile)  // zmiana z .profileId(1L)
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
    void testExportProfileToJson() throws IOException {
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

//    @Test
//    @DisplayName("Powinno wyeksportować profil do XML")
//    void testExportProfileToXml() throws IOException {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
//        when(photoRepository.findByProfileIdOrderByDisplayOrder(1L)).thenReturn(List.of(testPhoto));
//        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
//        when(matchRepository.findByUser1OrUser2(testUser, testUser, Pageable.unpaged()))
//                .thenReturn(new PageImpl<>(List.of(testMatch)));
//        when(messageRepository.findByMatch(testMatch)).thenReturn(List.of(testMessage));
//        when(matchRepository.countByUser1OrUser2(testUser, testUser)).thenReturn(1L);
//        when(messageRepository.countByMatchId(1L)).thenReturn(1L);
//
//        // Act
//        byte[] result = profileBackupService.exportProfileToXml(1L);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.length > 0);
//        verify(userRepository).findById(1L);
//        verify(profileRepository).findByUserId(1L);
//    }
//



    @Test
    @DisplayName("Powinno rzucić ResourceNotFoundException gdy użytkownik nie istnieje")
    void testExportProfileToJson_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            profileBackupService.exportProfileToJson(999L);
        });
    }

    @Test
    @DisplayName("Powinno zaimportować profil z JSON")
    void testImportProfileFromJson() throws IOException {
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
        assertThrows(BackupException.class, () -> {
            profileBackupService.importProfileFromJson(1L, emptyFile);
        });
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
        assertThrows(BackupException.class, () -> {
            profileBackupService.importProfileFromJson(1L, wrongFile);
        });
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
        assertThrows(BackupException.class, () -> {
            profileBackupService.importProfileFromXml(1L, emptyFile);
        });
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
        assertThrows(BackupException.class, () -> {
            profileBackupService.importProfileFromXml(1L, wrongFile);
        });
    }

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
        assertThrows(ResourceNotFoundException.class, () -> {
            profileBackupService.getBackupMetadata(999L);
        });
    }

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
}
