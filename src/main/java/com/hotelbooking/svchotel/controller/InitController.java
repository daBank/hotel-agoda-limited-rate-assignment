package com.hotelbooking.svchotel.controller;

import com.hotelbooking.svchotel.service.InitHotelDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class InitController {
    @Autowired
    private InitHotelDataService initHotelDataService;

    @PostMapping("/init")
    public Map<String, Object> initData() {
        return initHotelDataService.initMock();
    }
}
