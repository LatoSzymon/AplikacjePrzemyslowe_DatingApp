package AplikacjePrzemyslowe.DatApp.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageJdbcDao Tests")
public class MessageJdbcDaoTests {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private MessageJdbcDao messageJdbcDao;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    // ========== countUnreadMessages Tests ==========

    @Test
    @DisplayName("Powinno policzyć nieprzeczytane wiadomości w matchu")
    void testCountUnreadMessages_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(5);

        // Act
        int result = messageJdbcDao.countUnreadMessages(1L, 1L);

        // Assert
        assertEquals(5, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy brak nieprzeczytanych wiadomości")
    void testCountUnreadMessages_Zero() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        // Act
        int result = messageJdbcDao.countUnreadMessages(1L, 1L);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy wynik jest null")
    void testCountUnreadMessages_Null() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(null);

        // Act
        int result = messageJdbcDao.countUnreadMessages(1L, 1L);

        // Assert
        assertEquals(0, result);
    }

    // ========== countTotalUnreadMessages Tests ==========

    @Test
    @DisplayName("Powinno policzyć całkowite nieprzeczytane wiadomości dla użytkownika")
    void testCountTotalUnreadMessages_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(10);

        // Act
        int result = messageJdbcDao.countTotalUnreadMessages(1L);

        // Assert
        assertEquals(10, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy brak nieprzeczytanych wiadomości")
    void testCountTotalUnreadMessages_Zero() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        // Act
        int result = messageJdbcDao.countTotalUnreadMessages(1L);

        // Assert
        assertEquals(0, result);
    }

    // ========== markAllMessagesAsRead Tests ==========

    @Test
    @DisplayName("Powinno oznaczyć wszystkie wiadomości jako przeczytane")
    void testMarkAllMessagesAsRead_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(5);

        // Act
        int result = messageJdbcDao.markAllMessagesAsRead(1L, 1L);

        // Assert
        assertEquals(5, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy brak wiadomości do oznaczenia")
    void testMarkAllMessagesAsRead_Zero() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = messageJdbcDao.markAllMessagesAsRead(1L, 1L);

        // Assert
        assertEquals(0, result);
    }

    // ========== deleteConversation Tests ==========

    @Test
    @DisplayName("Powinno usunąć całą konwersację")
    void testDeleteConversation_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(10);

        // Act
        int result = messageJdbcDao.deleteConversation(1L);

        // Assert
        assertEquals(10, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy konwersacja nie istnieje")
    void testDeleteConversation_NotFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = messageJdbcDao.deleteConversation(999L);

        // Assert
        assertEquals(0, result);
    }

    // ========== getMessagingStatistics Tests ==========

    @Test
    @DisplayName("Powinno zwrócić statystyki wiadomości")
    void testGetMessagingStatistics_Success() {
        // Arrange
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{5, 50, 10.0, 15});

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = messageJdbcDao.getMessagingStatistics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0)[0]); // conversation_count
        assertEquals(50, result.get(0)[1]); // total_messages
        assertEquals(10.0, result.get(0)[2]); // avg_msg_per_conversation
        assertEquals(15, result.get(0)[3]); // max_msg_per_conversation
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak statystyk")
    void testGetMessagingStatistics_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<Object[]> result = messageJdbcDao.getMessagingStatistics(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getTopMessageSenders Tests ==========

    @Test
    @DisplayName("Powinno zwrócić top nadawców wiadomości")
    void testGetTopMessageSenders_Success() {
        // Arrange
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, "user1", 100});
        mockResult.add(new Object[]{2L, "user2", 50});

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = messageJdbcDao.getTopMessageSenders(10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0)[0]); // user_id
        assertEquals("user1", result.get(0)[1]); // username
        assertEquals(100, result.get(0)[2]); // message_count
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak nadawców")
    void testGetTopMessageSenders_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<Object[]> result = messageJdbcDao.getTopMessageSenders(10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getAverageResponseTime Tests ==========

    @Test
    @DisplayName("Powinno zwrócić średni czas odpowiedzi")
    void testGetAverageResponseTime_Success() {
        // Arrange
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{15.5, 1, 120});

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = messageJdbcDao.getAverageResponseTime();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(15.5, result.get(0)[0]); // avg_response_minutes
        assertEquals(1, result.get(0)[1]); // min_response_minutes
        assertEquals(120, result.get(0)[2]); // max_response_minutes
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak danych")
    void testGetAverageResponseTime_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<Object[]> result = messageJdbcDao.getAverageResponseTime();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== searchMessagesInConversation Tests ==========

    @Test
    @DisplayName("Powinno znaleźć wiadomości zawierające szukany tekst")
    void testSearchMessagesInConversation_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, 1L, "user1", "hello world", now});

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = messageJdbcDao.searchMessagesInConversation(1L, "hello");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0)[0]); // message_id
        assertEquals(1L, result.get(0)[1]); // sender_id
        assertEquals("user1", result.get(0)[2]); // username
        assertEquals("hello world", result.get(0)[3]); // content
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy nie znaleziono wiadomości")
    void testSearchMessagesInConversation_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<Object[]> result = messageJdbcDao.searchMessagesInConversation(1L, "notfound");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getLastMessage Tests ==========

    @Test
    @DisplayName("Powinno zwrócić ostatnią wiadomość w konwersacji")
    void testGetLastMessage_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Object[] mockMessage = new Object[]{1L, 1L, "user1", "last message", now};
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(mockMessage);

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        Object[] result = messageJdbcDao.getLastMessage(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result[0]); // message_id
        assertEquals(1L, result[1]); // sender_id
        assertEquals("user1", result[2]); // username
        assertEquals("last message", result[3]); // content
    }

    @Test
    @DisplayName("Powinno zwrócić null gdy brak wiadomości")
    void testGetLastMessage_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(new ArrayList<>());

        // Act
        Object[] result = messageJdbcDao.getLastMessage(1L);

        // Assert
        assertNull(result);
    }

    // ========== countMessagesSentByUser Tests ==========

    @Test
    @DisplayName("Powinno policzyć wiadomości wysłane przez użytkownika")
    void testCountMessagesSentByUser_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(25);

        // Act
        int result = messageJdbcDao.countMessagesSentByUser(1L);

        // Assert
        assertEquals(25, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy użytkownik nie wysłał wiadomości")
    void testCountMessagesSentByUser_Zero() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        // Act
        int result = messageJdbcDao.countMessagesSentByUser(1L);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy wynik jest null")
    void testCountMessagesSentByUser_Null() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(null);

        // Act
        int result = messageJdbcDao.countMessagesSentByUser(1L);

        // Assert
        assertEquals(0, result);
    }

    // ========== deleteOldMessages Tests ==========

    @Test
    @DisplayName("Powinno usunąć stare wiadomości")
    void testDeleteOldMessages_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(100);

        // Act
        int result = messageJdbcDao.deleteOldMessages(30);

        // Assert
        assertEquals(100, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy nie ma starych wiadomości")
    void testDeleteOldMessages_Zero() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = messageJdbcDao.deleteOldMessages(30);

        // Assert
        assertEquals(0, result);
    }
}

