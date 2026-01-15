package AplikacjePrzemyslowe.DatApp.dao.mapper;

import AplikacjePrzemyslowe.DatApp.entity.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RowMapper dla encji User.
 * Mapuje wyniki SQL query na obiekty User.
 */
@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .gender(Gender.valueOf(rs.getString("gender")))
                .birthDate(rs.getObject("birth_date", LocalDate.class))
                .city(rs.getString("city"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                .build();
    }
}

