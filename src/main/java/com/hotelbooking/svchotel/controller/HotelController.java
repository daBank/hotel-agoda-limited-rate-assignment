package com.hotelbooking.svchotel.controller;

import com.hotelbooking.svchotel.model.enumeration.SortTypeEnum;
import com.hotelbooking.svchotel.model.mapper.HotelDataByCity;
import com.hotelbooking.svchotel.model.response.CityResponse;
import com.hotelbooking.svchotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/city")
    public CityResponse retrieveDataByCity(@RequestParam String city, @RequestParam(required = false) SortTypeEnum sort) {
        return hotelService.retrieveHotelsByCity(city, sort);
    }

    @GetMapping("/room")
    public List<HotelDataByCity> retrieveAllHotels(@RequestParam(required = false) SortTypeEnum sort) {
        return hotelService.retrieveAllHotels(sort);
    }
}
