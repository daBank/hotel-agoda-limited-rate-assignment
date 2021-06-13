package com.hotelbooking.svchotel.service;

import com.hotelbooking.svchotel.model.Hotel;
import com.hotelbooking.svchotel.model.enumeration.SortTypeEnum;
import com.hotelbooking.svchotel.model.mapper.HotelDataByCity;
import com.hotelbooking.svchotel.model.response.CityResponse;
import com.hotelbooking.svchotel.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class HotelService {
    @Autowired
    private CityRepository cityRepository;

    public CityResponse retrieveHotelsByCity(String city, SortTypeEnum sort) {
        List<HotelDataByCity> hotelData = cityRepository.retrieveHotels(city);
        List<Hotel> hotels = new ArrayList<>();
        hotelData.forEach(record ->
                hotels.add(new Hotel().setName(record.getRoomName()).setPrice(record.getPrice()))
        );

        if (SortTypeEnum.DESC.equals(sort)) {
            hotels.sort(Comparator.comparing(Hotel::getPrice).reversed());
        } else {
            hotels.sort(Comparator.comparing(Hotel::getPrice));
        }

        return new CityResponse().setCity(city).setHotels(hotels);
    }

    public List<HotelDataByCity>  retrieveAllHotels(SortTypeEnum sort) {
        List<HotelDataByCity> hotelData = cityRepository.retrieveHotels(null);
        if (SortTypeEnum.DESC.equals(sort)) {
            hotelData.sort(Comparator.comparing(HotelDataByCity::getPrice).reversed());
        } else {
            hotelData.sort(Comparator.comparing(HotelDataByCity::getPrice));
        }

        return hotelData;
    }
}
