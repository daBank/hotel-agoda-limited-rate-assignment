package com.hotelbooking.svchotel.model.response;

import com.hotelbooking.svchotel.model.Hotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CityResponse {
    private String city;
    private List<Hotel> hotels;
}
