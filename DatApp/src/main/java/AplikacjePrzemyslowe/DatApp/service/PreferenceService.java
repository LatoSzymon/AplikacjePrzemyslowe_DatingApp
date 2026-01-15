package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.PreferenceRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.PreferenceResponse;
import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.Preference;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.PreferenceRepository;
import AplikacjePrzemyslowe.DatApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serwis do zarządzania preferencjami wyszukiwania użytkowników.
 * Umożliwia definiowanie kryteriów dopasowania (płeć, wiek, lokalizacja).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Pobiera preferencje użytkownika.
     * Jeśli użytkownik nie ma ustawionych preferencji, zwraca domyślne.
     *
     * @param userId ID użytkownika
     * @return Preferencje użytkownika
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(Long userId) {
        log.debug("Fetching preferences for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Preference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No preferences found for user {}, returning defaults", userId);
                    return createDefaultPreferences(user);
                });

        PreferenceResponse response = modelMapper.map(preference, PreferenceResponse.class);
        // Mapowanie maxDistanceKm na maxDistance w response
        response.setMaxDistance(preference.getMaxDistanceKm());
        return response;
    }

    /**
     * Ustawia lub aktualizuje preferencje użytkownika.
     *
     * @param userId ID użytkownika
     * @param request DTO z preferencjami
     * @return Zaktualizowane preferencje
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    @Transactional
    public PreferenceResponse setPreferences(Long userId, PreferenceRequest request) {
        log.info("Setting preferences for user {}: preferredGender={}, minAge={}, maxAge={}, maxDistance={}",
                userId, request.getPreferredGender(), request.getMinAge(), request.getMaxAge(), request.getMaxDistanceKm());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Sprawdź czy użytkownik już ma preferencje
        Preference preference = preferenceRepository.findByUserId(userId)
                .orElse(new Preference());

        // Aktualizuj wartości
        preference.setUser(user);
        // Konwertuj String na enum Gender
        preference.setPreferredGender(Gender.valueOf(request.getPreferredGender().toUpperCase()));
        preference.setMinAge(request.getMinAge());
        preference.setMaxAge(request.getMaxAge());
        preference.setMaxDistanceKm(request.getMaxDistanceKm());

        Preference savedPreference = preferenceRepository.save(preference);

        log.info("Preferences saved successfully for user {}", userId);

        PreferenceResponse response = modelMapper.map(savedPreference, PreferenceResponse.class);
        response.setMaxDistance(savedPreference.getMaxDistanceKm());
        return response;
    }

    /**
     * Resetuje preferencje użytkownika do domyślnych wartości.
     *
     * @param userId ID użytkownika
     * @return Domyślne preferencje
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    @Transactional
    public PreferenceResponse resetPreferences(Long userId) {
        log.info("Resetting preferences to defaults for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Preference preference = preferenceRepository.findByUserId(userId)
                .orElse(new Preference());

        // Ustaw domyślne wartości
        preference.setUser(user);
        preference.setPreferredGender(Gender.OTHER); // Dowolna płeć (OTHER jako "ANY")
        preference.setMinAge(18);
        preference.setMaxAge(99);
        preference.setMaxDistanceKm(50); // 50 km

        Preference savedPreference = preferenceRepository.save(preference);

        log.info("Preferences reset to defaults for user {}", userId);

        PreferenceResponse response = modelMapper.map(savedPreference, PreferenceResponse.class);
        response.setMaxDistance(savedPreference.getMaxDistanceKm());
        return response;
    }

    /**
     * Tworzy domyślne preferencje dla użytkownika (nie zapisuje do bazy).
     *
     * @param user Użytkownik
     * @return Obiekt z domyślnymi preferencjami
     */
    private Preference createDefaultPreferences(User user) {
        Preference defaultPreference = new Preference();
        defaultPreference.setUser(user);
        defaultPreference.setPreferredGender(Gender.OTHER); // Dowolna płeć
        defaultPreference.setMinAge(18);
        defaultPreference.setMaxAge(99);
        defaultPreference.setMaxDistanceKm(50);

        return defaultPreference;
    }

    /**
     * Usuwa preferencje użytkownika.
     *
     * @param userId ID użytkownika
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    @Transactional
    public void deletePreferences(Long userId) {
        log.info("Deleting preferences for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        preferenceRepository.findByUserId(userId).ifPresent(preference -> {
            preferenceRepository.delete(preference);
            log.info("Preferences deleted for user {}", userId);
        });
    }

    /**
     * Sprawdza czy użytkownik ma zdefiniowane preferencje.
     *
     * @param userId ID użytkownika
     * @return true jeśli preferencje istnieją, false w przeciwnym razie
     */
    @Transactional(readOnly = true)
    public boolean hasPreferences(Long userId) {
        log.debug("Checking if user {} has preferences", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return preferenceRepository.findByUserId(userId).isPresent();
    }

    /**
     * Pobiera encję Preference (dla użycia wewnętrznego w innych serwisach).
     *
     * @param userId ID użytkownika
     * @return Encja Preference
     */
    @Transactional(readOnly = true)
    public Preference getPreferenceEntity(Long userId) {
        log.debug("Fetching preference entity for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(user));
    }
}

