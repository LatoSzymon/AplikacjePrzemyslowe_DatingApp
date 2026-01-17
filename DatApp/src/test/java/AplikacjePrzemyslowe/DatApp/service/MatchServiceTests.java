package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.response.MatchResponse;
import AplikacjePrzemyslowe.DatApp.entity.Match;
import AplikacjePrzemyslowe.DatApp.entity.Message;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.exception.UnauthorizedException;
import AplikacjePrzemyslowe.DatApp.repository.MatchRepository;
import AplikacjePrzemyslowe.DatApp.repository.MessageRepository;
import AplikacjePrzemyslowe.DatApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService unit tests")
class MatchServiceTests {

    @Mock private MatchRepository matchRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private MatchService matchService;

    private User u1;
    private User u2;
    private Match m;

    @BeforeEach
    void setUp() {
        u1 = User.builder().id(1L).username("u1").email("u1@x.pl").password("password123").build();
        u2 = User.builder().id(2L).username("u2").email("u2@x.pl").password("password123").build();
        m = Match.builder().id(10L).user1(u1).user2(u2).isActive(true).build();
    }

    @Test
    @DisplayName("getMatches: returns page with partner fields")
    void getMatches_mapsPartner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(matchRepository.findByUser1OrUser2(eq(u1), eq(u1), any())).thenReturn(new PageImpl<>(List.of(m), PageRequest.of(0, 10), 1));
        when(modelMapper.map(eq(m), eq(MatchResponse.class))).thenReturn(MatchResponse.builder().id(10L).build());

        Page<MatchResponse> result = matchService.getMatches(1L, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        MatchResponse r = result.getContent().get(0);
        assertThat(r.getPartnerId()).isEqualTo(2L);
        assertThat(r.getPartnerName()).isEqualTo("u2");
    }

    @Test
    @DisplayName("getMatchById: unauthorized when requester not in match")
    void getMatchById_unauthorized() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> matchService.getMatchById(10L, 999L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("unmatch: deletes messages and match")
    void unmatch_deletesConversationAndMatch() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(m));
        when(messageRepository.findByMatch(m)).thenReturn(List.of(
                Message.builder().id(1L).match(m).content("hi").build(),
                Message.builder().id(2L).match(m).content("yo").build()
        ));

        matchService.unmatch(10L, 1L);

        verify(messageRepository).deleteAll(anyList());
        verify(matchRepository).delete(m);
    }

    @Test
    @DisplayName("areMatched: throws when user missing")
    void areMatched_userMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.areMatched(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getMatchCount: returns count")
    void getMatchCount_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(matchRepository.countByUser1OrUser2(u1, u1)).thenReturn(5L);

        assertThat(matchService.getMatchCount(1L)).isEqualTo(5L);
    }
}

