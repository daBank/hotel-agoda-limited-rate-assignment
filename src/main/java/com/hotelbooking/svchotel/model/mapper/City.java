package com.hotelbooking.svchotel.model.mapper;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class City {
    private String id;
    private String name;
}
