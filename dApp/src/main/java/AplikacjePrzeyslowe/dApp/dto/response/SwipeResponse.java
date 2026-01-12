package AplikacjePrzeyslowe.dApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO dla odpowiedzi po wykonaniu swipe'a.
 * Zawiera informację czy nastąpił match.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwipeResponse {

    private Long swipeId;
    private Long swipedUserId;
    private String swipeType;
    private LocalDateTime swipedAt;

    // Informacja o matchu
    private Boolean isMatch;
    private MatchResponse matchDetails;
}

