package AplikacjePrzemyslowe.DatApp;

import AplikacjePrzemyslowe.DatApp.dto.request.SwipeRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.MatchResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.SwipeResponse;
import AplikacjePrzemyslowe.DatApp.entity.Match;
import AplikacjePrzemyslowe.DatApp.entity.Swipe;
import AplikacjePrzemyslowe.DatApp.entity.SwipeType;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.InvalidSwipeException;
import AplikacjePrzemyslowe.DatApp.repository.MatchRepository;
import AplikacjePrzemyslowe.DatApp.repository.SwipeRepository;
import AplikacjePrzemyslowe.DatApp.service.SwipeService;
import AplikacjePrzemyslowe.DatApp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SwipeService unit tests")
class SwipeServiceTests {

    @Mock private SwipeRepository swipeRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private UserService userService;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private SwipeService swipeService;

    private User swiper;
    private User swiped;

    @BeforeEach
    void setUp() {
        swiper = User.builder().id(1L).username("u1").email("u1@x.pl").password("password123").isActive(true).build();
        swiped = User.builder().id(2L).username("u2").email("u2@x.pl").password("password123").isActive(true).build();
    }

    @Test
    @DisplayName("recordSwipe: cannot swipe yourself")
    void recordSwipe_cannotSwipeYourself() {
        SwipeRequest req = SwipeRequest.builder()
                .swipedUserId(1L)
                .swipeType("LIKE")
                .build();

        assertThatThrownBy(() -> swipeService.recordSwipe(1L, req))
                .isInstanceOf(InvalidSwipeException.class);

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("recordSwipe: cannot swipe same user twice")
    void recordSwipe_duplicateSwipe() {
        SwipeRequest req = SwipeRequest.builder()
                .swipedUserId(2L)
                .swipeType("LIKE")
                .build();

        when(userService.getUserEntity(1L)).thenReturn(swiper);
        when(userService.getUserEntity(2L)).thenReturn(swiped);
        when(swipeRepository.existsBySwipedUserIdAndSwiperId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> swipeService.recordSwipe(1L, req))
                .isInstanceOf(InvalidSwipeException.class);

        verify(swipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordSwipe: LIKE without reverse LIKE returns isMatch=false")
    void recordSwipe_likeNoMatch() {
        SwipeRequest req = SwipeRequest.builder()
                .swipedUserId(2L)
                .swipeType("LIKE")
                .build();

        when(userService.getUserEntity(1L)).thenReturn(swiper);
        when(userService.getUserEntity(2L)).thenReturn(swiped);
        when(swipeRepository.existsBySwipedUserIdAndSwiperId(2L, 1L)).thenReturn(false);

        Swipe saved = Swipe.builder().id(100L).swiper(swiper).swipedUser(swiped).swipeType(SwipeType.LIKE).swipedAt(LocalDateTime.now()).build();
        when(swipeRepository.save(any(Swipe.class))).thenReturn(saved);

        when(swipeRepository.findSwipe(2L, 1L)).thenReturn(Optional.empty());

        SwipeResponse resp = swipeService.recordSwipe(1L, req);

        assertThat(resp.getSwipeId()).isEqualTo(100L);
        assertThat(resp.getIsMatch()).isFalse();
        assertThat(resp.getMatchDetails()).isNull();
    }

    @Test
    @DisplayName("checkMutualLike: when reverse LIKE and no existing match -> creates match")
    void checkMutualLike_createsMatch() {
        Swipe reverse = Swipe.builder().id(200L).swiper(swiped).swipedUser(swiper).swipeType(SwipeType.LIKE).build();

        when(swipeRepository.findSwipe(2L, 1L)).thenReturn(Optional.of(reverse));
        when(matchRepository.findMatchBetween(1L, 2L)).thenReturn(Optional.empty());

        Match created = Match.builder().id(300L).user1(swiper).user2(swiped).isActive(true).build();
        when(matchRepository.save(any(Match.class))).thenReturn(created);

        Optional<Match> result = swipeService.checkMutualLike(swiper, swiped);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(300L);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("detectMatch: if existing match -> returns existing")
    void detectMatch_existing() {
        Match existing = Match.builder().id(400L).user1(swiper).user2(swiped).isActive(true).build();
        when(matchRepository.findMatchBetween(1L, 2L)).thenReturn(Optional.of(existing));

        Match result = swipeService.detectMatch(swiper, swiped);

        assertThat(result.getId()).isEqualTo(400L);
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("mapToMatchResponse path: checkMutualLike returns existing match")
    void recordSwipe_likeWithExistingMatch() {
        SwipeRequest req = SwipeRequest.builder()
                .swipedUserId(2L)
                .swipeType("LIKE")
                .build();

        when(userService.getUserEntity(1L)).thenReturn(swiper);
        when(userService.getUserEntity(2L)).thenReturn(swiped);
        when(swipeRepository.existsBySwipedUserIdAndSwiperId(2L, 1L)).thenReturn(false);

        Swipe saved = Swipe.builder().id(101L).swiper(swiper).swipedUser(swiped).swipeType(SwipeType.LIKE).swipedAt(LocalDateTime.now()).build();
        when(swipeRepository.save(any(Swipe.class))).thenReturn(saved);

        Swipe reverse = Swipe.builder().id(201L).swiper(swiped).swipedUser(swiper).swipeType(SwipeType.LIKE).build();
        when(swipeRepository.findSwipe(2L, 1L)).thenReturn(Optional.of(reverse));

        Match existing = Match.builder().id(401L).user1(swiper).user2(swiped).isActive(true).build();
        when(matchRepository.findMatchBetween(1L, 2L)).thenReturn(Optional.of(existing));

        MatchResponse mapped = MatchResponse.builder().id(401L).build();
        when(modelMapper.map(eq(existing), eq(MatchResponse.class))).thenReturn(mapped);

        SwipeResponse resp = swipeService.recordSwipe(1L, req);

        assertThat(resp.getIsMatch()).isTrue();
        assertThat(resp.getMatchDetails()).isNotNull();
        assertThat(resp.getMatchDetails().getId()).isEqualTo(401L);
        // partnerId nie jest ustawiany w SwipeService.mapToMatchResponse (TODO w kodzie), więc tego tu nie asercjonujemy
    }
}
