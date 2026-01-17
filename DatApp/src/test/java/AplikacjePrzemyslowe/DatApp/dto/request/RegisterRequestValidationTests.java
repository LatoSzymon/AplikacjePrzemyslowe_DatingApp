package AplikacjePrzemyslowe.DatApp.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterRequest Bean Validation Tests")
class RegisterRequestValidationTests {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Happy path: poprawny request przechodzi walidację")
    void validRequest_passesValidation() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user_123")
                .email("user@example.com")
                .password("Password1")
                .confirmPassword("Password1")
                .gender("MALE")
                .birthDate(LocalDate.now().minusYears(20))
                .city("Warszawa")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Walidacja nie powinna zwrócić błędów dla poprawnych danych");
    }

    @Test
    @DisplayName("Brak wymaganych pól powoduje błędy")
    void missingRequiredFields_producesViolations() {
        RegisterRequest req = RegisterRequest.builder().build();
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Hasła muszą być identyczne (AssertTrue)")
    void passwordsMustMatch() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user_123")
                .email("user@example.com")
                .password("Password1")
                .confirmPassword("Password2")
                .gender("MALE")
                .birthDate(LocalDate.now().minusYears(25))
                .city("Kraków")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Hasła muszą być identyczne")));
    }

    @Test
    @DisplayName("Hasło musi spełniać kryteria złożoności")
    void passwordComplexity_enforced() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user_123")
                .email("user@example.com")
                .password("password") // brak wielkiej litery i cyfry
                .confirmPassword("password")
                .gender("MALE")
                .birthDate(LocalDate.now().minusYears(25))
                .city("Łódź")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Hasło musi zawierać")));
    }

    @Test
    @DisplayName("Email musi mieć prawidłowy format")
    void emailFormat_enforced() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user_123")
                .email("not-an-email")
                .password("Password1")
                .confirmPassword("Password1")
                .gender("MALE")
                .birthDate(LocalDate.now().minusYears(25))
                .city("Gdańsk")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Nieprawidłowy format email")));
    }

    @Test
    @DisplayName("Płeć musi być jedną z MALE/FEMALE/OTHER")
    void gender_mustBeOneOfEnum() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user_123")
                .email("user@example.com")
                .password("Password1")
                .confirmPassword("Password1")
                .gender("UNKNOWN")
                .birthDate(LocalDate.now().minusYears(25))
                .city("Poznań")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Płeć musi być MALE, FEMALE lub OTHER")));
    }

    @Test
    @DisplayName("Data urodzenia musi być w przeszłości i minimalny wiek 18 lat")
    void birthDate_mustBePast_andAdult() {
        RegisterRequest reqTooYoung = RegisterRequest.builder()
                .username("user_123")
                .email("user@example.com")
                .password("Password1")
                .confirmPassword("Password1")
                .gender("MALE")
                .birthDate(LocalDate.now().minusYears(17))
                .city("Warszawa")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violationsYoung = validator.validate(reqTooYoung);
        assertTrue(violationsYoung.stream().anyMatch(v -> v.getMessage().contains("Musisz mieć co najmniej 18 lat")));

        RegisterRequest reqFuture = RegisterRequest.builder()
                .username("user_123")
                .email("user@example.com")
                .password("Password1")
                .confirmPassword("Password1")
                .gender("MALE")
                .birthDate(LocalDate.now().plusDays(1))
                .city("Warszawa")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violationsFuture = validator.validate(reqFuture);
        assertTrue(violationsFuture.stream().anyMatch(v -> v.getMessage().contains("Data urodzenia musi być w przeszłości")));
    }
}

