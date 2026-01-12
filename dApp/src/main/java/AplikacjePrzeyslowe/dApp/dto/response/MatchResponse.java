package AplikacjePrzeyslowe.dApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO dla odpowiedzi z informacją o dopasowaniu (match).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    private Long id;
    private Long userId;
    private UserResponse partner;
    private ProfileResponse partnerProfile;
    private Boolean isActive;
    private LocalDateTime matchedAt;
    private LocalDateTime unmatchedAt;

    // Dodatkowe info
    private Integer messagesCount;
    private Integer commonInterestsCount;
    private MessageResponse lastMessage;
    private Boolean hasUnreadMessages;
}

