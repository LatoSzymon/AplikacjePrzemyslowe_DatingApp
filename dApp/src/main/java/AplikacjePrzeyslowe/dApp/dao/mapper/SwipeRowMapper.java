package AplikacjePrzeyslowe.dApp.dao.mapper;

import AplikacjePrzeyslowe.dApp.entity.Swipe;
import AplikacjePrzeyslowe.dApp.entity.SwipeType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * RowMapper dla encji Swipe.
 */
@Component
public class SwipeRowMapper implements RowMapper<Swipe> {

    @Override
    public Swipe mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Swipe.builder()
                .id(rs.getLong("swipe_id"))
                .swipeType(SwipeType.valueOf(rs.getString("swipe_type")))
                .swipedAt(rs.getObject("swiped_at", LocalDateTime.class))
                .build();
    }
}

