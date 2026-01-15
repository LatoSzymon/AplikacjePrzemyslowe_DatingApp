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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis do zarządzania dopasowaniami (matches) między użytkownikami.
 * Implementuje listowanie dopasowań i mechanizm 'unmatch'.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Pobiera wszystkie dopasowania użytkownika (paginowane).
     *
     * @param userId ID użytkownika
     * @param pageable Parametry paginacji
     * @return Strona dopasowań użytkownika
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    @Transactional(readOnly = true)
    public Page<MatchResponse> getMatches(Long userId, Pageable pageable) {
        log.info("Fetching matches for user {} with pagination: page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // Sprawdź czy użytkownik istnieje
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Pobierz wszystkie matche gdzie użytkownik jest user1 lub user2
        Page<Match> matches = matchRepository.findByUser1OrUser2(user, user, pageable);

        log.info("Found {} total matches for user {}", matches.getTotalElements(), userId);

        return matches.map(match -> {
            MatchResponse response = modelMapper.map(match, MatchResponse.class);

            // Określ partnera (drugi użytkownik w matchu)
            User partner = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();
            response.setPartnerName(partner.getUsername());
            response.setPartnerId(partner.getId());

            return response;
        });
    }

    /**
     * Pobiera szczegóły pojedynczego matcha.
     *
     * @param matchId ID matcha
     * @param userId ID użytkownika żądającego
     * @return Szczegóły matcha
     * @throws ResourceNotFoundException gdy match nie istnieje
     * @throws UnauthorizedException gdy użytkownik nie należy do matcha
     */
    @Transactional(readOnly = true)
    public MatchResponse getMatchById(Long matchId, Long userId) {
        log.debug("User {} fetching match details for match {}", userId, matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        // Sprawdź uprawnienia
        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} attempted to access unauthorized match {}", userId, matchId);
            throw new UnauthorizedException("You are not part of this match");
        }

        MatchResponse response = modelMapper.map(match, MatchResponse.class);

        // Ustaw partnera
        User partner = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();
        response.setPartnerName(partner.getUsername());
        response.setPartnerId(partner.getId());

        return response;
    }

    /**
     * Usuwa dopasowanie (unmatch) i kaskadowo usuwa wszystkie powiązane wiadomości.
     * Blokuje możliwość dalszej rozmowy między użytkownikami.
     *
     * @param matchId ID matcha do usunięcia
     * @param userId ID użytkownika żądającego (musi być częścią matcha)
     * @throws ResourceNotFoundException gdy match nie istnieje
     * @throws UnauthorizedException gdy użytkownik nie należy do matcha
     */
    @Transactional
    public void unmatch(Long matchId, Long userId) {
        log.info("User {} initiating unmatch for match {}", userId, matchId);

        // Pobierz match i zweryfikuj uprawnienia
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} attempted to unmatch unauthorized match {}", userId, matchId);
            throw new UnauthorizedException("You are not part of this match");
        }

        // Określ partnera (drugi użytkownik)
        User partner = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();

        // Krok 1: Usuń wszystkie wiadomości w konwersacji (cascade delete)
        List<Message> messages = messageRepository.findByMatch(match);
        int messageCount = messages.size();

        if (!messages.isEmpty()) {
            messageRepository.deleteAll(messages);
            log.info("Cascade deleted {} messages from match {}", messageCount, matchId);
        }

        // Krok 2: Usuń sam match
        matchRepository.delete(match);

        log.info("Unmatch completed: Match {} deleted by user {}. Partner: {}. Messages deleted: {}",
                matchId, userId, partner.getId(), messageCount);

        // TODO: Wysłać notyfikację do partnera o unmatch (implementacja w przyszłości)
        notifyPartnerAboutUnmatch(partner, userId);
    }

    /**
     * Wysyła notyfikację do partnera o unmatch.
     * (Placeholder - implementacja zależna od systemu notyfikacji)
     *
     * @param partner Partner który otrzyma notyfikację
     * @param initiatorId ID użytkownika który zainicjował unmatch
     */
    private void notifyPartnerAboutUnmatch(User partner, Long initiatorId) {
        // TODO: Implementacja systemu notyfikacji (email, push notification, in-app)
        log.info("Notification sent to user {} about unmatch initiated by user {}",
                partner.getId(), initiatorId);
    }

    /**
     * Sprawdza czy użytkownicy są dopasowani.
     *
     * @param userId1 ID pierwszego użytkownika
     * @param userId2 ID drugiego użytkownika
     * @return true jeśli istnieje aktywny match, false w przeciwnym razie
     */
    @Transactional(readOnly = true)
    public boolean areMatched(Long userId1, Long userId2) {
        log.debug("Checking if users {} and {} are matched", userId1, userId2);

        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId1));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId2));

        return matchRepository.findByUsers(user1, user2).isPresent();
    }

    /**
     * Pobiera liczbę wszystkich dopasowań użytkownika.
     *
     * @param userId ID użytkownika
     * @return Liczba dopasowań
     */
    @Transactional(readOnly = true)
    public long getMatchCount(Long userId) {
        log.debug("Counting matches for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long count = matchRepository.countByUser1OrUser2(user, user);
        log.debug("User {} has {} matches", userId, count);

        return count;
    }
}

