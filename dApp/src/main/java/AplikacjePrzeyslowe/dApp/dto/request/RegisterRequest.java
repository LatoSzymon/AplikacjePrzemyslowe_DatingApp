package AplikacjePrzeyslowe.dApp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO dla żądania rejestracji nowego użytkownika.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username jest wymagany")
    @Size(min = 3, max = 50, message = "Username musi mieć od 3 do 50 znaków")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username może zawierać tylko litery, cyfry i podkreślenia")
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Size(max = 100, message = "Email nie może przekraczać 100 znaków")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, max = 100, message = "Hasło musi mieć od 8 do 100 znaków")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Hasło musi zawierać co najmniej jedną wielką literę, jedną małą literę i jedną cyfrę"
    )
    private String password;

    @NotBlank(message = "Potwierdzenie hasła jest wymagane")
    private String confirmPassword;

    @NotNull(message = "Płeć jest wymagana")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Płeć musi być MALE, FEMALE lub OTHER")
    private String gender;

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    private LocalDate birthDate;

    @NotBlank(message = "Miasto jest wymagane")
    @Size(min = 2, max = 100, message = "Miasto musi mieć od 2 do 100 znaków")
    private String city;

    /**
     * Walidacja biznesowa: hasła muszą się zgadzać
     */
    @AssertTrue(message = "Hasła muszą być identyczne")
    public boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null) {
            return true; // Inne walidacje to obsłużą
        }
        return password.equals(confirmPassword);
    }

    /**
     * Walidacja biznesowa: minimalny wiek 18 lat
     */
    @AssertTrue(message = "Musisz mieć co najmniej 18 lat")
    public boolean isAdult() {
        if (birthDate == null) {
            return true; // Inna walidacja to obsłuży
        }
        return LocalDate.now().minusYears(18).isAfter(birthDate) ||
               LocalDate.now().minusYears(18).isEqual(birthDate);
    }
}

