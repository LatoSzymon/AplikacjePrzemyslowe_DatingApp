package AplikacjePrzemyslowe.DatApp.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorResponse DTO Tests")
class ErrorResponseTests {

    @Test
    @DisplayName("Konstruktor 4-argumentowy powinien ustawić pola i timestamp")
    void constructorWithFourArgs_setsFieldsAndTimestamp() {
        // Arrange
        Integer status = 400;
        String error = "Bad Request";
        String message = "Nieprawidłowe dane";
        String path = "/api/v1/test";

        // Act
        ErrorResponse response = new ErrorResponse(status, error, message, path);

        // Assert
        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(path, response.getPath());
        assertNotNull(response.getTimestamp(), "Timestamp powinien być ustawiony");
        // Timestamp powinien być "teraz" (z tolerancją kilku sekund)
        assertTrue(Duration.between(response.getTimestamp(), LocalDateTime.now()).abs().getSeconds() < 5,
                "Timestamp powinien być blisko bieżącego czasu");
    }

    @Test
    @DisplayName("Builder powinien poprawnie tworzyć obiekt")
    void builder_createsObject() {
        // Arrange
        LocalDateTime ts = LocalDateTime.now().minusSeconds(1);
        Map<String, List<String>> validation = Map.of(
                "email", List.of("must be a well-formed email address"),
                "password", List.of("size must be between 8 and 64")
        );

        // Act
        ErrorResponse response = ErrorResponse.builder()
                .status(422)
                .error("Unprocessable Entity")
                .message("Validation failed")
                .path("/api/v1/register")
                .timestamp(ts)
                .validationErrors(validation)
                .build();

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Unprocessable Entity", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("/api/v1/register", response.getPath());
        assertEquals(ts, response.getTimestamp());
        assertEquals(validation, response.getValidationErrors());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać poprawnie")
    void settersAndGetters_work() {
        // Arrange
        ErrorResponse response = new ErrorResponse();
        LocalDateTime ts = LocalDateTime.now();

        // Act
        response.setStatus(401);
        response.setError("Unauthorized");
        response.setMessage("Brak autoryzacji");
        response.setPath("/api/v1/secure");
        response.setTimestamp(ts);
        response.setValidationErrors(Map.of("token", List.of("missing")));

        // Assert
        assertEquals(401, response.getStatus());
        assertEquals("Unauthorized", response.getError());
        assertEquals("Brak autoryzacji", response.getMessage());
        assertEquals("/api/v1/secure", response.getPath());
        assertEquals(ts, response.getTimestamp());
        assertEquals(Map.of("token", List.of("missing")), response.getValidationErrors());
    }
}

