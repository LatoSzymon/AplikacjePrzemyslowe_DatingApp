package AplikacjePrzemyslowe.DatApp.dao;

import AplikacjePrzemyslowe.DatApp.dao.mapper.MatchRowMapper;
import AplikacjePrzemyslowe.DatApp.entity.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO class dla Match.
 * Używa JdbcTemplate dla złożonych analytics i matching statistics.
 * Standardowe CRUD operacje pozostają w MatchRepository (JPA).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MatchJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MatchRowMapper matchRowMapper;

    // ========== MATCHING STATISTICS QUERIES ==========

    /**
     * Znajduje najpopularniejsze dopasowania (z największą liczbą wiadomości).
     */
    public Page<Match> findMostActiveMatches(Long userId, Pageable pageable) {

        log.debug("Finding most active matches for user {}", userId);

        String countSql = """
            SELECT COUNT(DISTINCT m.match_id)
            FROM matches m
            WHERE (m.user1_id = :userId OR m.user2_id = :userId)
            AND m.is_active = TRUE
            """;

        String selectSql = """
            SELECT 
                m.match_id, m.user1_id, m.user2_id, m.is_active, 
                m.matched_at, m.unmatched_at,
                COUNT(DISTINCT msg.message_id) as msg_count
            FROM matches m
            LEFT JOIN messages msg ON msg.match_id = m.match_id
            WHERE (m.user1_id = :userId OR m.user2_id = :userId)
            AND m.is_active = TRUE
            GROUP BY m.match_id
            ORDER BY msg_count DESC, m.matched_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        List<Match> content = jdbcTemplate.query(selectSql, params, matchRowMapper);

        log.debug("Found {} active matches for user {}", content.size(), userId);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Znajduje najnowsze dopasowania (z ostatnich N dni).
     */
    public List<Match> findRecentMatches(Long userId, int daysBack) {

        log.debug("Finding matches from last {} days for user {}", daysBack, userId);

        String sql = """
            SELECT m.match_id, m.user1_id, m.user2_id, m.is_active, m.matched_at, m.unmatched_at
            FROM matches m
            WHERE (m.user1_id = :userId OR m.user2_id = :userId)
            AND m.is_active = TRUE
            AND m.matched_at >= DATE_SUB(NOW(), INTERVAL :days DAY)
            ORDER BY m.matched_at DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("days", daysBack);

        return jdbcTemplate.query(sql, params, matchRowMapper);
    }

    /**
     * Znajduje dopasowania bez żadnych wiadomości (niezaczęte rozmowy).
     */
    public Page<Match> findMatchesWithoutMessages(Long userId, Pageable pageable) {

        log.debug("Finding matches without messages for user {}", userId);

        String countSql = """
            SELECT COUNT(DISTINCT m.match_id)
            FROM matches m
            WHERE (m.user1_id = :userId OR m.user2_id = :userId)
            AND m.is_active = TRUE
            AND NOT EXISTS (
                SELECT 1 FROM messages msg WHERE msg.match_id = m.match_id
            )
            """;

        String selectSql = """
            SELECT m.match_id, m.user1_id, m.user2_id, m.is_active, m.matched_at, m.unmatched_at
            FROM matches m
            WHERE (m.user1_id = :userId OR m.user2_id = :userId)
            AND m.is_active = TRUE
            AND NOT EXISTS (
                SELECT 1 FROM messages msg WHERE msg.match_id = m.match_id
            )
            ORDER BY m.matched_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        List<Match> content = jdbcTemplate.query(selectSql, params, matchRowMapper);

        log.debug("Found {} matches without messages for user {}", content.size(), userId);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Analityka: Liczba matchów per użytkownika.
     */
    public List<Object[]> getMatchStatistics() {

        log.debug("Getting match statistics");

        String sql = """
            SELECT 
                u.user_id,
                u.username,
                COUNT(DISTINCT CASE 
                    WHEN (m.user1_id = u.user_id OR m.user2_id = u.user_id) AND m.is_active = TRUE 
                    THEN m.match_id 
                END) as active_matches,
                COUNT(DISTINCT CASE 
                    WHEN (m.user1_id = u.user_id OR m.user2_id = u.user_id) AND m.is_active = FALSE 
                    THEN m.match_id 
                END) as unmatchcd_count
            FROM users u
            LEFT JOIN matches m ON (u.user_id = m.user1_id OR u.user_id = m.user2_id)
            WHERE u.is_active = TRUE
            GROUP BY u.user_id
            ORDER BY active_matches DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getInt("active_matches"),
            rs.getInt("unmatchcd_count")
        });
    }

    /**
     * Analityka: Średni czas od matcha do pierwszej wiadomości.
     */
    public List<Object[]> getAverageTimeToFirstMessage() {

        log.debug("Getting average time to first message statistics");

        String sql = """
            SELECT 
                AVG(TIMESTAMPDIFF(HOUR, m.matched_at, first_msg.sent_at)) as avg_hours,
                MIN(TIMESTAMPDIFF(HOUR, m.matched_at, first_msg.sent_at)) as min_hours,
                MAX(TIMESTAMPDIFF(HOUR, m.matched_at, first_msg.sent_at)) as max_hours
            FROM matches m
            JOIN (
                SELECT match_id, MIN(sent_at) as sent_at
                FROM messages
                GROUP BY match_id
            ) first_msg ON m.match_id = first_msg.match_id
            WHERE m.is_active = TRUE
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
            rs.getDouble("avg_hours"),
            rs.getInt("min_hours"),
            rs.getInt("max_hours")
        });
    }

    /**
     * Update: Zmiana statusu matcha na unmatch.
     */
    public int unmatchUsers(Long matchId) {

        log.info("Unmatching match id: {}", matchId);

        String sql = """
            UPDATE matches 
            SET is_active = FALSE, unmatched_at = NOW()
            WHERE match_id = :matchId AND is_active = TRUE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId);

        int rowsAffected = jdbcTemplate.update(sql, params);
        log.debug("Unmatched {} match(es)", rowsAffected);

        return rowsAffected;
    }

    /**
     * Insert: Tworzenie nowego matcha.
     */
    public int createMatch(Long user1Id, Long user2Id) {

        log.info("Creating match between users {} and {}", user1Id, user2Id);

        String sql = """
            INSERT INTO matches (user1_id, user2_id, is_active, matched_at)
            VALUES (:user1Id, :user2Id, TRUE, NOW())
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user1Id", user1Id)
                .addValue("user2Id", user2Id);

        return jdbcTemplate.update(sql, params);
    }

    /**
     * Delete: Usunięcie matcha (wraz z wiadomościami przez kaskadowe FK).
     */
    public int deleteMatch(Long matchId) {

        log.warn("Deleting match: {}", matchId);

        String sql = "DELETE FROM matches WHERE match_id = :matchId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId);

        return jdbcTemplate.update(sql, params);
    }

    /**
     * Query: Sprawdzenie czy match istnieje między dwoma użytkownikami.
     */
    public boolean existsMatchBetween(Long user1Id, Long user2Id) {

        String sql = """
            SELECT COUNT(*) 
            FROM matches 
            WHERE (user1_id = :user1Id AND user2_id = :user2Id)
            OR (user1_id = :user2Id AND user2_id = :user1Id)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user1Id", user1Id)
                .addValue("user2Id", user2Id);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Query: Pobieranie ID partnera w dopasowaniu.
     */
    public Long getPartnerUserId(Long matchId, Long currentUserId) {

        String sql = """
            SELECT CASE 
                WHEN user1_id = :currentUserId THEN user2_id 
                ELSE user1_id 
            END as partner_id
            FROM matches 
            WHERE match_id = :matchId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("matchId", matchId)
                .addValue("currentUserId", currentUserId);

        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    /**
     * Query: Liczba aktywnych matchów dla użytkownika.
     */
    public int countActiveMatches(Long userId) {

        String sql = """
            SELECT COUNT(*) 
            FROM matches 
            WHERE (user1_id = :userId OR user2_id = :userId) 
            AND is_active = TRUE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }
}

