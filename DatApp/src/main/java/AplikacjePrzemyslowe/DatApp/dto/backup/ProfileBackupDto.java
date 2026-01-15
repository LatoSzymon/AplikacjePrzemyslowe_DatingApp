package AplikacjePrzemyslowe.DatApp.dto.backup;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO reprezentujący kompletny backup profilu użytkownika.
 * Zawiera: dane użytkownika, profil, zainteresowania, preferencje, dopasowania i historię wiadomości.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "profileBackup")
public class ProfileBackupDto {

    // Metadata backupu
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime backupCreatedAt;

    private String backupVersion;

    private Long userId;

    // Dane użytkownika
    private UserBackupData user;

    // Dane profilu
    private ProfileBackupData profile;

    // Preferencje
    private PreferenceBackupData preferences;

    // Zainteresowania
    @JacksonXmlElementWrapper(localName = "interests")
    @JacksonXmlProperty(localName = "interest")
    private List<String> interests;

    // Dopasowania
    @JacksonXmlElementWrapper(localName = "matches")
    @JacksonXmlProperty(localName = "match")
    private List<MatchBackupData> matches;

    // Historia wiadomości
    @JacksonXmlElementWrapper(localName = "messages")
    @JacksonXmlProperty(localName = "message")
    private List<MessageBackupData> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBackupData {
        private String username;
        private String email;
        private String gender;
        private Integer age;
        private String city;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileBackupData {
        private String bio;
        private String occupation;
        private String education;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateOfBirth;

        private Double latitude;
        private Double longitude;

        @JacksonXmlElementWrapper(localName = "photos")
        @JacksonXmlProperty(localName = "photo")
        private List<PhotoBackupData> photos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoBackupData {
        private String url;
        private Integer displayOrder;
        private Boolean isPrimary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceBackupData {
        private String preferredGender;
        private Integer minAge;
        private Integer maxAge;
        private Integer maxDistanceKm;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchBackupData {
        private Long matchId;
        private String partnerUsername;
        private Boolean isActive;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime matchedAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime unmatchedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageBackupData {
        private Long matchId;
        private String partnerUsername;
        private String content;
        private Boolean isSentByMe;
        private Boolean isRead;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime sentAt;
    }
}

