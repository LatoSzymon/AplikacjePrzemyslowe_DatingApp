package AplikacjePrzemyslowe.DatApp.exception;

/**
 * Wyjątek rzucany gdy użytkownicy już są dopasowani (409 Conflict).
 * Próba utworzenia duplikatu matcha.
 */
public class AlreadyMatchedException extends RuntimeException {

    public AlreadyMatchedException(String message) {
        super(message);
    }

    public AlreadyMatchedException(Long user1Id, Long user2Id) {
        super(String.format("Użytkownicy %d i %d są już dopasowani", user1Id, user2Id));
    }
}

