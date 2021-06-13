package com.hotelbooking.svchotel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Hotel {
    private String name;
    private BigDecimal price;
}
