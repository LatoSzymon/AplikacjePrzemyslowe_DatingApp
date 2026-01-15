package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dao.UserJdbcDao;
import AplikacjePrzemyslowe.DatApp.dto.response.CandidateResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.InterestResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.PhotoResponse;
import AplikacjePrzemyslowe.DatApp.entity.*;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.SwipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service dla matching algorithm - główny silnik dopasowywania kandydatów.
 *
 * Algorytm matching:
 * 1. Filtruje po preferencjach (płeć, wiek)
 * 2. Wyklucza już ocenionych użytkowników
 * 3. Oblicza compatibility score (wspólne zainteresowania, dystans)
 * 4. Sortuje po score (malejąco)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final UserService userService;
    private final PreferenceService preferenceService;
    private final ProfileService profileService;
    private final InterestService interestService;
    private final SwipeRepository swipeRepository;
    private final UserJdbcDao userJdbcDao;
    private final ModelMapper modelMapper;

    // ========== MATCHING ALGORITHM ==========

    /**
     * Znajduje następnego kandydata dla użytkownika.
     * Algorytm filtruje po preferencjach i sortuje po compatibility score.
     *
     * @param userId ID użytkownika szukającego
     * @return CandidateResponse lub null jeśli brak kandydatów
     */
    @Transactional(readOnly = true)
    public CandidateResponse getNextCandidate(Long userId) {
        log.info("Finding next candidate for user: {}", userId);

        // Pobierz użytkownika i jego preferencje
        User currentUser = userService.getUserEntity(userId);
        Preference preferences = preferenceService.getPreferenceEntity(userId);

        // Znajdź kandydatów spełniających preferencje
        List<User> candidates = findEligibleCandidates(currentUser, preferences);

        if (candidates.isEmpty()) {
            log.info("No candidates found for user: {}", userId);
            return null;
        }

        // Oblicz compatibility score dla każdego kandydata
        List<ScoredCandidate> scoredCandidates = candidates.stream()
                .map(candidate -> scoreCandidate(currentUser, candidate))
                .sorted(Comparator.comparingInt(ScoredCandidate::getScore).reversed())
                .collect(Collectors.toList());

        // Zwróć najlepiej pasującego kandydata
        User bestMatch = scoredCandidates.get(0).getUser();

        log.info("Found best match for user {}: {} (score: {})",
                userId, bestMatch.getUsername(), scoredCandidates.get(0).getScore());

        return mapToCandidateResponse(currentUser, bestMatch, scoredCandidates.get(0).getScore());
    }

    /**
     * Znajduje kandydatów z paginacją.
     * Zwraca listę kandydatów posortowanych po compatibility score.
     */
    @Transactional(readOnly = true)
    public Page<CandidateResponse> getCandidates(Long userId, Pageable pageable) {
        log.info("Finding candidates for user: {} (page: {}, size: {})",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        User currentUser = userService.getUserEntity(userId);
        Preference preferences = preferenceService.getPreferenceEntity(userId);

        // Znajdź wszystkich kandydatów
        List<User> candidates = findEligibleCandidates(currentUser, preferences);

        if (candidates.isEmpty()) {
            log.info("No candidates found for user: {}", userId);
            return Page.empty(pageable);
        }

        // Score i sortuj
        List<ScoredCandidate> scoredCandidates = candidates.stream()
                .map(candidate -> scoreCandidate(currentUser, candidate))
                .sorted(Comparator.comparingInt(ScoredCandidate::getScore).reversed())
                .collect(Collectors.toList());

        // Paginacja
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), scoredCandidates.size());

        List<CandidateResponse> pageContent = scoredCandidates.subList(start, end).stream()
                .map(scored -> mapToCandidateResponse(currentUser, scored.getUser(), scored.getScore()))
                .collect(Collectors.toList());

        log.info("Found {} candidates for user: {}", scoredCandidates.size(), userId);

        return new PageImpl<>(pageContent, pageable, scoredCandidates.size());
    }

    // ========== HELPER METHODS ==========

    /**
     * Znajduje eligible kandydatów (spełniających preferencje, nie swipe'niętych).
     */
    private List<User> findEligibleCandidates(User currentUser, Preference preferences) {
        log.debug("Finding eligible candidates for user: {}", currentUser.getId());

        // Użyj JDBC DAO dla wydajności (złożone query)
        Page<User> candidatesPage = userJdbcDao.findCandidatesByPreference(
                currentUser.getId(),
                preferences.getPreferredGender(),
                preferences.getMinAge(),
                preferences.getMaxAge(),
                Pageable.unpaged()
        );

        List<User> candidates = candidatesPage.getContent();

        // Dodatkowe filtrowanie w Javie (age calculation)
        return candidates.stream()
                .filter(candidate -> isAgeInRange(candidate, preferences))
                .filter(User::getIsActive)
                .collect(Collectors.toList());
    }

    /**
     * Sprawdza czy wiek kandydata mieści się w zakresie.
     */
    private boolean isAgeInRange(User candidate, Preference preferences) {
        int age = Period.between(candidate.getBirthDate(), LocalDate.now()).getYears();
        return age >= preferences.getMinAge() && age <= preferences.getMaxAge();
    }

    /**
     * Oblicza compatibility score dla kandydata.
     *
     * Scoring algorithm:
     * - Wspólne zainteresowania: +10 punktów za każde
     * - Dystans: -1 punkt za każde 10km
     * - Completeness profilu: +20 punktów za kompletny profil
     * - Age difference: -2 punkty za każdy rok różnicy (max -20)
     *
     * Max score: ~100 punktów
     */
    private ScoredCandidate scoreCandidate(User currentUser, User candidate) {
        int score = 0;

        // 1. Wspólne zainteresowania (+10 za każde)
        long commonInterests = interestService.countCommonInterests(
                currentUser.getId(),
                candidate.getId()
        );
        score += (int) (commonInterests * 10);

        // 2. Dystans (-1 za każde 10km)
        double distance = calculateDistance(currentUser, candidate);
        score -= (int) (distance / 10);

        // 3. Completeness profilu (+20 punktów)
        if (isProfileComplete(candidate)) {
            score += 20;
        }

        // 4. Age difference (-2 za każdy rok, max -20)
        int ageDifference = Math.abs(
                Period.between(currentUser.getBirthDate(), candidate.getBirthDate()).getYears()
        );
        score -= Math.min(ageDifference * 2, 20);

        // Minimum score: 0
        score = Math.max(score, 0);

        log.debug("Scored candidate {} for user {}: {} points (commonInterests: {}, distance: {}km)",
                candidate.getUsername(), currentUser.getUsername(), score, commonInterests, distance);

        return new ScoredCandidate(candidate, score, (int) commonInterests, distance);
    }

    /**
     * Oblicza dystans między użytkownikami (Haversine formula).
     * Zwraca dystans w kilometrach.
     */
    private double calculateDistance(User user1, User user2) {
        Profile profile1 = profileService.getProfileEntity(user1.getId());
        Profile profile2 = profileService.getProfileEntity(user2.getId());

        // Jeśli brak koordynatów, zwróć średni dystans
        if (profile1.getLatitude() == null || profile1.getLongitude() == null ||
            profile2.getLatitude() == null || profile2.getLongitude() == null) {
            return 50.0; // Default: 50km
        }

        double lat1 = Math.toRadians(profile1.getLatitude());
        double lon1 = Math.toRadians(profile1.getLongitude());
        double lat2 = Math.toRadians(profile2.getLatitude());
        double lon2 = Math.toRadians(profile2.getLongitude());

        // Haversine formula
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double earthRadiusKm = 6371.0;
        return earthRadiusKm * c;
    }

    /**
     * Sprawdza czy profil jest kompletny.
     */
    private boolean isProfileComplete(User user) {
        try {
            Profile profile = profileService.getProfileEntity(user.getId());
            return profile.getBio() != null && !profile.getBio().isBlank() &&
                   !profile.getPhotos().isEmpty() &&
                   !profile.getInterests().isEmpty();
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    /**
     * Mapuje User entity na CandidateResponse DTO.
     */
    private CandidateResponse mapToCandidateResponse(User currentUser, User candidate, int compatibilityScore) {
        Profile candidateProfile = profileService.getProfileEntity(candidate.getId());

        // Main photo
        PhotoResponse mainPhoto = candidateProfile.getPhotos().stream()
                .filter(Photo::getIsPrimary)
                .findFirst()
                .map(photo -> modelMapper.map(photo, PhotoResponse.class))
                .orElse(null);

        // Interests
        Set<InterestResponse> interests = candidateProfile.getInterests().stream()
                .map(interest -> modelMapper.map(interest, InterestResponse.class))
                .collect(Collectors.toSet());

        // Common interests count
        int commonInterestsCount = (int) interestService.countCommonInterests(
                currentUser.getId(),
                candidate.getId()
        );

        // Distance
        double distance = calculateDistance(currentUser, candidate);

        // Age
        int age = Period.between(candidate.getBirthDate(), LocalDate.now()).getYears();

        return CandidateResponse.builder()
                .id(candidate.getId())
                .username(candidate.getUsername())
                .gender(candidate.getGender().name())
                .age(age)
                .city(candidate.getCity())
                .bio(candidateProfile.getBio())
                .heightCm(candidateProfile.getHeightCm())
                .occupation(candidateProfile.getOccupation())
                .education(candidateProfile.getEducation())
                .mainPhoto(mainPhoto)
                .interests(interests)
                .commonInterestsCount(commonInterestsCount)
                .distanceKm(distance)
                .compatibilityScore(compatibilityScore)
                .build();
    }

    // ========== INNER CLASS ==========

    /**
     * Helper class do przechowywania kandydata z jego score.
     */
    private static class ScoredCandidate {
        private final User user;
        private final int score;
        private final int commonInterests;
        private final double distance;

        public ScoredCandidate(User user, int score, int commonInterests, double distance) {
            this.user = user;
            this.score = score;
            this.commonInterests = commonInterests;
            this.distance = distance;
        }

        public User getUser() {
            return user;
        }

        public int getScore() {
            return score;
        }

        public int getCommonInterests() {
            return commonInterests;
        }

        public double getDistance() {
            return distance;
        }
    }
}

