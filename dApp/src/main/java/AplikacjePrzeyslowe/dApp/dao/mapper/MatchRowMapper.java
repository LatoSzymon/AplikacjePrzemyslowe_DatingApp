package AplikacjePrzeyslowe.dApp.dao.mapper;

import AplikacjePrzeyslowe.dApp.entity.Match;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * RowMapper dla encji Match.
 */
@Component
public class MatchRowMapper implements RowMapper<Match> {

    @Override
    public Match mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Match.builder()
                .id(rs.getLong("match_id"))
                .isActive(rs.getBoolean("is_active"))
                .matchedAt(rs.getObject("matched_at", LocalDateTime.class))
                .unmatchedAt(rs.getObject("unmatched_at", LocalDateTime.class))
                .build();
    }
}

