package AplikacjePrzemyslowe.DatApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO dla odpowiedzi z błędem.
 * Używane w GlobalExceptionHandler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    // Walidacja errors (field-specific)
    private Map<String, List<String>> validationErrors;

    public ErrorResponse(Integer status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}

