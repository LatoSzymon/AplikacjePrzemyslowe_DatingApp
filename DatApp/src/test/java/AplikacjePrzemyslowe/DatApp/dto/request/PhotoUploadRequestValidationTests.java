package AplikacjePrzemyslowe.DatApp.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhotoUploadRequest Bean Validation Tests")
class PhotoUploadRequestValidationTests {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private MultipartFile mockFile(String contentType, long size, boolean empty) {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getContentType()).thenReturn(contentType);
        Mockito.when(file.getSize()).thenReturn(size);
        Mockito.when(file.isEmpty()).thenReturn(empty);
        return file;
    }

    @Test
    @DisplayName("Happy path: poprawny plik przechodzi walidację")
    void validFile_passesValidation() {
        MultipartFile file = mockFile("image/jpeg", 1024L, false);
        PhotoUploadRequest req = PhotoUploadRequest.builder()
                .photo(file)
                .isPrimary(true)
                .displayOrder(1)
                .build();

        Set<ConstraintViolation<PhotoUploadRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Walidacja nie powinna zwrócić błędów dla poprawnego pliku");
    }

    @Test
    @DisplayName("Brak pliku powinien generować błąd")
    void missingFile_producesViolation() {
        PhotoUploadRequest req = PhotoUploadRequest.builder()
                .photo(null)
                .build();
        Set<ConstraintViolation<PhotoUploadRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Plik zdjęcia jest wymagany")));
    }

    @Test
    @DisplayName("Zły content type powoduje błąd")
    void wrongContentType_producesViolation() {
        MultipartFile file = mockFile("application/pdf", 1024L, false);
        PhotoUploadRequest req = PhotoUploadRequest.builder()
                .photo(file)
                .build();
        Set<ConstraintViolation<PhotoUploadRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Plik musi być obrazem")));
    }

    @Test
    @DisplayName("Za duży plik powoduje błąd")
    void tooBigFile_producesViolation() {
        MultipartFile file = mockFile("image/png", 11L * 1024 * 1024, false);
        PhotoUploadRequest req = PhotoUploadRequest.builder()
                .photo(file)
                .build();
        Set<ConstraintViolation<PhotoUploadRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Rozmiar pliku nie może przekraczać 10MB")));
    }
}

