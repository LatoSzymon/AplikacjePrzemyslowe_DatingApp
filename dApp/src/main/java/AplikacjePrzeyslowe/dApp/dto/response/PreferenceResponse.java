package AplikacjePrzeyslowe.dApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla odpowiedzi z preferencjami wyszukiwania.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponse {

    private Long id;
    private Long userId;
    private String preferredGender;
    private Integer minAge;
    private Integer maxAge;
    private Integer maxDistanceKm;
}

