package AplikacjePrzeyslowe.dApp.service;

import AplikacjePrzeyslowe.dApp.dto.request.PreferenceRequest;
import AplikacjePrzeyslowe.dApp.dto.response.PreferenceResponse;
import AplikacjePrzeyslowe.dApp.entity.Gender;
import AplikacjePrzeyslowe.dApp.entity.Preference;
import AplikacjePrzeyslowe.dApp.entity.User;
import AplikacjePrzeyslowe.dApp.exception.ResourceNotFoundException;
import AplikacjePrzeyslowe.dApp.exception.ValidationException;
import AplikacjePrzeyslowe.dApp.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service dla zarządzania preferencjami wyszukiwania.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    // ========== READ OPERATIONS ==========

    /**
     * Znajduje preferencje użytkownika.
     */
    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(Long userId) {
        log.debug("Finding preferences for user: {}", userId);

        Preference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preference", "userId", userId));

        return modelMapper.map(preference, PreferenceResponse.class);
    }

    /**
     * Sprawdza czy użytkownik ma ustawione preferencje.
     */
    @Transactional(readOnly = true)
    public boolean hasPreferences(Long userId) {
        return preferenceRepository.existsByUserId(userId);
    }

    // ========== WRITE OPERATIONS ==========

    /**
     * Tworzy preferencje dla użytkownika.
     */
    @Transactional
    public PreferenceResponse createPreferences(Long userId, PreferenceRequest request) {
        log.info("Creating preferences for user: {}", userId);

        // Sprawdź czy użytkownik istnieje
        User user = userService.getUserEntity(userId);

        // Sprawdź czy preferencje już istnieją
        if (preferenceRepository.existsByUserId(userId)) {
            log.error("Preferences creation failed - already exist for user: {}", userId);
            throw new ValidationException("preferences", "Preferencje już istnieją dla tego użytkownika");
        }

        // Tworzenie preferencji
        Preference preference = Preference.builder()
                .user(user)
                .preferredGender(Gender.valueOf(request.getPreferredGender()))
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .maxDistanceKm(request.getMaxDistanceKm())
                .build();

        Preference savedPreference = preferenceRepository.save(preference);

        log.info("Preferences created successfully for user: {}", userId);

        return modelMapper.map(savedPreference, PreferenceResponse.class);
    }

    /**
     * Aktualizuje preferencje użytkownika.
     */
    @Transactional
    public PreferenceResponse updatePreferences(Long userId, PreferenceRequest request) {
        log.info("Updating preferences for user: {}", userId);

        Preference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preference", "userId", userId));

        // Aktualizacja pól
        preference.setPreferredGender(Gender.valueOf(request.getPreferredGender()));
        preference.setMinAge(request.getMinAge());
        preference.setMaxAge(request.getMaxAge());
        preference.setMaxDistanceKm(request.getMaxDistanceKm());

        Preference savedPreference = preferenceRepository.save(preference);

        log.info("Preferences updated successfully for user: {}", userId);

        return modelMapper.map(savedPreference, PreferenceResponse.class);
    }

    /**
     * Usuwa preferencje użytkownika.
     */
    @Transactional
    public void deletePreferences(Long userId) {
        log.info("Deleting preferences for user: {}", userId);

        Preference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preference", "userId", userId));

        preferenceRepository.delete(preference);

        log.info("Preferences deleted successfully for user: {}", userId);
    }

    // ========== HELPER METHODS ==========

    /**
     * Pobiera Preference entity (do użytku wewnętrznego).
     */
    @Transactional(readOnly = true)
    public Preference getPreferenceEntity(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preference", "userId", userId));
    }
}

