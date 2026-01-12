package AplikacjePrzeyslowe.dApp.exception;

/**
 * Wyjątek rzucany gdy zasób już istnieje (409 Conflict).
 * Np. próba rejestracji z emailem który już istnieje.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s o %s '%s' już istnieje", resourceName, fieldName, fieldValue));
    }
}

