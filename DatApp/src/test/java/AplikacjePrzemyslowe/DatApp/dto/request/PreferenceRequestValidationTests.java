package AplikacjePrzemyslowe.DatApp.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PreferenceRequest Bean Validation Tests")
class PreferenceRequestValidationTests {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Happy path: poprawny request przechodzi walidację")
    void validRequest_passesValidation() {
        PreferenceRequest req = PreferenceRequest.builder()
                .preferredGender("FEMALE")
                .minAge(25)
                .maxAge(35)
                .maxDistanceKm(50)
                .build();

        Set<ConstraintViolation<PreferenceRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Walidacja nie powinna zwrócić błędów dla poprawnych danych");
    }

    @Test
    @DisplayName("Brak wymaganych pól powoduje błędy")
    void missingRequiredFields_producesViolations() {
        PreferenceRequest req = PreferenceRequest.builder().build();
        Set<ConstraintViolation<PreferenceRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Płeć musi być jedną z MALE/FEMALE/OTHER")
    void preferredGender_mustBeOneOfEnum() {
        PreferenceRequest req = PreferenceRequest.builder()
                .preferredGender("UNKNOWN")
                .minAge(25)
                .maxAge(35)
                .maxDistanceKm(50)
                .build();

        Set<ConstraintViolation<PreferenceRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Preferowana płeć musi być MALE, FEMALE lub OTHER")));
    }

    @Test
    @DisplayName("Granice wieku i odległości są wymuszane")
    void ageAndDistanceBounds_enforced() {
        PreferenceRequest tooYoungMin = PreferenceRequest.builder()
                .preferredGender("MALE")
                .minAge(17)
                .maxAge(30)
                .maxDistanceKm(50)
                .build();
        Set<ConstraintViolation<PreferenceRequest>> v1 = validator.validate(tooYoungMin);
        assertTrue(v1.stream().anyMatch(v -> v.getMessage().contains("Minimalny wiek musi być co najmniej 18")));

        PreferenceRequest tooOldMax = PreferenceRequest.builder()
                .preferredGender("MALE")
                .minAge(18)
                .maxAge(101)
                .maxDistanceKm(50)
                .build();
        Set<ConstraintViolation<PreferenceRequest>> v2 = validator.validate(tooOldMax);
        assertTrue(v2.stream().anyMatch(v -> v.getMessage().contains("Maksymalny wiek nie może przekraczać 100")));

        PreferenceRequest tooSmallDistance = PreferenceRequest.builder()
                .preferredGender("MALE")
                .minAge(18)
                .maxAge(30)
                .maxDistanceKm(0)
                .build();
        Set<ConstraintViolation<PreferenceRequest>> v3 = validator.validate(tooSmallDistance);
        assertTrue(v3.stream().anyMatch(v -> v.getMessage().contains("Maksymalna odległość musi być co najmniej 1 km")));

        PreferenceRequest tooBigDistance = PreferenceRequest.builder()
                .preferredGender("MALE")
                .minAge(18)
                .maxAge(30)
                .maxDistanceKm(501)
                .build();
        Set<ConstraintViolation<PreferenceRequest>> v4 = validator.validate(tooBigDistance);
        assertTrue(v4.stream().anyMatch(v -> v.getMessage().contains("Maksymalna odległość nie może przekraczać 500 km")));
    }

    @Test
    @DisplayName("Walidacja biznesowa: minAge nie może być większe niż maxAge")
    void businessValidation_minAgeNotGreaterThanMaxAge() {
        PreferenceRequest req = PreferenceRequest.builder()
                .preferredGender("OTHER")
                .minAge(40)
                .maxAge(30)
                .maxDistanceKm(50)
                .build();

        Set<ConstraintViolation<PreferenceRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Minimalny wiek nie może być większy niż maksymalny")));
    }
}

