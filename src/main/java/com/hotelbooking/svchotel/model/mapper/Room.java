package com.hotelbooking.svchotel.model.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class Room {
    private String hotelId;
    private String name;
    private String cityId;
    private BigDecimal price;

    @JsonIgnore
    private String cityName;
}
