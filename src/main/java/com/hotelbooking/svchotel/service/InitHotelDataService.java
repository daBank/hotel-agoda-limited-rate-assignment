package com.hotelbooking.svchotel.service;

import com.hotelbooking.svchotel.model.mapper.City;
import com.hotelbooking.svchotel.model.mapper.Room;
import com.hotelbooking.svchotel.repository.CityRepository;
import com.hotelbooking.svchotel.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.hotelbooking.svchotel.config.CommonConstants.CITY_KEY;
import static com.hotelbooking.svchotel.config.CommonConstants.ROOM_KEY;

@Service
@Slf4j
public class InitHotelDataService {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private RoomRepository roomRepository;

    public Map<String, Object> initMock() {
        int citiesCount = cityRepository.getCountCities();
        int roomsCount = roomRepository.getCountRooms();
        boolean isSuccess = true;
        if (citiesCount == 0) {
            log.debug("Not found any data. Starting to init...");
            isSuccess = this.initHotelData();
        }

        Map<String, Object> results = new HashMap<>();
        results.put("cities", citiesCount);
        results.put("rooms", roomsCount);
        if (isSuccess) {
            results.put("status", "Success");
            results.put("description", citiesCount != 0? "Data already existed":"Init data");
        } else {
            results.put("status", "Failed");
            results.put("description", "");
        }
        return results;
    }

    private boolean initHotelData() {
        try {
            Map<String, Object> hotelDbMapping = this.readHotelDb();
            log.debug("hotelDbMapping: {}", hotelDbMapping);
            cityRepository.initCities((List<City>) (hotelDbMapping.get(CITY_KEY)));
            roomRepository.initRooms((List<Room>) (hotelDbMapping.get(ROOM_KEY)));
            return true;
        } catch (Exception ex) {
            log.error("Error when init data", ex);
            return false;
        }
    }

    private Map<String, Object> readHotelDb() throws IOException {
        Map<String, Object> hotelMapping =new HashMap<>();
        Set<String> cities = new HashSet<>();
        String row;
        int lineNo = 1;
        Map<String, List<Room>> roomMapping = new HashMap<>();

        // read from file hoteldb.csv
        try (BufferedReader csvReader = new BufferedReader( new InputStreamReader(this.getClass().getResourceAsStream("/" + "hoteldb.csv")))) {
            while ((row = csvReader.readLine()) != null) {
                if (lineNo == 1) {
                    lineNo++;
                    continue;
                }
                String[] data = row.split(",");
                // do something with the data
                cities.add(data[0]);
                if (roomMapping.containsKey(data[0])) {
                    roomMapping.get(data[0]).add(new Room().setHotelId(data[1]).setCityName(data[0]).setName(data[2]).setPrice(new BigDecimal(data[3])));
                } else {
                    roomMapping.put(data[0], new ArrayList<>(Arrays.asList(new Room().setCityName(data[0]).setHotelId(data[1]).setName(data[2]).setPrice(new BigDecimal(data[3])))));
                }
            }
        }

        //map cities
        List<City> cityList = cities.stream().map(city -> new City().setId(UUID.randomUUID().toString()).setName(city)).collect(Collectors.toList());
        HashMap<String, String> cityIdMapping = cityList.stream().collect(HashMap::new, (map, city) -> map.put(city.getName(), city.getId()), Map::putAll);

        // map room
        List<Room> roomList = new ArrayList<>();
        roomMapping.forEach((cityName, roomDetails) -> {
            roomDetails.stream().forEach(roomDetail ->
                    roomList.add(roomDetail.setCityId(cityIdMapping.get(roomDetail.getCityName()))));
        });

        hotelMapping.put(CITY_KEY, cityList);
        hotelMapping.put(ROOM_KEY, roomList);
        return hotelMapping;
    }

}
