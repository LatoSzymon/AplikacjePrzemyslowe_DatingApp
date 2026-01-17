package AplikacjePrzemyslowe.DatApp.dao;

import AplikacjePrzemyslowe.DatApp.dao.mapper.UserRowMapper;
import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.User;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserJdbcDao Tests")
class UserJdbcDaoTests {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private UserRowMapper userRowMapper;

    @InjectMocks
    private UserJdbcDao userJdbcDao;

    private User testUser1;
    private User testUser2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .isActive(true)
                .build();

        testUser2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1996, 6, 20))
                .city("Warsaw")
                .isActive(true)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    // ========== findCandidatesByPreference Tests ==========

    @Test
    @DisplayName("Powinno znaleźć kandydatów według preferencji")
    void testFindCandidatesByPreference_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(2L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper)))
                .thenReturn(Arrays.asList(testUser1, testUser2));

        // Act
        Page<User> result = userJdbcDao.findCandidatesByPreference(1L, Gender.FEMALE, 25, 35, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak kandydatów")
    void testFindCandidatesByPreference_Empty() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper)))
                .thenReturn(Arrays.asList());

        // Act
        Page<User> result = userJdbcDao.findCandidatesByPreference(1L, Gender.FEMALE, 25, 35, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Powinno obsłużyć paginację")
    void testFindCandidatesByPreference_WithPagination() {
        // Arrange
        Pageable page2 = PageRequest.of(1, 5);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(10L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper)))
                .thenReturn(Arrays.asList(testUser1));

        // Act
        Page<User> result = userJdbcDao.findCandidatesByPreference(1L, Gender.FEMALE, 25, 35, page2);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber()); // Page number
    }

    // ========== findCandidatesByCommonInterests Tests ==========

    @Test
    @DisplayName("Powinno znaleźć kandydatów ze wspólnymi zainteresowaniami")
    void testFindCandidatesByCommonInterests_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(1L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper)))
                .thenReturn(Arrays.asList(testUser2));

        // Act
        Page<User> result = userJdbcDao.findCandidatesByCommonInterests(1L, Gender.FEMALE, 25, 35, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak wspólnych zainteresowań")
    void testFindCandidatesByCommonInterests_Empty() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(userRowMapper)))
                .thenReturn(Arrays.asList());

        // Act
        Page<User> result = userJdbcDao.findCandidatesByCommonInterests(1L, Gender.FEMALE, 25, 35, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ========== findUsersWithCommonInterestCounts Tests ==========

    @Test
    @DisplayName("Powinno znaleźć użytkowników z liczbą wspólnych zainteresowań")
    void testFindUsersWithCommonInterestCounts_Success() {
        // Arrange
        List<Object[]> mockResult = Arrays.asList(
                new Object[]{1L, "user1", 5},
                new Object[]{2L, "user2", 3}
        );
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = userJdbcDao.findUsersWithCommonInterestCounts(1L, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0)[0]);
        assertEquals("user1", result.get(0)[1]);
        assertEquals(5, result.get(0)[2]);
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak użytkowników ze wspólnymi zainteresowaniami")
    void testFindUsersWithCommonInterestCounts_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<Object[]> result = userJdbcDao.findUsersWithCommonInterestCounts(1L, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getAverageActiveUsersByCity Tests ==========

    @Test
    @DisplayName("Powinno zwrócić liczbę aktywnych użytkowników w mieście")
    void testGetAverageActiveUsersByCity_Success() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(10);

        // Act
        int result = userJdbcDao.getAverageActiveUsersByCity("Warsaw");

        // Assert
        assertEquals(10, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy brak użytkowników w mieście")
    void testGetAverageActiveUsersByCity_Zero() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        // Act
        int result = userJdbcDao.getAverageActiveUsersByCity("Unknown");

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy wynik jest null")
    void testGetAverageActiveUsersByCity_Null() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(null);

        // Act
        int result = userJdbcDao.getAverageActiveUsersByCity("Warsaw");

        // Assert
        assertEquals(0, result);
    }

    // ========== getMostLikedUsers Tests ==========

    @Test
    @DisplayName("Powinno zwrócić najbardziej lubianych użytkowników")
    void testGetMostLikedUsers_Success() {
        // Arrange
        List<Object[]> mockResult = Arrays.asList(
                new Object[]{1L, "user1", 100},
                new Object[]{2L, "user2", 50}
        );
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = userJdbcDao.getMostLikedUsers(10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0)[2]); // likes count
        verify(jdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak polubień")
    void testGetMostLikedUsers_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<Object[]> result = userJdbcDao.getMostLikedUsers(10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getUserDistributionByCity Tests ==========

    @Test
    @DisplayName("Powinno zwrócić rozkład użytkowników po mieście")
    void testGetUserDistributionByCity_Success() {
        // Arrange
        List<Object[]> mockResult = Arrays.asList(
                new Object[]{"Warsaw", 100, 80},
                new Object[]{"Krakow", 50, 40}
        );
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(mockResult);

        // Act
        List<Object[]> result = userJdbcDao.getUserDistributionByCity();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Warsaw", result.get(0)[0]);
        assertEquals(100, result.get(0)[1]);
        assertEquals(80, result.get(0)[2]);
        verify(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class));
    }

    @Test
    @DisplayName("Powinno zwrócić pustą listę gdy brak użytkowników")
    void testGetUserDistributionByCity_Empty() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<Object[]> result = userJdbcDao.getUserDistributionByCity();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== deactivateUser Tests ==========

    @Test
    @DisplayName("Powinno dezaktywować użytkownika")
    void testDeactivateUser_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        // Act
        int result = userJdbcDao.deactivateUser(1L);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy użytkownik nie istnieje")
    void testDeactivateUser_NotFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = userJdbcDao.deactivateUser(999L);

        // Assert
        assertEquals(0, result);
    }

    // ========== updateUserCity Tests ==========

    @Test
    @DisplayName("Powinno zaktualizować miasto użytkownika")
    void testUpdateUserCity_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        // Act
        int result = userJdbcDao.updateUserCity(1L, "Krakow");

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy użytkownik nie istnieje")
    void testUpdateUserCity_NotFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = userJdbcDao.updateUserCity(999L, "Krakow");

        // Assert
        assertEquals(0, result);
    }

    // ========== deleteUser Tests ==========

    @Test
    @DisplayName("Powinno usunąć użytkownika")
    void testDeleteUser_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        // Act
        int result = userJdbcDao.deleteUser(1L);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Powinno zwrócić 0 gdy użytkownik nie istnieje")
    void testDeleteUser_NotFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(0);

        // Act
        int result = userJdbcDao.deleteUser(999L);

        // Assert
        assertEquals(0, result);
    }
}

