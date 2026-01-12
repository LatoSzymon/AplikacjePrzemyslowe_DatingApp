package AplikacjePrzeyslowe.dApp.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO class dla Message.
 * Używa JdbcTemplate dla złożonych chat analytics i bulk operations.
 * Standardowe CRUD operacje pozostają w MessageRepository (JPA).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MessageJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // ========== MESSAGE ANALYTICS QUERIES ==========

    /**
     * Znajduje nieprzeczytane wiadomości dla użytkownika w danym matchu.
     */
    public int countUnreadMessages(Long userId, Long matchId) {

        log.debug("Counting unread messages for user {} in match {}", userId, matchId);

        String sql = """
            SELECT COUNT(*) 
            FROM messages msg
            JOIN matches m ON m.match_id = msg.match_id
            WHERE msg.match_id = :matchId
            AND msg.is_read = FALSE
            AND msg.sender_id != :userId
            AND (m.user1_id = :userId OR m.user2_id = :userId)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("matchId", matchId);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Liczy całkowite nieprzeczytane wiadomości dla użytkownika.
     */
    public int countTotalUnreadMessages(Long userId) {

        log.debug("Counting total unread messages for user {}", userId);

        String sql = """
            SELECT COUNT(*) 
            FROM messages msg
            JOIN matches m ON m.match_id = msg.match_id
            WHERE msg.is_read = FALSE
            AND msg.sender_id != :userId
            AND (m.user1_id = :userId OR m.user2_id = :userId)
            AND m.is_active = TRUE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Bulk operation: Oznaczenie wszystkich wiadomości w matchu jako przeczytane.
     */
    public int markAllMessagesAsRead(Long matchId, Long userId) {

        log.info("Marking all messages as read in match {} for user {}", matchId, userId);

        String sql = """
            UPDATE messages 
            SET is_read = TRUE, read_at = NOW()
            WHERE match_id = :matchId
            AND sender_id != :userId
            AND is_read = FALSE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId)
                .addValue("userId", userId);

        int rowsAffected = jdbcTemplate.update(sql, params);
        log.debug("Marked {} messages as read", rowsAffected);

        return rowsAffected;
    }

    /**
     * Bulk operation: Usunięcie całej konwersacji (wszystkie wiadomości w matchu).
     */
    public int deleteConversation(Long matchId) {

        log.warn("Deleting conversation for match {}", matchId);

        String sql = "DELETE FROM messages WHERE match_id = :matchId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId);

        int rowsAffected = jdbcTemplate.update(sql, params);
        log.debug("Deleted {} messages", rowsAffected);

        return rowsAffected;
    }

    /**
     * Analityka: Średnia liczba wiadomości na konwersację.
     */
    public List<Object[]> getMessagingStatistics(Long userId) {

        log.debug("Getting messaging statistics for user {}", userId);

        String sql = """
            SELECT 
                COUNT(DISTINCT msg.match_id) as conversation_count,
                COUNT(msg.message_id) as total_messages,
                AVG(msg_per_conv.msg_count) as avg_msg_per_conversation,
                MAX(msg_per_conv.msg_count) as max_msg_per_conversation
            FROM (
                SELECT match_id, COUNT(*) as msg_count
                FROM messages
                WHERE match_id IN (
                    SELECT match_id FROM matches 
                    WHERE user1_id = :userId OR user2_id = :userId
                )
                GROUP BY match_id
            ) msg_per_conv
            JOIN messages msg ON msg.match_id = msg_per_conv.match_id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Object[]{
            rs.getInt("conversation_count"),
            rs.getInt("total_messages"),
            rs.getDouble("avg_msg_per_conversation"),
            rs.getInt("max_msg_per_conversation")
        });
    }

    /**
     * Analityka: Najczęstszych nadawców (top senders).
     */
    public List<Object[]> getTopMessageSenders(int limit) {

        log.debug("Getting top {} message senders", limit);

        String sql = """
            SELECT 
                u.user_id,
                u.username,
                COUNT(msg.message_id) as message_count
            FROM messages msg
            JOIN users u ON u.user_id = msg.sender_id
            GROUP BY u.user_id
            ORDER BY message_count DESC
            LIMIT :limit
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Object[]{
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getInt("message_count")
        });
    }

    /**
     * Analityka: Średni czas między wiadomościami w konwersacji.
     */
    public List<Object[]> getAverageResponseTime() {

        log.debug("Getting average response time statistics");

        String sql = """
            SELECT 
                AVG(TIMESTAMPDIFF(MINUTE, m1.sent_at, m2.sent_at)) as avg_response_minutes,
                MIN(TIMESTAMPDIFF(MINUTE, m1.sent_at, m2.sent_at)) as min_response_minutes,
                MAX(TIMESTAMPDIFF(MINUTE, m1.sent_at, m2.sent_at)) as max_response_minutes
            FROM messages m1
            JOIN messages m2 ON m1.match_id = m2.match_id 
                AND m1.sender_id != m2.sender_id
                AND m2.sent_at > m1.sent_at
                AND NOT EXISTS (
                    SELECT 1 FROM messages m3 
                    WHERE m3.match_id = m1.match_id 
                    AND m3.sender_id = m2.sender_id
                    AND m3.sent_at BETWEEN m1.sent_at AND m2.sent_at
                )
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
            rs.getDouble("avg_response_minutes"),
            rs.getInt("min_response_minutes"),
            rs.getInt("max_response_minutes")
        });
    }

    /**
     * Wyszukiwanie: Wiadomości zawierające określony tekst w danym matchu.
     */
    public List<Object[]> searchMessagesInConversation(Long matchId, String searchText) {

        log.debug("Searching for '{}' in match {}", searchText, matchId);

        String sql = """
            SELECT 
                msg.message_id,
                msg.sender_id,
                u.username,
                msg.content,
                msg.sent_at
            FROM messages msg
            JOIN users u ON u.user_id = msg.sender_id
            WHERE msg.match_id = :matchId
            AND LOWER(msg.content) LIKE LOWER(CONCAT('%', :searchText, '%'))
            ORDER BY msg.sent_at DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId)
                .addValue("searchText", searchText);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Object[]{
            rs.getLong("message_id"),
            rs.getLong("sender_id"),
            rs.getString("username"),
            rs.getString("content"),
            rs.getObject("sent_at", LocalDateTime.class)
        });
    }

    /**
     * Query: Pobieranie ostatniej wiadomości w konwersacji.
     */
    public Object[] getLastMessage(Long matchId) {

        String sql = """
            SELECT 
                msg.message_id,
                msg.sender_id,
                u.username,
                msg.content,
                msg.sent_at
            FROM messages msg
            JOIN users u ON u.user_id = msg.sender_id
            WHERE msg.match_id = :matchId
            ORDER BY msg.sent_at DESC
            LIMIT 1
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId);

        List<Object[]> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new Object[]{
            rs.getLong("message_id"),
            rs.getLong("sender_id"),
            rs.getString("username"),
            rs.getString("content"),
            rs.getObject("sent_at", LocalDateTime.class)
        });

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Query: Liczba wiadomości wysłanych przez użytkownika.
     */
    public int countMessagesSentByUser(Long userId) {

        String sql = "SELECT COUNT(*) FROM messages WHERE sender_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Bulk: Usunięcie starych wiadomości (starsze niż N dni).
     * Przydatne dla konserwacji bazy danych.
     */
    public int deleteOldMessages(int daysOld) {

        log.warn("Deleting messages older than {} days", daysOld);

        String sql = """
            DELETE FROM messages 
            WHERE sent_at < DATE_SUB(NOW(), INTERVAL :days DAY)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("days", daysOld);

        int rowsAffected = jdbcTemplate.update(sql, params);
        log.debug("Deleted {} old messages", rowsAffected);

        return rowsAffected;
    }
}

