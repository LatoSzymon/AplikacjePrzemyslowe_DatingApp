package AplikacjePrzemyslowe.DatApp.dao.mapper;

import AplikacjePrzemyslowe.DatApp.entity.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileRowMapper Tests")
class ProfileRowMapperTests {

    @Mock
    private ResultSet resultSet;

    private ProfileRowMapper profileRowMapper;

    @BeforeEach
    void setUp() {
        profileRowMapper = new ProfileRowMapper();
    }

    @Test
    @DisplayName("Powinno zmapować ResultSet na Profile")
    void testMapRow_Success() throws SQLException {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        BigDecimal latitude = new BigDecimal("52.2297");
        BigDecimal longitude = new BigDecimal("21.0122");

        when(resultSet.getLong("profile_id")).thenReturn(1L);
        when(resultSet.getString("bio")).thenReturn("Test bio");
        when(resultSet.getInt("height_cm")).thenReturn(180);
        when(resultSet.getString("occupation")).thenReturn("Developer");
        when(resultSet.getString("education")).thenReturn("Bachelor");
        when(resultSet.getBigDecimal("latitude")).thenReturn(latitude);
        when(resultSet.getBigDecimal("longitude")).thenReturn(longitude);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(createdAt);
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(updatedAt);

        // Act
        Profile result = profileRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test bio", result.getBio());
        assertEquals(180, result.getHeightCm());
        assertEquals("Developer", result.getOccupation());
        assertEquals("Bachelor", result.getEducation());
        assertEquals(52.2297, result.getLatitude());
        assertEquals(21.0122, result.getLongitude());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }

    @Test
    @DisplayName("Powinno zmapować Profile z height_cm = 0 jako null")
    void testMapRow_HeightZeroAsNull() throws SQLException {
        // Arrange
        when(resultSet.getLong("profile_id")).thenReturn(2L);
        when(resultSet.getString("bio")).thenReturn("Bio");
        when(resultSet.getInt("height_cm")).thenReturn(0);
        when(resultSet.getString("occupation")).thenReturn("Designer");
        when(resultSet.getString("education")).thenReturn("Master");
        when(resultSet.getBigDecimal("latitude")).thenReturn(new BigDecimal("52.2297"));
        when(resultSet.getBigDecimal("longitude")).thenReturn(new BigDecimal("21.0122"));
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        // Act
        Profile result = profileRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertNull(result.getHeightCm());
    }

    @Test
    @DisplayName("Powinno zmapować Profile z null geografią")
    void testMapRow_NullGeography() throws SQLException {
        // Arrange
        when(resultSet.getLong("profile_id")).thenReturn(3L);
        when(resultSet.getString("bio")).thenReturn("Bio");
        when(resultSet.getInt("height_cm")).thenReturn(175);
        when(resultSet.getString("occupation")).thenReturn("Engineer");
        when(resultSet.getString("education")).thenReturn("PhD");
        when(resultSet.getBigDecimal("latitude")).thenReturn(null);
        when(resultSet.getBigDecimal("longitude")).thenReturn(null);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        // Act
        Profile result = profileRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
    }

    @Test
    @DisplayName("Powinno zmapować Profile z null polami")
    void testMapRow_NullFields() throws SQLException {
        // Arrange
        when(resultSet.getLong("profile_id")).thenReturn(4L);
        when(resultSet.getString("bio")).thenReturn(null);
        when(resultSet.getInt("height_cm")).thenReturn(0);
        when(resultSet.getString("occupation")).thenReturn(null);
        when(resultSet.getString("education")).thenReturn(null);
        when(resultSet.getBigDecimal("latitude")).thenReturn(null);
        when(resultSet.getBigDecimal("longitude")).thenReturn(null);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(null);
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(null);

        // Act
        Profile result = profileRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertNull(result.getBio());
        assertNull(result.getHeightCm());
        assertNull(result.getOccupation());
        assertNull(result.getEducation());
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Powinno zmapować Profile z dokładnymi współrzędnymi geograficznymi")
    void testMapRow_PreciseCoordinates() throws SQLException {
        // Arrange
        BigDecimal latitude = new BigDecimal("51.5074");
        BigDecimal longitude = new BigDecimal("-0.1278");

        when(resultSet.getLong("profile_id")).thenReturn(5L);
        when(resultSet.getString("bio")).thenReturn("London");
        when(resultSet.getInt("height_cm")).thenReturn(170);
        when(resultSet.getString("occupation")).thenReturn("Artist");
        when(resultSet.getString("education")).thenReturn("High School");
        when(resultSet.getBigDecimal("latitude")).thenReturn(latitude);
        when(resultSet.getBigDecimal("longitude")).thenReturn(longitude);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        // Act
        Profile result = profileRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(51.5074, result.getLatitude());
        assertEquals(-0.1278, result.getLongitude());
    }
}

