package AplikacjePrzeyslowe.dApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO dla odpowiedzi z danymi profilu użytkownika.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String bio;
    private Integer heightCm;
    private String occupation;
    private String education;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Zagnieżdżone kolekcje
    private List<PhotoResponse> photos;
    private Set<InterestResponse> interests;

    // Flagi statusu
    private Boolean isComplete;
    private Integer photosCount;
    private Integer interestsCount;
}

