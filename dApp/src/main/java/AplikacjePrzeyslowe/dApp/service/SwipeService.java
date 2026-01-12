package AplikacjePrzeyslowe.dApp.service;

import AplikacjePrzeyslowe.dApp.dto.request.SwipeRequest;
import AplikacjePrzeyslowe.dApp.dto.response.MatchResponse;
import AplikacjePrzeyslowe.dApp.dto.response.SwipeResponse;
import AplikacjePrzeyslowe.dApp.entity.Match;
import AplikacjePrzeyslowe.dApp.entity.Swipe;
import AplikacjePrzeyslowe.dApp.entity.SwipeType;
import AplikacjePrzeyslowe.dApp.entity.User;
import AplikacjePrzeyslowe.dApp.exception.InvalidSwipeException;
import AplikacjePrzeyslowe.dApp.exception.ResourceNotFoundException;
import AplikacjePrzeyslowe.dApp.repository.MatchRepository;
import AplikacjePrzeyslowe.dApp.repository.SwipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service dla zarządzania swipe'ami i detekcji matchów.
 *
 * Logika mutual like:
 * 1. User A swipe'uje User B (LIKE)
 * 2. System sprawdza czy User B już dał LIKE dla User A
 * 3. Jeśli TAK → tworzy Match
 * 4. Jeśli NIE → zapisuje tylko Swipe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    // ========== WRITE OPERATIONS ==========

    /**
     * Zapisuje swipe i sprawdza czy nastąpił match.
     *
     * @param swiperId ID użytkownika wykonującego swipe
     * @param request Dane swipe'a
     * @return SwipeResponse z informacją o matchu
     */
    @Transactional
    public SwipeResponse recordSwipe(Long swiperId, SwipeRequest request) {
        log.info("Recording swipe from user {} to user {} (type: {})",
                swiperId, request.getSwipedUserId(), request.getSwipeType());

        Long swipedUserId = request.getSwipedUserId();
        SwipeType swipeType = SwipeType.valueOf(request.getSwipeType());

        // Walidacja - nie można swipnąć samego siebie
        if (swiperId.equals(swipedUserId)) {
            log.error("Swipe failed - user {} tried to swipe themselves", swiperId);
            throw new InvalidSwipeException("Nie możesz swipnąć samego siebie");
        }

        // Walidacja - czy użytkownicy istnieją
        User swiper = userService.getUserEntity(swiperId);
        User swipedUser = userService.getUserEntity(swipedUserId);

        // Walidacja - czy już swipnięto tego użytkownika
        if (swipeRepository.existsBySwipedUserIdAndSwiperId(swipedUserId, swiperId)) {
            log.error("Swipe failed - user {} already swiped user {}", swiperId, swipedUserId);
            throw new InvalidSwipeException(swipedUserId, "Ten użytkownik został już oceniony");
        }

        // Walidacja - czy swipedUser jest aktywny
        if (!swipedUser.getIsActive()) {
            log.error("Swipe failed - swiped user {} is not active", swipedUserId);
            throw new InvalidSwipeException(swipedUserId, "Ten użytkownik nie jest już aktywny");
        }

        // Zapisz swipe
        Swipe swipe = Swipe.builder()
                .swiper(swiper)
                .swipedUser(swipedUser)
                .swipeType(swipeType)
                .build();

        Swipe savedSwipe = swipeRepository.save(swipe);

        log.info("Swipe recorded successfully: {} -> {} ({})",
                swiper.getUsername(), swipedUser.getUsername(), swipeType);

        // Jeśli LIKE, sprawdź czy nastąpił match
        boolean isMatch = false;
        MatchResponse matchDetails = null;

        if (swipeType == SwipeType.LIKE) {
            Optional<Match> potentialMatch = checkMutualLike(swiper, swipedUser);

            if (potentialMatch.isPresent()) {
                isMatch = true;
                matchDetails = mapToMatchResponse(potentialMatch.get(), swiperId);
                log.info("MATCH DETECTED! Users {} and {} matched!",
                        swiper.getUsername(), swipedUser.getUsername());
            }
        }

        // Zwróć response
        return SwipeResponse.builder()
                .swipeId(savedSwipe.getId())
                .swipedUserId(swipedUserId)
                .swipeType(swipeType.name())
                .swipedAt(savedSwipe.getSwipedAt())
                .isMatch(isMatch)
                .matchDetails(matchDetails)
                .build();
    }

    /**
     * Sprawdza czy nastąpił mutual like i tworzy match jeśli tak.
     *
     * @param swiper Użytkownik wykonujący swipe
     * @param swipedUser Użytkownik będący obiektem swipe'a
     * @return Optional<Match> jeśli nastąpił match
     */
    @Transactional
    public Optional<Match> checkMutualLike(User swiper, User swipedUser) {
        log.debug("Checking mutual like between users {} and {}",
                swiper.getId(), swipedUser.getId());

        // Sprawdź czy swipedUser dał już LIKE dla swiper
        Optional<Swipe> reverseSwipe = swipeRepository.findSwipe(
                swipedUser.getId(),
                swiper.getId()
        );

        // Jeśli nie ma reverse swipe lub nie jest LIKE, brak matcha
        if (reverseSwipe.isEmpty() || reverseSwipe.get().getSwipeType() != SwipeType.LIKE) {
            log.debug("No mutual like - reverse swipe not found or not LIKE");
            return Optional.empty();
        }

        // Sprawdź czy match już nie istnieje (zabezpieczenie przed duplikatami)
        Optional<Match> existingMatch = matchRepository.findMatchBetween(
                swiper.getId(),
                swipedUser.getId()
        );

        if (existingMatch.isPresent()) {
            log.warn("Match already exists between users {} and {}",
                    swiper.getId(), swipedUser.getId());
            return existingMatch;
        }

        // Utwórz match
        Match match = detectMatch(swiper, swipedUser);

        return Optional.of(match);
    }

    /**
     * Tworzy match między użytkownikami.
     * WYWOŁANE TYLKO gdy oba swipe'y to LIKE.
     */
    @Transactional
    public Match detectMatch(User user1, User user2) {
        log.info("Creating match between users {} and {}", user1.getUsername(), user2.getUsername());

        // Sprawdź czy match już nie istnieje
        Optional<Match> existingMatch = matchRepository.findMatchBetween(user1.getId(), user2.getId());
        if (existingMatch.isPresent()) {
            log.warn("Match already exists, returning existing match");
            return existingMatch.get();
        }

        // Twórz match (user1 zawsze ma mniejsze ID dla konsystencji)
        Long userId1 = Math.min(user1.getId(), user2.getId());
        Long userId2 = Math.max(user1.getId(), user2.getId());

        User userWithSmallerId = userId1.equals(user1.getId()) ? user1 : user2;
        User userWithLargerId = userId1.equals(user1.getId()) ? user2 : user1;

        Match match = Match.builder()
                .user1(userWithSmallerId)
                .user2(userWithLargerId)
                .isActive(true)
                .build();

        Match savedMatch = matchRepository.save(match);

        log.info("Match created successfully: {} (id: {})",
                savedMatch.getId(), savedMatch.getId());

        return savedMatch;
    }

    // ========== READ OPERATIONS ==========

    /**
     * Znajduje wszystkie swipe'y użytkownika.
     */
    @Transactional(readOnly = true)
    public Page<SwipeResponse> getUserSwipes(Long userId, Pageable pageable) {
        log.debug("Finding swipes for user: {}", userId);

        Page<Swipe> swipes = swipeRepository.findUserSwipeHistory(userId, pageable);

        return swipes.map(swipe -> modelMapper.map(swipe, SwipeResponse.class));
    }

    /**
     * Znajduje wszystkie LIKE'i otrzymane przez użytkownika.
     */
    @Transactional(readOnly = true)
    public Page<SwipeResponse> getLikesReceived(Long userId, Pageable pageable) {
        log.debug("Finding likes received by user: {}", userId);

        Page<Swipe> likes = swipeRepository.findAllLikesReceived(userId, pageable);

        return likes.map(swipe -> modelMapper.map(swipe, SwipeResponse.class));
    }

    /**
     * Liczy swipe'y wykonane przez użytkownika.
     */
    @Transactional(readOnly = true)
    public long countUserSwipes(Long userId) {
        return swipeRepository.countReviewedProfiles(userId);
    }

    /**
     * Liczy LIKE'i wykonane przez użytkownika.
     */
    @Transactional(readOnly = true)
    public long countLikesMade(Long userId) {
        return swipeRepository.countLikesMade(userId);
    }

    /**
     * Liczy LIKE'i otrzymane przez użytkownika.
     */
    @Transactional(readOnly = true)
    public long countLikesReceived(Long userId) {
        return swipeRepository.countLikesReceived(userId);
    }

    // ========== HELPER METHODS ==========

    /**
     * Mapuje Match entity na MatchResponse DTO.
     */
    private MatchResponse mapToMatchResponse(Match match, Long currentUserId) {
        // Określ partnera (drugi user w matchu)
        Long partnerId = match.getUser1().getId().equals(currentUserId)
                ? match.getUser2().getId()
                : match.getUser1().getId();

        MatchResponse response = modelMapper.map(match, MatchResponse.class);
        response.setUserId(currentUserId);

        // TODO: Dodać więcej szczegółów (partner profile, messages count)
        // To będzie zrobione w MatchService (STEP 10)

        return response;
    }
}

