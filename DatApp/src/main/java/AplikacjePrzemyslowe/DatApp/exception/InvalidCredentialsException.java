package AplikacjePrzemyslowe.DatApp.exception;

/**
 * Wyjątek rzucany gdy dane uwierzytelniające są nieprawidłowe (401 Unauthorized).
 * Błędny login lub hasło.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Nieprawidłowy email/username lub hasło");
    }
}

