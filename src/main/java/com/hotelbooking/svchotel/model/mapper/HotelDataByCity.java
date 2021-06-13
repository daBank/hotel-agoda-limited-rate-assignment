package com.hotelbooking.svchotel.model.mapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class HotelDataByCity {
    private String city;
    private String roomName;
    private BigDecimal price;
}
