package AplikacjePrzeyslowe.dApp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla wysłania wiadomości.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotNull(message = "ID dopasowania jest wymagane")
    @Positive(message = "ID dopasowania musi być liczbą dodatnią")
    private Long matchId;

    @NotBlank(message = "Treść wiadomości jest wymagana")
    @Size(min = 1, max = 2000, message = "Wiadomość musi mieć od 1 do 2000 znaków")
    private String content;
}

