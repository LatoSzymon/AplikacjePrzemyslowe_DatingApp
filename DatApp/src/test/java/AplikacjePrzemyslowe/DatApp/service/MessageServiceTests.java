package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.MessageRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.MessageResponse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService unit tests")
class MessageServiceTests {

    @Mock private MessageRepository messageRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private MessageService messageService;

    private User u1;
    private User u2;
    private Match match;

    @BeforeEach
    void setUp() {
        u1 = User.builder().id(1L).username("u1").email("u1@x.pl").password("Password123").isActive(true).build();
        u2 = User.builder().id(2L).username("u2").email("u2@x.pl").password("Password123").isActive(true).build();
        match = Match.builder().id(10L).user1(u1).user2(u2).isActive(true).build();
    }

    @Test
    @DisplayName("sendMessage: throws when sender not found")
    void sendMessage_senderMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        MessageRequest req = MessageRequest.builder().matchId(10L).content("hi").build();

        assertThatThrownBy(() -> messageService.sendMessage(1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("sendMessage: throws when match missing")
    void sendMessage_matchMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(matchRepository.findById(10L)).thenReturn(Optional.empty());

        MessageRequest req = MessageRequest.builder().matchId(10L).content("hi").build();

        assertThatThrownBy(() -> messageService.sendMessage(1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("sendMessage: throws when sender not in match")
    void sendMessage_unauthorized() {
        when(userRepository.findById(999L)).thenReturn(Optional.of(User.builder().id(999L).username("uX").email("x@x.pl").password("Password123").isActive(true).build()));
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));

        MessageRequest req = MessageRequest.builder().matchId(10L).content("hi").build();

        assertThatThrownBy(() -> messageService.sendMessage(999L, req))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("sendMessage: saves message and maps response")
    void sendMessage_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));

        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });

        MessageResponse mapped = MessageResponse.builder().id(100L).content("hi").build();
        when(modelMapper.map(any(Message.class), eq(MessageResponse.class))).thenReturn(mapped);

        MessageRequest req = MessageRequest.builder().matchId(10L).content("hi").build();
        MessageResponse resp = messageService.sendMessage(1L, req);

        assertThat(resp.getId()).isEqualTo(100L);
        verify(messageRepository).save(argThat(m -> m.getSender().getId().equals(1L) && m.getReceiver().getId().equals(2L) && m.getContent().equals("hi") && Boolean.FALSE.equals(m.getIsRead())));
    }

    @Test
    @DisplayName("getConversation: throws when user not in match")
    void getConversation_unauthorized() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> messageService.getConversation(999L, 10L, PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("getConversation: returns page")
    void getConversation_ok() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));

        Page<Message> page = new PageImpl<>(List.of(
                Message.builder().id(1L).match(match).sender(u1).receiver(u2).content("a").isRead(false).build(),
                Message.builder().id(2L).match(match).sender(u2).receiver(u1).content("b").isRead(true).build()
        ), PageRequest.of(0, 10), 2);

        when(messageRepository.findByMatchOrderBySentAtAsc(eq(match), any())).thenReturn(page);
        when(modelMapper.map(any(Message.class), eq(MessageResponse.class))).thenReturn(new MessageResponse());

        Page<MessageResponse> resp = messageService.getConversation(1L, 10L, PageRequest.of(0, 10));

        assertThat(resp.getTotalElements()).isEqualTo(2);
        verify(messageRepository).findByMatchOrderBySentAtAsc(eq(match), any());
    }

    @Test
    @DisplayName("markAsRead: unauthorized when not receiver")
    void markAsRead_unauthorized() {
        Message m = Message.builder().id(5L).match(match).sender(u1).receiver(u2).content("x").isRead(false).build();
        when(messageRepository.findById(5L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> messageService.markAsRead(5L, 1L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("markAsRead: saves when unread")
    void markAsRead_ok() {
        Message m = Message.builder().id(5L).match(match).sender(u1).receiver(u2).content("x").isRead(false).build();
        when(messageRepository.findById(5L)).thenReturn(Optional.of(m));

        messageService.markAsRead(5L, 2L);

        verify(messageRepository).save(argThat(saved -> Boolean.TRUE.equals(saved.getIsRead())));
    }

    @Test
    @DisplayName("deleteConversation: deletes all messages")
    void deleteConversation_ok() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatch(match)).thenReturn(List.of(
                Message.builder().id(1L).match(match).build(),
                Message.builder().id(2L).match(match).build()
        ));

        messageService.deleteConversation(1L, 10L);

        verify(messageRepository).deleteAll(anyList());
    }
}

