package AplikacjePrzemyslowe.DatApp.dao.mapper;

import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRowMapper Tests")
class UserRowMapperTests {

    @Mock
    private ResultSet resultSet;

    private UserRowMapper userRowMapper;

    @BeforeEach
    void setUp() {
        userRowMapper = new UserRowMapper();
    }

    @Test
    @DisplayName("Powinno zmapować ResultSet na User")
    void testMapRow_Success() throws SQLException {
        // Arrange
        LocalDate birthDate = LocalDate.of(1995, 5, 15);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        when(resultSet.getLong("user_id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("testuser");
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("password")).thenReturn("hashed_password");
        when(resultSet.getString("gender")).thenReturn("MALE");
        when(resultSet.getObject("birth_date", LocalDate.class)).thenReturn(birthDate);
        when(resultSet.getString("city")).thenReturn("Warsaw");
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(createdAt);
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(updatedAt);

        // Act
        User result = userRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("hashed_password", result.getPassword());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(birthDate, result.getBirthDate());
        assertEquals("Warsaw", result.getCity());
        assertTrue(result.getIsActive());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }

    @Test
    @DisplayName("Powinno zmapować User z gender FEMALE")
    void testMapRow_Female() throws SQLException {
        // Arrange
        when(resultSet.getLong("user_id")).thenReturn(2L);
        when(resultSet.getString("username")).thenReturn("femaleuser");
        when(resultSet.getString("email")).thenReturn("female@example.com");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getString("gender")).thenReturn("FEMALE");
        when(resultSet.getObject("birth_date", LocalDate.class)).thenReturn(LocalDate.of(1996, 6, 20));
        when(resultSet.getString("city")).thenReturn("Krakow");
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        // Act
        User result = userRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(Gender.FEMALE, result.getGender());
        assertEquals(2L, result.getId());
    }

    @Test
    @DisplayName("Powinno zmapować nieaktywnego użytkownika")
    void testMapRow_Inactive() throws SQLException {
        // Arrange
        when(resultSet.getLong("user_id")).thenReturn(3L);
        when(resultSet.getString("username")).thenReturn("inactiveuser");
        when(resultSet.getString("email")).thenReturn("inactive@example.com");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getString("gender")).thenReturn("MALE");
        when(resultSet.getObject("birth_date", LocalDate.class)).thenReturn(LocalDate.of(1990, 1, 1));
        when(resultSet.getString("city")).thenReturn("Warsaw");
        when(resultSet.getBoolean("is_active")).thenReturn(false);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        // Act
        User result = userRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsActive());
    }

    @Test
    @DisplayName("Powinno zmapować User z null wartościami")
    void testMapRow_WithNullValues() throws SQLException {
        // Arrange
        when(resultSet.getLong("user_id")).thenReturn(4L);
        when(resultSet.getString("username")).thenReturn("nulluser");
        when(resultSet.getString("email")).thenReturn("null@example.com");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getString("gender")).thenReturn("MALE");
        when(resultSet.getObject("birth_date", LocalDate.class)).thenReturn(null);
        when(resultSet.getString("city")).thenReturn(null);
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(null);
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(null);

        // Act
        User result = userRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertNull(result.getBirthDate());
        assertNull(result.getCity());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
}

