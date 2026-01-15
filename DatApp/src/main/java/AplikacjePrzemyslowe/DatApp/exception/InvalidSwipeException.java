package AplikacjePrzemyslowe.DatApp.exception;

/**
 * Wyjątek rzucany gdy operacja swipe jest nieprawidłowa (400 Bad Request).
 * Np. próba swipe'a tego samego użytkownika dwa razy, swipe na samego siebie.
 */
public class InvalidSwipeException extends RuntimeException {

    public InvalidSwipeException(String message) {
        super(message);
    }

    public InvalidSwipeException(Long userId, String reason) {
        super(String.format("Nieprawidłowy swipe na użytkownika %d: %s", userId, reason));
    }
}

