package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.UpdateProfileRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.InterestResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.PhotoResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.ProfileResponse;
import AplikacjePrzemyslowe.DatApp.entity.Interest;
import AplikacjePrzemyslowe.DatApp.entity.Photo;
import AplikacjePrzemyslowe.DatApp.entity.Profile;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.exception.ValidationException;
import AplikacjePrzemyslowe.DatApp.repository.InterestRepository;
import AplikacjePrzemyslowe.DatApp.repository.PhotoRepository;
import AplikacjePrzemyslowe.DatApp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service dla zarządzania profilami użytkowników.
 * Obsługuje tworzenie, aktualizację profilu, dodawanie zdjęć i zainteresowań.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final InterestRepository interestRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    // ========== READ OPERATIONS ==========

    /**
     * Znajduje profil użytkownika.
     * @param userId ID użytkownika
     * @return ProfileResponse
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        log.debug("Finding profile for user: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        ProfileResponse response = mapToResponse(profile);

        log.info("Found profile for user: {}", userId);
        return response;
    }

    /**
     * Sprawdza czy profil istnieje dla użytkownika.
     * @param userId ID użytkownika
     * @return true jeśli istnieje
     */
    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return profileRepository.existsByUserId(userId);
    }

    // ========== WRITE OPERATIONS ==========

    /**
     * Tworzy profil dla użytkownika.
     * @param userId ID użytkownika
     * @param request Dane profilu
     * @return ProfileResponse
     */
    @Transactional
    public ProfileResponse createProfile(Long userId, UpdateProfileRequest request) {
        log.info("Creating profile for user: {}", userId);

        // Sprawdź czy użytkownik istnieje
        User user = userService.getUserEntity(userId);

        // Sprawdź czy profil już istnieje
        if (profileRepository.existsByUserId(userId)) {
            log.error("Profile creation failed - profile already exists for user: {}", userId);
            throw new ValidationException("profile", "Profil już istnieje dla tego użytkownika");
        }

        // Tworzenie profilu
        Profile profile = Profile.builder()
                .user(user)
                .bio(request.getBio())
                .heightCm(request.getHeightCm())
                .occupation(request.getOccupation())
                .education(request.getEducation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .photos(new HashSet<>())
                .interests(new HashSet<>())
                .build();

        // Dodaj zainteresowania jeśli podane
        if (request.getInterestIds() != null && !request.getInterestIds().isEmpty()) {
            addInterestsToProfile(profile, request.getInterestIds());
        }

        Profile savedProfile = profileRepository.save(profile);

        log.info("Profile created successfully for user: {}", userId);

        return mapToResponse(savedProfile);
    }

    /**
     * Aktualizuje profil użytkownika.
     * @param userId ID użytkownika
     * @param request Dane do aktualizacji
     * @return ProfileResponse
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        // Aktualizacja pól (jeśli podane)
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getHeightCm() != null) {
            profile.setHeightCm(request.getHeightCm());
        }
        if (request.getOccupation() != null) {
            profile.setOccupation(request.getOccupation());
        }
        if (request.getEducation() != null) {
            profile.setEducation(request.getEducation());
        }
        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }

        // Aktualizacja zainteresowań (jeśli podane)
        if (request.getInterestIds() != null) {
            updateProfileInterests(profile, request.getInterestIds());
        }

        Profile savedProfile = profileRepository.save(profile);

        log.info("Profile updated successfully for user: {}", userId);

        return mapToResponse(savedProfile);
    }

    /**
     * Dodaje zdjęcie do profilu.
     * @param userId ID użytkownika
     * @param photoUrl URL zdjęcia
     * @param isPrimary Czy główne zdjęcie
     * @param displayOrder Kolejność wyświetlania
     * @return PhotoResponse
     */
    @Transactional
    public PhotoResponse addPhoto(Long userId, String photoUrl, Boolean isPrimary, Integer displayOrder) {
        log.info("Adding photo to profile for user: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        // Jeśli to ma być główne zdjęcie, usuń flag z innych
        if (Boolean.TRUE.equals(isPrimary)) {
            profile.getPhotos().forEach(photo -> photo.setIsPrimary(false));
        }

        // Tworzenie zdjęcia
        Photo photo = Photo.builder()
                .profile(profile)
                .photoUrl(photoUrl)
                .isPrimary(isPrimary != null ? isPrimary : false)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .build();

        Photo savedPhoto = photoRepository.save(photo);

        log.info("Photo added successfully to profile for user: {}", userId);

        return modelMapper.map(savedPhoto, PhotoResponse.class);
    }

    /**
     * Usuwa zdjęcie z profilu.
     * @param userId ID użytkownika
     * @param photoId ID zdjęcia
     */
    @Transactional
    public void removePhoto(Long userId, Long photoId) {
        log.info("Removing photo {} from profile for user: {}", photoId, userId);

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", photoId));

        // Sprawdź czy zdjęcie należy do użytkownika
        if (!photo.getProfile().getUser().getId().equals(userId)) {
            log.error("Photo removal failed - photo does not belong to user: {}", userId);
            throw new ValidationException("photo", "To zdjęcie nie należy do Twojego profilu");
        }

        photoRepository.delete(photo);

        log.info("Photo removed successfully from profile for user: {}", userId);
    }

    /**
     * Ustawia zdjęcie jako główne.
     * @param userId ID użytkownika
     * @param photoId ID zdjęcia
     */
    @Transactional
    public void setPrimaryPhoto(Long userId, Long photoId) {
        log.info("Setting photo {} as primary for user: {}", photoId, userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", photoId));

        // Sprawdź czy zdjęcie należy do użytkownika
        if (!photo.getProfile().getId().equals(profile.getId())) {
            log.error("Set primary photo failed - photo does not belong to user: {}", userId);
            throw new ValidationException("photo", "To zdjęcie nie należy do Twojego profilu");
        }

        // Usuń flag z innych zdjęć
        profile.getPhotos().forEach(p -> p.setIsPrimary(false));

        // Ustaw jako główne
        photo.setIsPrimary(true);
        photoRepository.save(photo);

        log.info("Photo set as primary successfully for user: {}", userId);
    }

    /**
     * Dodaje zainteresowania do profilu.
     * @param userId ID użytkownika
     * @param interestIds IDs zainteresowań
     */
    @Transactional
    public ProfileResponse addInterests(Long userId, Set<Long> interestIds) {
        log.info("Adding {} interests to profile for user: {}", interestIds.size(), userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        addInterestsToProfile(profile, interestIds);

        Profile savedProfile = profileRepository.save(profile);

        log.info("Interests added successfully to profile for user: {}", userId);

        return mapToResponse(savedProfile);
    }

    /**
     * Usuwa zainteresowanie z profilu.
     * @param userId ID użytkownika
     * @param interestId ID zainteresowania
     */
    @Transactional
    public ProfileResponse removeInterest(Long userId, Long interestId) {
        log.info("Removing interest {} from profile for user: {}", interestId, userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", interestId));

        profile.removeInterest(interest);

        Profile savedProfile = profileRepository.save(profile);

        log.info("Interest removed successfully from profile for user: {}", userId);

        return mapToResponse(savedProfile);
    }

    /**
     * Usuwa wszystkie zainteresowania z profilu.
     * @param userId ID użytkownika
     */
    @Transactional
    public void clearInterests(Long userId) {
        log.info("Clearing all interests from profile for user: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        profile.getInterests().clear();
        profileRepository.save(profile);

        log.info("All interests cleared from profile for user: {}", userId);
    }

    // ========== HELPER METHODS ==========

    /**
     * Dodaje zainteresowania do profilu (helper).
     */
    private void addInterestsToProfile(Profile profile, Set<Long> interestIds) {
        for (Long interestId : interestIds) {
            Interest interest = interestRepository.findById(interestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Interest", interestId));
            profile.addInterest(interest);
        }
    }

    /**
     * Aktualizuje zainteresowania profilu (helper).
     * Zastępuje wszystkie zainteresowania nowymi.
     */
    private void updateProfileInterests(Profile profile, Set<Long> interestIds) {
        profile.getInterests().clear();
        addInterestsToProfile(profile, interestIds);
    }

    /**
     * Mapuje Profile entity na ProfileResponse DTO.
     */
    private ProfileResponse mapToResponse(Profile profile) {
        ProfileResponse response = modelMapper.map(profile, ProfileResponse.class);

        // Mapowanie zdjęć
        List<PhotoResponse> photos = profile.getPhotos().stream()
                .map(photo -> modelMapper.map(photo, PhotoResponse.class))
                .collect(Collectors.toList());
        response.setPhotos(photos);

        // Mapowanie zainteresowań
        Set<InterestResponse> interests = profile.getInterests().stream()
                .map(interest -> modelMapper.map(interest, InterestResponse.class))
                .collect(Collectors.toSet());
        response.setInterests(interests);

        // Computed fields
        response.setPhotosCount(profile.getPhotos().size());
        response.setInterestsCount(profile.getInterests().size());
        response.setIsComplete(isProfileComplete(profile));

        return response;
    }

    /**
     * Sprawdza czy profil jest kompletny.
     */
    private boolean isProfileComplete(Profile profile) {
        return profile.getBio() != null && !profile.getBio().isBlank()
                && !profile.getPhotos().isEmpty()
                && !profile.getInterests().isEmpty();
    }

    /**
     * Zwraca encję Profile dla userId.
     */
    @Transactional(readOnly = true)
    public Profile getProfileEntity(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }
}
