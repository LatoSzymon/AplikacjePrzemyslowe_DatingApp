package AplikacjePrzeyslowe.dApp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla wykonania akcji swipe.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwipeRequest {

    @NotNull(message = "ID użytkownika do swipe'a jest wymagane")
    @Positive(message = "ID użytkownika musi być liczbą dodatnią")
    private Long swipedUserId;

    @NotNull(message = "Typ swipe'a jest wymagany")
    @Pattern(
        regexp = "^(LIKE|DISLIKE|SUPER_LIKE)$",
        message = "Typ swipe'a musi być LIKE, DISLIKE lub SUPER_LIKE"
    )
    private String swipeType;
}

