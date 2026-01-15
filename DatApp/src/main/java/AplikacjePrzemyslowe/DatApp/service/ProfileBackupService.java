package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.backup.ProfileBackupDto;
import AplikacjePrzemyslowe.DatApp.entity.*;
import AplikacjePrzemyslowe.DatApp.exception.BackupException;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis do zarządzania backupem profili użytkowników.
 * Implementuje eksport do JSON/XML i import z walidacją.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileBackupService {

    private static final String BACKUP_VERSION = "1.0";

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final InterestRepository interestRepository;
    private final PreferenceRepository preferenceRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;

    /**
     * Eksportuje profil użytkownika do formatu JSON.
     *
     * @param userId ID użytkownika
     * @return Backup jako byte array w formacie JSON
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     * @throws BackupException gdy eksport nie powiedzie się
     */
    @Transactional(readOnly = true)
    public byte[] exportProfileToJson(Long userId) {
        log.info("Exporting profile to JSON for user {}", userId);

        ProfileBackupDto backup = createBackup(userId);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mapper.writeValue(outputStream, backup);

            byte[] result = outputStream.toByteArray();
            log.info("Successfully exported profile to JSON for user {}. Size: {} bytes", userId, result.length);

            return result;
        } catch (IOException e) {
            log.error("Failed to export profile to JSON for user {}", userId, e);
            throw new BackupException("Failed to export profile to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Eksportuje profil użytkownika do formatu XML.
     *
     * @param userId ID użytkownika
     * @return Backup jako byte array w formacie XML
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     * @throws BackupException gdy eksport nie powiedzie się
     */
    @Transactional(readOnly = true)
    public byte[] exportProfileToXml(Long userId) {
        log.info("Exporting profile to XML for user {}", userId);

        ProfileBackupDto backup = createBackup(userId);

        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mapper.writeValue(outputStream, backup);

            byte[] result = outputStream.toByteArray();
            log.info("Successfully exported profile to XML for user {}. Size: {} bytes", userId, result.length);

            return result;
        } catch (IOException e) {
            log.error("Failed to export profile to XML for user {}", userId, e);
            throw new BackupException("Failed to export profile to XML: " + e.getMessage(), e);
        }
    }

    /**
     * Importuje profil z pliku JSON.
     * UWAGA: To jest operation read-only - parsuje dane ale NIE zapisuje do bazy.
     *
     * @param userId ID użytkownika
     * @param file Plik JSON z backupem
     * @return Sparsowany backup DTO
     * @throws BackupException gdy import nie powiedzie się
     */
    @Transactional
    public ProfileBackupDto importProfileFromJson(Long userId, MultipartFile file) {
        log.info("Importing profile from JSON for user {}", userId);

        if (file.isEmpty()) {
            throw new BackupException("Uploaded file is empty");
        }

        if (!file.getOriginalFilename().endsWith(".json")) {
            throw new BackupException("File must be in JSON format (.json extension)");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ProfileBackupDto backup = mapper.readValue(file.getInputStream(), ProfileBackupDto.class);

            // Walidacja backupu
            validateBackup(backup, userId);

            log.info("Successfully imported profile from JSON for user {}", userId);
            log.debug("Backup contains: {} interests, {} matches, {} messages",
                    backup.getInterests() != null ? backup.getInterests().size() : 0,
                    backup.getMatches() != null ? backup.getMatches().size() : 0,
                    backup.getMessages() != null ? backup.getMessages().size() : 0);

            return backup;

        } catch (IOException e) {
            log.error("Failed to parse JSON backup file for user {}", userId, e);
            throw new BackupException("Failed to parse JSON file: " + e.getMessage(), e);
        }
    }

    /**
     * Importuje profil z pliku XML.
     * UWAGA: To jest operation read-only - parsuje dane ale NIE zapisuje do bazy.
     *
     * @param userId ID użytkownika
     * @param file Plik XML z backupem
     * @return Sparsowany backup DTO
     * @throws BackupException gdy import nie powiedzie się
     */
    @Transactional
    public ProfileBackupDto importProfileFromXml(Long userId, MultipartFile file) {
        log.info("Importing profile from XML for user {}", userId);

        if (file.isEmpty()) {
            throw new BackupException("Uploaded file is empty");
        }

        if (!file.getOriginalFilename().endsWith(".xml")) {
            throw new BackupException("File must be in XML format (.xml extension)");
        }

        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());

            ProfileBackupDto backup = mapper.readValue(file.getInputStream(), ProfileBackupDto.class);

            // Walidacja backupu
            validateBackup(backup, userId);

            log.info("Successfully imported profile from XML for user {}", userId);
            log.debug("Backup contains: {} interests, {} matches, {} messages",
                    backup.getInterests() != null ? backup.getInterests().size() : 0,
                    backup.getMatches() != null ? backup.getMatches().size() : 0,
                    backup.getMessages() != null ? backup.getMessages().size() : 0);

            return backup;

        } catch (IOException e) {
            log.error("Failed to parse XML backup file for user {}", userId, e);
            throw new BackupException("Failed to parse XML file: " + e.getMessage(), e);
        }
    }

    /**
     * Tworzy kompletny backup profilu użytkownika.
     *
     * @param userId ID użytkownika
     * @return DTO z pełnym backupem
     */
    private ProfileBackupDto createBackup(Long userId) {
        log.debug("Creating backup for user {}", userId);

        // Pobierz użytkownika
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Pobierz profil
        Profile profile = profileRepository.findByUserId(userId)
                .orElse(null);

        // Pobierz preferencje
        Preference preferences = preferenceRepository.findByUserId(userId)
                .orElse(null);

        // Zbuduj backup DTO
        ProfileBackupDto.ProfileBackupData profileData = null;
        if (profile != null) {
            // Pobierz zdjęcia
            List<Photo> photos = photoRepository.findByProfileIdOrderByDisplayOrder(profile.getId());

            profileData = ProfileBackupDto.ProfileBackupData.builder()
                    .bio(profile.getBio())
                    .occupation(profile.getOccupation())
                    .education(profile.getEducation())
                    .dateOfBirth(user.getBirthDate()) // birthDate jest w User, nie Profile
                    .latitude(profile.getLatitude())
                    .longitude(profile.getLongitude())
                    .photos(photos.stream()
                            .map(photo -> ProfileBackupDto.PhotoBackupData.builder()
                                    .url(photo.getPhotoUrl()) // Używamy photoUrl
                                    .displayOrder(photo.getDisplayOrder())
                                    .isPrimary(photo.getIsPrimary())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

        // Pobierz zainteresowania
        List<String> interests = profile != null && profile.getInterests() != null
                ? profile.getInterests().stream()
                        .map(Interest::getName)
                        .collect(Collectors.toList())
                : List.of();

        // Pobierz dopasowania
        List<Match> matches = matchRepository.findByUser1OrUser2(user, user, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        List<ProfileBackupDto.MatchBackupData> matchesData = matches.stream()
                .map(match -> {
                    User partner = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();
                    return ProfileBackupDto.MatchBackupData.builder()
                            .matchId(match.getId())
                            .partnerUsername(partner.getUsername())
                            .isActive(match.getIsActive())
                            .matchedAt(match.getMatchedAt())
                            .unmatchedAt(match.getUnmatchedAt())
                            .build();
                })
                .collect(Collectors.toList());

        // Pobierz wiadomości ze wszystkich matchy
        List<ProfileBackupDto.MessageBackupData> messagesData = matches.stream()
                .flatMap(match -> {
                    List<Message> messages = messageRepository.findByMatch(match);
                    User partner = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();

                    return messages.stream()
                            .map(msg -> ProfileBackupDto.MessageBackupData.builder()
                                    .matchId(match.getId())
                                    .partnerUsername(partner.getUsername())
                                    .content(msg.getContent())
                                    .isSentByMe(msg.getSender().getId().equals(userId))
                                    .isRead(msg.getIsRead())
                                    .sentAt(msg.getSentAt())
                                    .build());
                })
                .collect(Collectors.toList());

        // Preferencje
        ProfileBackupDto.PreferenceBackupData preferencesData = null;
        if (preferences != null) {
            preferencesData = ProfileBackupDto.PreferenceBackupData.builder()
                    .preferredGender(preferences.getPreferredGender().name())
                    .minAge(preferences.getMinAge())
                    .maxAge(preferences.getMaxAge())
                    .maxDistanceKm(preferences.getMaxDistanceKm())
                    .build();
        }

        // Zbuduj finalny backup
        return ProfileBackupDto.builder()
                .backupCreatedAt(LocalDateTime.now())
                .backupVersion(BACKUP_VERSION)
                .userId(userId)
                .user(ProfileBackupDto.UserBackupData.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .gender(user.getGender().name())
                        .age(user.getAge())
                        .city(user.getCity())
                        .createdAt(user.getCreatedAt())
                        .build())
                .profile(profileData)
                .preferences(preferencesData)
                .interests(interests)
                .matches(matchesData)
                .messages(messagesData)
                .build();
    }

    /**
     * Waliduje backup przed importem.
     *
     * @param backup Backup do walidacji
     * @param userId ID użytkownika
     * @throws BackupException gdy backup jest nieprawidłowy
     */
    private void validateBackup(ProfileBackupDto backup, Long userId) {
        log.debug("Validating backup for user {}", userId);

        if (backup == null) {
            throw new BackupException("Backup data is null");
        }

        if (backup.getUser() == null) {
            throw new BackupException("Backup must contain user data");
        }

        if (backup.getBackupVersion() == null) {
            throw new BackupException("Backup version is missing");
        }

        if (!BACKUP_VERSION.equals(backup.getBackupVersion())) {
            log.warn("Backup version mismatch. Expected: {}, Got: {}", BACKUP_VERSION, backup.getBackupVersion());
            // Nie rzucamy exception - pozwalamy na import starszych wersji
        }

        // Walidacja danych użytkownika
        if (backup.getUser().getUsername() == null || backup.getUser().getUsername().trim().isEmpty()) {
            throw new BackupException("Username is required in backup");
        }

        if (backup.getUser().getEmail() == null || backup.getUser().getEmail().trim().isEmpty()) {
            throw new BackupException("Email is required in backup");
        }

        log.debug("Backup validation passed for user {}", userId);
    }

    /**
     * Pobiera informacje o backupie (metadata) bez pełnych danych.
     *
     * @param userId ID użytkownika
     * @return Metadata backupu
     */
    @Transactional(readOnly = true)
    public ProfileBackupDto getBackupMetadata(Long userId) {
        log.debug("Fetching backup metadata for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Profile profile = profileRepository.findByUserId(userId).orElse(null);

        int interestCount = profile != null && profile.getInterests() != null
                ? profile.getInterests().size()
                : 0;

        long matchCount = matchRepository.countByUser1OrUser2(user, user);

        // Policz wiadomości
        List<Match> matches = matchRepository.findByUser1OrUser2(user, user, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        long messageCount = matches.stream()
                .mapToLong(match -> messageRepository.countByMatchId(match.getId()))
                .sum();

        return ProfileBackupDto.builder()
                .backupVersion(BACKUP_VERSION)
                .userId(userId)
                .user(ProfileBackupDto.UserBackupData.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .interests(List.of(String.format("%d interests", interestCount)))
                .matches(List.of(ProfileBackupDto.MatchBackupData.builder()
                        .matchId(matchCount)
                        .build()))
                .messages(List.of(ProfileBackupDto.MessageBackupData.builder()
                        .matchId(messageCount)
                        .build()))
                .build();
    }
}

