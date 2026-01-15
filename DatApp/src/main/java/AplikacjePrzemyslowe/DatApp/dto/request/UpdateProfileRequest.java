package AplikacjePrzemyslowe.DatApp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO dla aktualizacji profilu użytkownika.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 1000, message = "Bio nie może przekraczać 1000 znaków")
    private String bio;

    @Min(value = 150, message = "Wzrost musi być co najmniej 150 cm")
    @Max(value = 250, message = "Wzrost nie może przekraczać 250 cm")
    private Integer heightCm;

    @Size(max = 100, message = "Zawód nie może przekraczać 100 znaków")
    private String occupation;

    @Size(max = 100, message = "Wykształcenie nie może przekraczać 100 znaków")
    private String education;

    @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być między -90 a 90")
    @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być między -90 a 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być między -180 a 180")
    @DecimalMax(value = "180.0", message = "Długość geograficzna musi być między -180 a 180")
    private Double longitude;

    @Size(max = 10, message = "Możesz wybrać maksymalnie 10 zainteresowań")
    private Set<@NotNull @Positive Long> interestIds;
}

