package AplikacjePrzemyslowe.DatApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO dla odpowiedzi z kandydatem do swipe'a.
 * Zawiera pełne informacje potrzebne do wyświetlenia karty kandydata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {

    private Long id;
    private String username;
    private String gender;
    private Integer age;
    private String city;

    // Informacje z profilu
    private String bio;
    private Integer heightCm;
    private String occupation;
    private String education;

    // Zdjęcia i zainteresowania
    private PhotoResponse mainPhoto;
    private Set<InterestResponse> interests;

    // Metryki kompatybilności
    private Integer commonInterestsCount;
    private Double distanceKm;
    private Integer compatibilityScore; // 0-100
}

