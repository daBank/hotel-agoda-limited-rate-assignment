package com.hotelbooking.svchotel.repository;

import com.hotelbooking.svchotel.model.mapper.City;
import com.hotelbooking.svchotel.model.mapper.CityJoinRoomMapper;
import com.hotelbooking.svchotel.model.mapper.HotelDataByCity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;
import java.util.StringJoiner;

@Repository
@Transactional
@Slf4j
public class CityRepository {
    public static String TABLE_NAME = "cities";

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<HotelDataByCity> retrieveHotels(String city) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("SELECT");
        sql.add("ct.name as city, ro.name as room_name, ro.price");
        sql.add("FROM");
        sql.add(TABLE_NAME);
        sql.add("ct");
        sql.add("INNER JOIN");
        sql.add("rooms ro on ct.id=ro.city_id");
        if (StringUtils.isNotEmpty(city)) {
            sql.add("WHERE ct.name = :city");
        }
        sql.add(";");

        Map<String, Object> namedParameters = new HashMap<>();
        namedParameters.put("city", city);
        return namedParameterJdbcTemplate.query(sql.toString(), namedParameters, new CityJoinRoomMapper());
    }

    public Integer getCountCities() {
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

    public int[] initCities(List<City> cities) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("INSERT INTO");
        sql.add(TABLE_NAME);
        sql.add("(id, name) VALUES (?, ?)");

        return jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, cities.get(i).getId());
                ps.setString(2, cities.get(i).getName());
            }

            @Override
            public int getBatchSize() {
                return cities.size();
            }
        });
    }
}
