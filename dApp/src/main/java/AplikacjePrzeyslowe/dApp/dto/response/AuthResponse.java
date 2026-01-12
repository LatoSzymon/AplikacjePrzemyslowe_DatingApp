package AplikacjePrzeyslowe.dApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla odpowiedzi z tokenem JWT po logowaniu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // w sekundach
    private UserResponse user;
}

