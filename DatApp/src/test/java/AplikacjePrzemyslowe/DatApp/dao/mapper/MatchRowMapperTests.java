package AplikacjePrzemyslowe.DatApp.dao.mapper;

import AplikacjePrzemyslowe.DatApp.entity.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchRowMapper Tests")
class MatchRowMapperTests {

    @Mock
    private ResultSet resultSet;

    private MatchRowMapper matchRowMapper;

    @BeforeEach
    void setUp() {
        matchRowMapper = new MatchRowMapper();
    }

    @Test
    @DisplayName("Powinno zmapować ResultSet na aktywny Match")
    void testMapRow_ActiveMatch() throws SQLException {
        // Arrange
        LocalDateTime matchedAt = LocalDateTime.of(2024, 1, 15, 10, 30);

        when(resultSet.getLong("match_id")).thenReturn(1L);
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getObject("matched_at", LocalDateTime.class)).thenReturn(matchedAt);
        when(resultSet.getObject("unmatched_at", LocalDateTime.class)).thenReturn(null);

        // Act
        Match result = matchRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getIsActive());
        assertEquals(matchedAt, result.getMatchedAt());
        assertNull(result.getUnmatchedAt());
    }

    @Test
    @DisplayName("Powinno zmapować ResultSet na nieaktywny Match")
    void testMapRow_InactiveMatch() throws SQLException {
        // Arrange
        LocalDateTime matchedAt = LocalDateTime.of(2024, 1, 10, 10, 0);
        LocalDateTime unmatchedAt = LocalDateTime.of(2024, 1, 15, 15, 30);

        when(resultSet.getLong("match_id")).thenReturn(2L);
        when(resultSet.getBoolean("is_active")).thenReturn(false);
        when(resultSet.getObject("matched_at", LocalDateTime.class)).thenReturn(matchedAt);
        when(resultSet.getObject("unmatched_at", LocalDateTime.class)).thenReturn(unmatchedAt);

        // Act
        Match result = matchRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertFalse(result.getIsActive());
        assertEquals(matchedAt, result.getMatchedAt());
        assertEquals(unmatchedAt, result.getUnmatchedAt());
    }

    @Test
    @DisplayName("Powinno zmapować Match z null unmatched_at")
    void testMapRow_NullUnmatchedAt() throws SQLException {
        // Arrange
        LocalDateTime matchedAt = LocalDateTime.now();

        when(resultSet.getLong("match_id")).thenReturn(3L);
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getObject("matched_at", LocalDateTime.class)).thenReturn(matchedAt);
        when(resultSet.getObject("unmatched_at", LocalDateTime.class)).thenReturn(null);

        // Act
        Match result = matchRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsActive());
        assertNull(result.getUnmatchedAt());
    }

    @Test
    @DisplayName("Powinno zmapować Match z różnymi ID")
    void testMapRow_DifferentIds() throws SQLException {
        // Arrange
        LocalDateTime matchedAt = LocalDateTime.now();

        when(resultSet.getLong("match_id")).thenReturn(999L);
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getObject("matched_at", LocalDateTime.class)).thenReturn(matchedAt);
        when(resultSet.getObject("unmatched_at", LocalDateTime.class)).thenReturn(null);

        // Act
        Match result = matchRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
    }
}

