package AplikacjePrzeyslowe.dApp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla żądania logowania.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email lub username jest wymagany")
    @Size(max = 100, message = "Login nie może przekraczać 100 znaków")
    private String usernameOrEmail;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, max = 100, message = "Hasło musi mieć od 8 do 100 znaków")
    private String password;
}

