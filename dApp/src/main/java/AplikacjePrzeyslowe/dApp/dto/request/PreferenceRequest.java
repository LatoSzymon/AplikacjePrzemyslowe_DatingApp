package AplikacjePrzeyslowe.dApp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla ustawień preferencji wyszukiwania.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceRequest {

    @NotNull(message = "Preferowana płeć jest wymagana")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Preferowana płeć musi być MALE, FEMALE lub OTHER")
    private String preferredGender;

    @NotNull(message = "Minimalny wiek jest wymagany")
    @Min(value = 18, message = "Minimalny wiek musi być co najmniej 18")
    @Max(value = 100, message = "Minimalny wiek nie może przekraczać 100")
    private Integer minAge;

    @NotNull(message = "Maksymalny wiek jest wymagany")
    @Min(value = 18, message = "Maksymalny wiek musi być co najmniej 18")
    @Max(value = 100, message = "Maksymalny wiek nie może przekraczać 100")
    private Integer maxAge;

    @NotNull(message = "Maksymalna odległość jest wymagana")
    @Min(value = 1, message = "Maksymalna odległość musi być co najmniej 1 km")
    @Max(value = 500, message = "Maksymalna odległość nie może przekraczać 500 km")
    private Integer maxDistanceKm;

    /**
     * Walidacja biznesowa: minAge <= maxAge
     */
    @AssertTrue(message = "Minimalny wiek nie może być większy niż maksymalny")
    public boolean isAgeRangeValid() {
        if (minAge == null || maxAge == null) {
            return true; // Inne walidacje to obsłużą
        }
        return minAge <= maxAge;
    }
}

