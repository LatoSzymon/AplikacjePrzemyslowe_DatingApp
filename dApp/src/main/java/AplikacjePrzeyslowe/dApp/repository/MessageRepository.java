package AplikacjePrzeyslowe.dApp.repository;

import AplikacjePrzeyslowe.dApp.entity.Match;
import AplikacjePrzeyslowe.dApp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository dla encji Message.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ========== FINDER METHODS ==========

    /**
     * Znajduje wszystkie wiadomości w danym dopasowaniu
     */
    List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);

    /**
     * Liczy wszystkie wiadomości w dopasowaniu
     */
    long countByMatchId(Long matchId);

    /**
     * Znajduje wiadomości wysłane przez użytkownika
     */
    List<Message> findBySenderId(Long senderId);

    /**
     * Liczy wiadomości wysłane przez użytkownika
     */
    long countBySenderId(Long senderId);

    /**
     * Liczy nieprzeczytane wiadomości dla danego dopasowania
     */
    long countByMatchIdAndIsReadFalse(Long matchId);

    // ========== CUSTOM QUERIES ==========

    /**
     * Znajduje wszystkie wiadomości w konwersacji między dwoma użytkownikami
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.match.id = :matchId
        ORDER BY m.sentAt ASC
        """)
    Page<Message> findConversation(@Param("matchId") Long matchId, Pageable pageable);

    /**
     * Znajduje ostatnią wiadomość w konwersacji
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.match.id = :matchId
        ORDER BY m.sentAt DESC
        LIMIT 1
        """)
    Message findLastMessageInMatch(@Param("matchId") Long matchId);

    /**
     * Znajduje wiadomości wysłane w określonym przedziale czasowym
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.match.id = :matchId
        AND m.sentAt BETWEEN :startDate AND :endDate
        ORDER BY m.sentAt ASC
        """)
    List<Message> findMessagesBetween(
        @Param("matchId") Long matchId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Znajduje wiadomości zawierające określony tekst
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.match.id = :matchId
        AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%'))
        ORDER BY m.sentAt DESC
        """)
    List<Message> searchInConversation(
        @Param("matchId") Long matchId,
        @Param("searchText") String searchText
    );

    /**
     * Liczy wiadomości od konkretnego użytkownika w dopasowaniu
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.match.id = :matchId
        AND m.sender.id = :userId
        """)
    long countMessagesByUserInMatch(
        @Param("matchId") Long matchId,
        @Param("userId") Long userId
    );

    /**
     * Znajduje nieprzeczytane wiadomości od konkretnego użytkownika
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.match.id = :matchId
        AND m.sender.id = :senderId
        AND m.isRead = false
        ORDER BY m.sentAt ASC
        """)
    List<Message> findUnreadMessagesFromUser(
        @Param("matchId") Long matchId,
        @Param("senderId") Long senderId
    );

    /**
     * Liczy wiadomości wysłane przez użytkownika do wszystkich partnerów
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.sender.id = :userId
        """)
    long countTotalMessagesSent(@Param("userId") Long userId);
}


