package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.RegisterRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.UserResponse;
import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.ResourceAlreadyExistsException;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.exception.ValidationException;
import AplikacjePrzemyslowe.DatApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service dla zarządzania użytkownikami.
 * Obsługuje rejestrację, usuwanie konta, zmianę hasła.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // ========== READ OPERATIONS ==========

    /**
     * Znajduje użytkownika po ID.
     * @param id ID użytkownika
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.debug("Finding user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setAge(user.getAge());

        log.info("Found user: {}", user.getUsername());
        return response;
    }

    /**
     * Znajduje użytkownika po email.
     * @param email Email użytkownika
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setAge(user.getAge());

        return response;
    }

    /**
     * Znajduje użytkownika po username.
     * @param username Username użytkownika
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        log.debug("Finding user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setAge(user.getAge());

        return response;
    }

    /**
     * Znajduje wszystkich użytkowników z paginacją.
     * @param pageable Paginacja
     * @return Page<UserResponse>
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        log.debug("Finding all users - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);

        log.info("Found {} users", users.getTotalElements());

        return users.map(user -> {
            UserResponse response = modelMapper.map(user, UserResponse.class);
            response.setAge(user.getAge());
            return response;
        });
    }

    /**
     * Znajduje aktywnych użytkowników z paginacją.
     * @param pageable Paginacja
     * @return Page<UserResponse>
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findActiveUsers(Pageable pageable) {
        log.debug("Finding active users - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findByIsActiveTrue(pageable);

        log.info("Found {} active users", users.getTotalElements());

        return users.map(user -> {
            UserResponse response = modelMapper.map(user, UserResponse.class);
            response.setAge(user.getAge());
            return response;
        });
    }

    // ========== WRITE OPERATIONS ==========

    /**
     * Rejestracja nowego użytkownika.
     * @param request Dane rejestracji
     * @return UserResponse
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Walidacja - email już istnieje?
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Registration failed - email already exists: {}", request.getEmail());
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        // Walidacja - username już istnieje?
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Registration failed - username already exists: {}", request.getUsername());
            throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
        }

        // Walidacja biznesowa - minimalny wiek 18 lat
        LocalDate minBirthDate = LocalDate.now().minusYears(18);
        if (request.getBirthDate().isAfter(minBirthDate)) {
            log.error("Registration failed - user too young: {}", request.getBirthDate());
            throw new ValidationException("birthDate", "Musisz mieć co najmniej 18 lat");
        }

        // Tworzenie użytkownika
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(Gender.valueOf(request.getGender()))
                .birthDate(request.getBirthDate())
                .city(request.getCity())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {} (id: {})", savedUser.getUsername(), savedUser.getId());

        UserResponse response = modelMapper.map(savedUser, UserResponse.class);
        response.setAge(savedUser.getAge());

        return response;
    }

    /**
     * Aktualizacja hasła użytkownika.
     * @param userId ID użytkownika
     * @param currentPassword Aktualne hasło
     * @param newPassword Nowe hasło
     */
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Updating password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Weryfikacja aktualnego hasła
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.error("Password update failed - incorrect current password for user: {}", userId);
            throw new ValidationException("currentPassword", "Aktualne hasło jest nieprawidłowe");
        }

        // Walidacja - nowe hasło nie może być takie samo jak stare
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.error("Password update failed - new password same as old for user: {}", userId);
            throw new ValidationException("newPassword", "Nowe hasło musi być różne od aktualnego");
        }

        // Aktualizacja hasła
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password updated successfully for user: {}", userId);
    }

    /**
     * Aktualizacja miasta użytkownika.
     * @param userId ID użytkownika
     * @param newCity Nowe miasto
     */
    @Transactional
    public UserResponse updateCity(Long userId, String newCity) {
        log.info("Updating city for user: {} to: {}", userId, newCity);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setCity(newCity);
        User savedUser = userRepository.save(user);

        log.info("City updated successfully for user: {}", userId);

        UserResponse response = modelMapper.map(savedUser, UserResponse.class);
        response.setAge(savedUser.getAge());

        return response;
    }

    /**
     * Deaktywacja konta użytkownika.
     * @param userId ID użytkownika
     */
    @Transactional
    public void deactivateAccount(Long userId) {
        log.info("Deactivating account for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Account deactivated successfully for user: {}", userId);
    }

    /**
     * Reaktywacja konta użytkownika.
     * @param userId ID użytkownika
     */
    @Transactional
    public void reactivateAccount(Long userId) {
        log.info("Reactivating account for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("Account reactivated successfully for user: {}", userId);
    }

    /**
     * Całkowite usunięcie konta użytkownika (kaskadowe).
     * Usuwa również: profile, swipes, matches, messages.
     * @param userId ID użytkownika
     */
    @Transactional
    public void deleteAccount(Long userId) {
        log.warn("Permanently deleting account for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String username = user.getUsername();

        // Kaskadowe usunięcie dzięki ON DELETE CASCADE w FK constraints
        userRepository.delete(user);

        log.warn("Account permanently deleted for user: {} (username: {})", userId, username);
    }

    // ========== HELPER METHODS ==========

    /**
     * Sprawdza czy użytkownik istnieje.
     * @param userId ID użytkownika
     * @return true jeśli istnieje
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Sprawdza czy email jest już zajęty.
     * @param email Email
     * @return true jeśli zajęty
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Sprawdza czy username jest już zajęty.
     * @param username Username
     * @return true jeśli zajęty
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Liczy aktywnych użytkowników.
     * @return liczba aktywnych użytkowników
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    /**
     * Zwraca encję User (bez mapowania do DTO).
     */
    @Transactional(readOnly = true)
    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Usuwa użytkownika po ID.
     */
    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        log.info("User {} deleted", id);
    }
}
