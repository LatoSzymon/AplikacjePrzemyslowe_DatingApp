package AplikacjePrzemyslowe.DatApp.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Wyjątek rzucany gdy walidacja biznesowa nie powiedzie się (400 Bad Request).
 * Zawiera szczegóły błędów walidacji.
 */
@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(String field, String error) {
        super("Błąd walidacji");
        this.errors = new HashMap<>();
        this.errors.put(field, error);
    }

    public void addError(String field, String error) {
        this.errors.put(field, error);
    }
}

