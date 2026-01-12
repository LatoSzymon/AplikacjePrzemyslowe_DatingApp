package AplikacjePrzeyslowe.dApp.exception;

/**
 * Wyjątek rzucany gdy zasób nie został znaleziony (404 Not Found).
 * Np. użytkownik, profil, match, wiadomość.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s o ID %d nie został znaleziony", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s o %s '%s' nie został znaleziony", resourceName, fieldName, fieldValue));
    }
}

