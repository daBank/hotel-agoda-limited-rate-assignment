package com.hotelbooking.svchotel.repository;

import com.hotelbooking.svchotel.model.mapper.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

@Repository
@Transactional
@Slf4j
public class RoomRepository {
    public static String TABLE_NAME = "rooms";

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer getCountRooms() {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("SELECT COUNT(*) FROM");
        sql.add(TABLE_NAME);
        sql.add(";");

        try {
            return namedParameterJdbcTemplate.queryForObject(sql.toString(), new HashMap<>(), Integer.class);
        } catch (DataAccessException dex) {
            log.error("Error in getCountCities", dex);
            return 0;
        }
    }

    public int[] initRooms(List<Room> rooms) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("INSERT INTO");
        sql.add(TABLE_NAME);
        sql.add("(hotel_id, name, city_id, price) VALUES (?, ?, ?, ?)");

        return jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, rooms.get(i).getHotelId());
                ps.setString(2, rooms.get(i).getName());
                ps.setString(3, rooms.get(i).getCityId());
                ps.setBigDecimal(4, rooms.get(i).getPrice());
            }

            @Override
            public int getBatchSize() {
                return rooms.size();
            }
        });
    }
}
