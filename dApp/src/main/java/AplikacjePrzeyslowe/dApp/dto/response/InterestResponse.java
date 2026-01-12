package AplikacjePrzeyslowe.dApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla odpowiedzi z zainteresowaniem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String icon;
    private Integer userCount;
}

