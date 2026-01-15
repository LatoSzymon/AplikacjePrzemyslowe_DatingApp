package AplikacjePrzemyslowe.DatApp.dao;

import AplikacjePrzemyslowe.DatApp.dao.mapper.UserRowMapper;
import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO class dla User.
 * Używa JdbcTemplate dla złożonych matching queries.
 * Standardowe CRUD operacje pozostają w UserRepository (JPA).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    // ========== MATCHING ALGORITHM QUERIES ==========

    /**
     * Znajduje kandydatów spełniających preferencje użytkownika.
     * Query filtruje po:
     * - Płci (z preferencji)
     * - Wieku (min/max z preferencji)
     * - Już ocenionych profilach (exclude swipe'y)
     * - Tylko aktywni użytkownicy
     */
    public Page<User> findCandidatesByPreference(
            Long currentUserId,
            Gender preferredGender,
            int minAge,
            int maxAge,
            Pageable pageable) {

        log.debug("Finding candidates for user {} with preferences: gender={}, age={}-{}",
                currentUserId, preferredGender, minAge, maxAge);

        // Query do zliczania total
        String countSql = """
            SELECT COUNT(DISTINCT u.user_id) 
            FROM users u
            WHERE u.user_id != :currentUserId
            AND u.is_active = TRUE
            AND u.gender = :gender
            AND YEAR(CURDATE()) - YEAR(u.birth_date) BETWEEN :minAge AND :maxAge
            AND u.user_id NOT IN (
                SELECT DISTINCT s.swiped_user_id 
                FROM swipes s 
                WHERE s.swiper_id = :currentUserId
            )
            """;

        // Query do pobrania danych z paginacją
        String selectSql = """
            SELECT u.user_id, u.username, u.email, u.password, u.gender, 
                   u.birth_date, u.city, u.is_active, u.created_at, u.updated_at
            FROM users u
            WHERE u.user_id != :currentUserId
            AND u.is_active = TRUE
            AND u.gender = :gender
            AND YEAR(CURDATE()) - YEAR(u.birth_date) BETWEEN :minAge AND :maxAge
            AND u.user_id NOT IN (
                SELECT DISTINCT s.swiped_user_id 
                FROM swipes s 
                WHERE s.swiper_id = :currentUserId
            )
            ORDER BY u.created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentUserId", currentUserId)
                .addValue("gender", preferredGender.name())
                .addValue("minAge", minAge)
                .addValue("maxAge", maxAge)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        List<User> content = jdbcTemplate.query(selectSql, params, userRowMapper);

        log.debug("Found {} candidates for user {} (page {}/{})",
                content.size(), currentUserId, pageable.getPageNumber(), pageable.getPageSize());

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Znajduje kandydatów z wspólnymi zainteresowaniami.
     * Sortuje po liczbie wspólnych zainteresowań (malejąco).
     */
    public Page<User> findCandidatesByCommonInterests(
            Long currentUserId,
            Gender preferredGender,
            int minAge,
            int maxAge,
            Pageable pageable) {

        log.debug("Finding candidates with common interests for user {}", currentUserId);

        String countSql = """
            SELECT COUNT(DISTINCT u.user_id)
            FROM users u
            WHERE u.user_id != :currentUserId
            AND u.is_active = TRUE
            AND u.gender = :gender
            AND YEAR(CURDATE()) - YEAR(u.birth_date) BETWEEN :minAge AND :maxAge
            AND u.user_id NOT IN (
                SELECT DISTINCT s.swiped_user_id 
                FROM swipes s 
                WHERE s.swiper_id = :currentUserId
            )
            AND u.user_id IN (
                SELECT DISTINCT p.user_id
                FROM profiles p
                WHERE p.user_id IN (
                    SELECT DISTINCT pi.profile_id
                    FROM profile_interests pi
                    WHERE pi.interest_id IN (
                        SELECT pi2.interest_id
                        FROM profile_interests pi2
                        WHERE pi2.profile_id = (
                            SELECT p2.profile_id FROM profiles p2 WHERE p2.user_id = :currentUserId
                        )
                    )
                )
            )
            """;

        String selectSql = """
            SELECT u.user_id, u.username, u.email, u.password, u.gender, 
                   u.birth_date, u.city, u.is_active, u.created_at, u.updated_at,
                   COUNT(DISTINCT pi.interest_id) as common_interests_count
            FROM users u
            JOIN profiles p ON p.user_id = u.user_id
            JOIN profile_interests pi ON pi.profile_id = p.profile_id
            WHERE u.user_id != :currentUserId
            AND u.is_active = TRUE
            AND u.gender = :gender
            AND YEAR(CURDATE()) - YEAR(u.birth_date) BETWEEN :minAge AND :maxAge
            AND u.user_id NOT IN (
                SELECT DISTINCT s.swiped_user_id 
                FROM swipes s 
                WHERE s.swiper_id = :currentUserId
            )
            AND pi.interest_id IN (
                SELECT pi2.interest_id
                FROM profile_interests pi2
                WHERE pi2.profile_id = (
                    SELECT p2.profile_id FROM profiles p2 WHERE p2.user_id = :currentUserId
                )
            )
            GROUP BY u.user_id
            ORDER BY common_interests_count DESC, u.created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentUserId", currentUserId)
                .addValue("gender", preferredGender.name())
                .addValue("minAge", minAge)
                .addValue("maxAge", maxAge)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        List<User> content = jdbcTemplate.query(selectSql, params, userRowMapper);

        log.info("Found {} candidates with common interests for user {}", content.size(), currentUserId);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Znajduje użytkowników z największą liczbą wspólnych zainteresowań.
     * Zwraca użytkownika i liczbę wspólnych zainteresowań.
     */
    public List<Object[]> findUsersWithCommonInterestCounts(Long currentUserId, int limit) {

        log.debug("Finding users with common interest counts for user {}", currentUserId);

        String sql = """
            SELECT 
                u.user_id,
                u.username,
                COUNT(DISTINCT pi.interest_id) as common_count
            FROM users u
            JOIN profiles p ON p.user_id = u.user_id
            JOIN profile_interests pi ON pi.profile_id = p.profile_id
            WHERE u.user_id != :currentUserId
            AND u.is_active = TRUE
            AND pi.interest_id IN (
                SELECT pi2.interest_id
                FROM profile_interests pi2
                WHERE pi2.profile_id = (
                    SELECT p2.profile_id FROM profiles p2 WHERE p2.user_id = :currentUserId
                )
            )
            AND u.user_id NOT IN (
                SELECT DISTINCT s.swiped_user_id 
                FROM swipes s 
                WHERE s.swiper_id = :currentUserId
            )
            GROUP BY u.user_id
            ORDER BY common_count DESC
            LIMIT :limit
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentUserId", currentUserId)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Object[]{
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getInt("common_count")
        });
    }

    /**
     * Statystyka: Średnia liczba aktywnych użytkowników w danym mieście.
     */
    public int getAverageActiveUsersByCity(String city) {

        log.debug("Getting statistics for city: {}", city);

        String sql = """
            SELECT COUNT(*) 
            FROM users 
            WHERE city = :city AND is_active = TRUE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("city", city);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Ranking: Użytkownicy z największą liczbą polubień (likes received).
     */
    public List<Object[]> getMostLikedUsers(int limit) {

        log.debug("Getting top {} most liked users", limit);

        String sql = """
            SELECT 
                u.user_id,
                u.username,
                COUNT(s.swipe_id) as likes_count
            FROM users u
            LEFT JOIN swipes s ON s.swiped_user_id = u.user_id AND s.swipe_type = 'LIKE'
            WHERE u.is_active = TRUE
            GROUP BY u.user_id
            ORDER BY likes_count DESC
            LIMIT :limit
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Object[]{
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getInt("likes_count")
        });
    }

    /**
     * Analityka: Rozkład użytkowników po mieście.
     */
    public List<Object[]> getUserDistributionByCity() {

        log.debug("Getting user distribution by city");

        String sql = """
            SELECT 
                city,
                COUNT(*) as user_count,
                SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_count
            FROM users
            GROUP BY city
            ORDER BY user_count DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
            rs.getString("city"),
            rs.getInt("user_count"),
            rs.getInt("active_count")
        });
    }

    /**
     * Update: Deaktywacja użytkownika.
     * Zwraca liczbę zaktualizowanych wierszy.
     */
    public int deactivateUser(Long userId) {

        log.info("Deactivating user: {}", userId);

        String sql = """
            UPDATE users 
            SET is_active = FALSE, updated_at = NOW() 
            WHERE user_id = :userId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        int rowsAffected = jdbcTemplate.update(sql, params);
        log.debug("Deactivated {} user(s)", rowsAffected);

        return rowsAffected;
    }

    /**
     * Update: Aktualizacja miasta dla użytkownika.
     */
    public int updateUserCity(Long userId, String newCity) {

        log.info("Updating city for user {}: {}", userId, newCity);

        String sql = """
            UPDATE users 
            SET city = :city, updated_at = NOW() 
            WHERE user_id = :userId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("city", newCity);

        return jdbcTemplate.update(sql, params);
    }

    /**
     * Delete: Usunięcie użytkownika (kaskadowe przez FK constraints).
     */
    public int deleteUser(Long userId) {

        log.warn("Deleting user: {}", userId);

        String sql = "DELETE FROM users WHERE user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return jdbcTemplate.update(sql, params);
    }
}

