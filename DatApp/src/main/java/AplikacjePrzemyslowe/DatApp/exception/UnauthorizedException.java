package AplikacjePrzemyslowe.DatApp.exception;

/**
 * Wyjątek rzucany gdy użytkownik nie ma uprawnień do wykonania operacji (403 Forbidden).
 * Np. próba wysłania wiadomości do matcha który nie należy do użytkownika.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String action, String reason) {
        super(String.format("Brak uprawnień do: %s. Powód: %s", action, reason));
    }
}

