package AplikacjePrzemyslowe.DatApp.dao;

import AplikacjePrzemyslowe.DatApp.dao.mapper.MatchRowMapper;
import AplikacjePrzemyslowe.DatApp.entity.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchJdbcDao Tests")
public class MatchJdbcDaoTests {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private MatchRowMapper matchRowMapper;

    @InjectMocks
    private MatchJdbcDao matchJdbcDao;

    private Match testMatch1;
    private Match testMatch2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testMatch1 = Match.builder()
                .id(1L)
                .isActive(true)
                .matchedAt(LocalDateTime.now())
                .build();

        testMatch2 = Match.builder()
                .id(2L)
                .isActive(true)
                .matchedAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 10);
    }

    // ========== findMostActiveMatches Tests ==========

    @Test
    @DisplayName("Powinno znaleźć najpopularniejsze dopasowania")
    void testFindMostActiveMatches_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(2L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(Arrays.asList(testMatch1, testMatch2));

        // Act
        Page<Match> result = matchJdbcDao.findMostActiveMatches(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak matchów")
    void testFindMostActiveMatches_Empty() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(List.of());

        // Act
        Page<Match> result = matchJdbcDao.findMostActiveMatches(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Powinno obsłużyć paginację")
    void testFindMostActiveMatches_WithPagination() {
        // Arrange
        Pageable page2 = PageRequest.of(1, 5);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(10L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(Arrays.asList(testMatch1));

        // Act
        Page<Match> result = matchJdbcDao.findMostActiveMatches(1L, page2);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
    }

    // ========== findRecentMatches Tests ==========

    @Test
    @DisplayName("Powinno znaleźć niedawne dopasowania")
    void testFindRecentMatches_Success() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(Arrays.asList(testMatch1, testMatch2));

        // Act
        List<Match> result = matchJdbcDao.findRecentMatches(1L, 7);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak niedawnych matchów")
    void testFindRecentMatches_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(List.of());

        // Act
        List<Match> result = matchJdbcDao.findRecentMatches(1L, 7);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findMatchesWithoutMessages Tests ==========

    @Test
    @DisplayName("Powinno znaleźć dopasowania bez wiadomości")
    void testFindMatchesWithoutMessages_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(1L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(Arrays.asList(testMatch1));

        // Act
        Page<Match> result = matchJdbcDao.findMatchesWithoutMessages(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy wszystkie matchesy mają wiadomości")
    void testFindMatchesWithoutMessages_Empty() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(matchRowMapper)))
                .thenReturn(List.of());

        // Act
        Page<Match> result = matchJdbcDao.findMatchesWithoutMessages(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ========== getMatchStatistics Tests ==========

    @Test
    @DisplayName("Powinno zwrócić statystyki matchów")
    void testGetMatchStatistics_Success() {
        // Arrange
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, "user1", 5, 2});
        mockResult.add(new Object[]{2L, "user2", 3, 1});

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = matchJdbcDao.getMatchStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0)[0]); // user_id
        assertEquals("user1", result.get(0)[1]); // username
        assertEquals(5, result.get(0)[2]); // active_matches
        assertEquals(2, result.get(0)[3]); // unmatched_count
        verify(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak użytkowników")
    void testGetMatchStatistics_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of());

        // Act
        List<Object[]> result = matchJdbcDao.getMatchStatistics();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getAverageTimeToFirstMessage Tests ==========

    @Test
    @DisplayName("Powinno zwrócić średni czas do pierwszej wiadomości")
    void testGetAverageTimeToFirstMessage_Success() {
        // Arrange
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{2.5, 1, 10});

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = matchJdbcDao.getAverageTimeToFirstMessage();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2.5, result.get(0)[0]); // avg_hours
        assertEquals(1, result.get(0)[1]); // min_hours
        assertEquals(10, result.get(0)[2]); // max_hours
        verify(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak wiadomości")
    void testGetAverageTimeToFirstMessage_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of());

        // Act
        List<Object[]> result = matchJdbcDao.getAverageTimeToFirstMessage();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== unmatchUsers Tests ==========

    @Test
    @DisplayName("Powinno rozwiązać match")
    void testUnmatchUsers_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        // Act
        int result = matchJdbcDao.unmatchUsers(1L);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy match nie istnieje")
    void testUnmatchUsers_NotFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = matchJdbcDao.unmatchUsers(999L);

        // Assert
        assertEquals(0, result);
    }

    // ========== createMatch Tests ==========

    @Test
    @DisplayName("Powinno stworzyć nowy match")
    void testCreateMatch_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        // Act
        int result = matchJdbcDao.createMatch(1L, 2L);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy tworzenie matcha nie powiodło się")
    void testCreateMatch_Failure() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = matchJdbcDao.createMatch(1L, 2L);

        // Assert
        assertEquals(0, result);
    }

    // ========== deleteMatch Tests ==========

    @Test
    @DisplayName("Powinno usunąć match")
    void testDeleteMatch_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        // Act
        int result = matchJdbcDao.deleteMatch(1L);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy match do usunięcia nie istnieje")
    void testDeleteMatch_NotFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = matchJdbcDao.deleteMatch(999L);

        // Assert
        assertEquals(0, result);
    }

    // ========== existsMatchBetween Tests ==========

    @Test
    @DisplayName("Powinno sprawdzić czy match istnieje między użytkownikami")
    void testExistsMatchBetween_True() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(1);

        // Act
        boolean result = matchJdbcDao.existsMatchBetween(1L, 2L);

        // Assert
        assertTrue(result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    @DisplayName("Powinno zwrócić false gdy match nie istnieje")
    void testExistsMatchBetween_False() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        // Act
        boolean result = matchJdbcDao.existsMatchBetween(1L, 2L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Powinno zwrócić false gdy wynik jest null")
    void testExistsMatchBetween_Null() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(null);

        // Act
        boolean result = matchJdbcDao.existsMatchBetween(1L, 2L);

        // Assert
        assertFalse(result);
    }

    // ========== getPartnerUserId Tests ==========

    @Test
    @DisplayName("Powinno zwrócić ID partnera gdy bieżący użytkownik to user1")
    void testGetPartnerUserId_AsUser1() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(2L);

        // Act
        Long result = matchJdbcDao.getPartnerUserId(1L, 1L);

        // Assert
        assertEquals(2L, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
    }

    @Test
    @DisplayName("Powinno zwrócić ID partnera gdy bieżący użytkownik to user2")
    void testGetPartnerUserId_AsUser2() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(1L);

        // Act
        Long result = matchJdbcDao.getPartnerUserId(1L, 2L);

        // Assert
        assertEquals(1L, result);
    }

    // ========== countActiveMatches Tests ==========

    @Test
    @DisplayName("Powinno policzyć aktywne matche dla użytkownika")
    void testCountActiveMatches_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(5);

        // Act
        int result = matchJdbcDao.countActiveMatches(1L);

        // Assert
        assertEquals(5, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy brak matchów")
    void testCountActiveMatches_Zero() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        // Act
        int result = matchJdbcDao.countActiveMatches(1L);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy wynik jest null")
    void testCountActiveMatches_Null() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(null);

        // Act
        int result = matchJdbcDao.countActiveMatches(1L);

        // Assert
        assertEquals(0, result);
    }
}


