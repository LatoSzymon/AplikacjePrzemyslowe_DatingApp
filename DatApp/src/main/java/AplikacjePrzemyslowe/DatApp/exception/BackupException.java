package AplikacjePrzemyslowe.DatApp.exception;

/**
 * Wyjątek rzucany gdy operacja importu/eksportu backupu nie powiodła się.
 */
public class BackupException extends RuntimeException {

    public BackupException(String message) {
        super(message);
    }

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}

