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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serwis do zarządzania wiadomościami między dopasowanymi użytkownikami.
 * Implementuje wysyłanie, odbieranie i usuwanie wiadomości tekstowych.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Wysyła wiadomość tekstową do dopasowanego użytkownika.
     *
     * @param senderId ID użytkownika wysyłającego
     * @param request DTO z treścią wiadomości i ID matcha
     * @return Response z wysłaną wiadomością
     * @throws ResourceNotFoundException gdy match nie istnieje
     * @throws UnauthorizedException gdy użytkownik nie należy do matcha
     */
    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        log.info("User {} sending message to match {}", senderId, request.getMatchId());

        // Pobierz nadawcę
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + senderId));

        // Pobierz match i zweryfikuj uprawnienia
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + request.getMatchId()));

        // Sprawdź czy użytkownik należy do tego matcha
        if (!match.getUser1().getId().equals(senderId) && !match.getUser2().getId().equals(senderId)) {
            log.error("User {} attempted to send message to unauthorized match {}", senderId, request.getMatchId());
            throw new UnauthorizedException("You are not part of this match");
        }

        // Określ odbiorcę
        User receiver = match.getUser1().getId().equals(senderId) ? match.getUser2() : match.getUser1();

        // Utwórz wiadomość
        Message message = Message.builder()
                .match(match)
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message {} sent successfully from user {} to user {}",
                savedMessage.getId(), senderId, receiver.getId());

        return modelMapper.map(savedMessage, MessageResponse.class);
    }

    /**
     * Pobiera historię konwersacji dla danego matcha (paginowana i posortowana chronologicznie).
     *
     * @param userId ID użytkownika żądającego
     * @param matchId ID matcha
     * @param pageable Parametry paginacji
     * @return Strona wiadomości posortowana chronologicznie
     * @throws ResourceNotFoundException gdy match nie istnieje
     * @throws UnauthorizedException gdy użytkownik nie należy do matcha
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversation(Long userId, Long matchId, Pageable pageable) {
        log.debug("User {} fetching conversation for match {}", userId, matchId);

        // Pobierz match i zweryfikuj uprawnienia
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} attempted to access unauthorized conversation {}", userId, matchId);
            throw new UnauthorizedException("You are not part of this match");
        }

        // Pobierz wiadomości posortowane chronologicznie (od najstarszych)
        Page<Message> messages = messageRepository.findByMatchOrderBySentAtAsc(match, pageable);

        log.debug("Retrieved {} messages for match {}", messages.getTotalElements(), matchId);

        return messages.map(message -> modelMapper.map(message, MessageResponse.class));
    }

    /**
     * Oznacza wiadomość jako przeczytaną.
     *
     * @param messageId ID wiadomości
     * @param userId ID użytkownika (musi być odbiorcą)
     * @throws ResourceNotFoundException gdy wiadomość nie istnieje
     * @throws UnauthorizedException gdy użytkownik nie jest odbiorcą
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        log.debug("Marking message {} as read by user {}", messageId, userId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        // Sprawdź czy użytkownik jest odbiorcą
        if (!message.getReceiver().getId().equals(userId)) {
            log.error("User {} attempted to mark message {} as read (not receiver)", userId, messageId);
            throw new UnauthorizedException("You can only mark your own messages as read");
        }

        if (!message.getIsRead()) {
            message.setIsRead(true);
            messageRepository.save(message);
            log.info("Message {} marked as read by user {}", messageId, userId);
        }
    }

    /**
     * Usuwa całą konwersację (wszystkie wiadomości) dla danego matcha.
     *
     * @param userId ID użytkownika żądającego
     * @param matchId ID matcha
     * @throws ResourceNotFoundException gdy match nie istnieje
     * @throws UnauthorizedException gdy użytkownik nie należy do matcha
     */
    @Transactional
    public void deleteConversation(Long userId, Long matchId) {
        log.info("User {} deleting conversation for match {}", userId, matchId);

        // Pobierz match i zweryfikuj uprawnienia
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} attempted to delete unauthorized conversation {}", userId, matchId);
            throw new UnauthorizedException("You are not part of this match");
        }

        // Usuń wszystkie wiadomości
        List<Message> messages = messageRepository.findByMatch(match);
        int deletedCount = messages.size();
        messageRepository.deleteAll(messages);

        log.info("Deleted {} messages from conversation {} by user {}", deletedCount, matchId, userId);
    }

    /**
     * Pobiera liczbę nieprzeczytanych wiadomości dla użytkownika.
     *
     * @param userId ID użytkownika
     * @return Liczba nieprzeczytanych wiadomości
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long userId) {
        log.debug("Counting unread messages for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long count = messageRepository.countByReceiverAndIsReadFalse(user);
        log.debug("User {} has {} unread messages", userId, count);

        return count;
    }
}

