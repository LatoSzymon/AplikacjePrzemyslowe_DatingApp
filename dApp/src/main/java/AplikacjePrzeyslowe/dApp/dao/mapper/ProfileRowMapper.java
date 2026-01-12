package AplikacjePrzeyslowe.dApp.dao.mapper;

import AplikacjePrzeyslowe.dApp.entity.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * RowMapper dla encji Profile.
 */
@Component
public class ProfileRowMapper implements RowMapper<Profile> {

    @Override
    public Profile mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Profile.builder()
                .id(rs.getLong("profile_id"))
                .bio(rs.getString("bio"))
                .heightCm(rs.getInt("height_cm") != 0 ? rs.getInt("height_cm") : null)
                .occupation(rs.getString("occupation"))
                .education(rs.getString("education"))
                .latitude(rs.getBigDecimal("latitude") != null ? rs.getBigDecimal("latitude").doubleValue() : null)
                .longitude(rs.getBigDecimal("longitude") != null ? rs.getBigDecimal("longitude").doubleValue() : null)
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                .build();
    }
}

