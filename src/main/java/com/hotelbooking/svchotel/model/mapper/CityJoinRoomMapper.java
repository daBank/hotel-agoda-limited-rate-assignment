package com.hotelbooking.svchotel.model.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CityJoinRoomMapper implements RowMapper<HotelDataByCity> {

    @Override
    public HotelDataByCity mapRow(ResultSet resultSet, int i) throws SQLException {
        return new HotelDataByCity().setCity(resultSet.getString("city"))
                .setRoomName(resultSet.getString("room_name"))
                .setPrice(resultSet.getBigDecimal("price"));
    }

}
