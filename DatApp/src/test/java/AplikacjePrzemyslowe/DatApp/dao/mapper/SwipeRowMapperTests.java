package AplikacjePrzemyslowe.DatApp.dao.mapper;

import AplikacjePrzemyslowe.DatApp.entity.Swipe;
import AplikacjePrzemyslowe.DatApp.entity.SwipeType;
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
@DisplayName("SwipeRowMapper Tests")
class SwipeRowMapperTests {

    @Mock
    private ResultSet resultSet;

    private SwipeRowMapper swipeRowMapper;

    @BeforeEach
    void setUp() {
        swipeRowMapper = new SwipeRowMapper();
    }

    @Test
    @DisplayName("Powinno zmapować ResultSet na Swipe typu LIKE")
    void testMapRow_LikeSwipe() throws SQLException {
        // Arrange
        LocalDateTime swipedAt = LocalDateTime.of(2024, 1, 15, 10, 30);

        when(resultSet.getLong("swipe_id")).thenReturn(1L);
        when(resultSet.getString("swipe_type")).thenReturn("LIKE");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(swipedAt);

        // Act
        Swipe result = swipeRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(SwipeType.LIKE, result.getSwipeType());
        assertEquals(swipedAt, result.getSwipedAt());
    }

    @Test
    @DisplayName("Powinno zmapować ResultSet na Swipe typu PASS")
    void testMapRow_PassSwipe() throws SQLException {
        // Arrange
        LocalDateTime swipedAt = LocalDateTime.of(2024, 1, 14, 15, 45);

        when(resultSet.getLong("swipe_id")).thenReturn(2L);
        when(resultSet.getString("swipe_type")).thenReturn("PASS");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(swipedAt);

        // Act
        Swipe result = swipeRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(SwipeType.PASS, result.getSwipeType());
        assertEquals(swipedAt, result.getSwipedAt());
    }

    @Test
    @DisplayName("Powinno zmapować Swipe z różnymi ID")
    void testMapRow_DifferentIds() throws SQLException {
        // Arrange
        LocalDateTime swipedAt = LocalDateTime.now();

        when(resultSet.getLong("swipe_id")).thenReturn(999L);
        when(resultSet.getString("swipe_type")).thenReturn("LIKE");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(swipedAt);

        // Act
        Swipe result = swipeRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
    }

    @Test
    @DisplayName("Powinno zmapować Swipe z null swiped_at")
    void testMapRow_NullSwipedAt() throws SQLException {
        // Arrange
        when(resultSet.getLong("swipe_id")).thenReturn(3L);
        when(resultSet.getString("swipe_type")).thenReturn("LIKE");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(null);

        // Act
        Swipe result = swipeRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(SwipeType.LIKE, result.getSwipeType());
        assertNull(result.getSwipedAt());
    }

    @Test
    @DisplayName("Powinno zmapować wiele Swipe'ów w sekwencji")
    void testMapRow_MultipleSequence() throws SQLException {
        // Arrange
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 15, 10, 15);
        LocalDateTime time3 = LocalDateTime.of(2024, 1, 15, 10, 30);

        // First swipe
        when(resultSet.getLong("swipe_id")).thenReturn(1L);
        when(resultSet.getString("swipe_type")).thenReturn("LIKE");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(time1);

        Swipe swipe1 = swipeRowMapper.mapRow(resultSet, 0);
        assertEquals(1L, swipe1.getId());
        assertEquals(SwipeType.LIKE, swipe1.getSwipeType());

        // Second swipe
        when(resultSet.getLong("swipe_id")).thenReturn(2L);
        when(resultSet.getString("swipe_type")).thenReturn("PASS");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(time2);

        Swipe swipe2 = swipeRowMapper.mapRow(resultSet, 1);
        assertEquals(2L, swipe2.getId());
        assertEquals(SwipeType.PASS, swipe2.getSwipeType());

        // Third swipe
        when(resultSet.getLong("swipe_id")).thenReturn(3L);
        when(resultSet.getString("swipe_type")).thenReturn("LIKE");
        when(resultSet.getObject("swiped_at", LocalDateTime.class)).thenReturn(time3);

        Swipe swipe3 = swipeRowMapper.mapRow(resultSet, 2);
        assertEquals(3L, swipe3.getId());
        assertEquals(SwipeType.LIKE, swipe3.getSwipeType());

        // Assert all mapped correctly with different timestamps
        assertTrue(swipe1.getSwipedAt().isBefore(swipe2.getSwipedAt()));
        assertTrue(swipe2.getSwipedAt().isBefore(swipe3.getSwipedAt()));
    }
}

