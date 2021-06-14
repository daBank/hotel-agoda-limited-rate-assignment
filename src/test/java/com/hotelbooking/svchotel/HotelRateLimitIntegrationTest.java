package com.hotelbooking.svchotel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.svchotel.config.RequestThrottlePerRequestFilter;
import com.hotelbooking.svchotel.model.mapper.HotelDataByCity;
import com.hotelbooking.svchotel.model.response.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class HotelRateLimitIntegrationTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private RequestThrottlePerRequestFilter requestThrottlePerRequestFilter;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(requestThrottlePerRequestFilter).build();
    }

    /******************************************************************************
     * Feature: GET /city returns collect result
     *   GET /city return hotels of the input city and I might want to sorted price.
     *
     * Given I retrieve hotels from GET /city
     *     When I call GET /city
     *     And I specify city name with a specific city name e.g. Bangkok
     *     And optionally, I want sorted result by price in descending or ascending order
     *     Then I should get correctly hotels of the city and price in the correct order
     *
     */
    @Test
    public void orderA_testOneCallCity() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        CityResponse response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CityResponse.class);
        sleep(5000); // sleep 5 seconds then can test next test case
        assertEquals("Bangkok", response.getCity());
        assertEquals(7, response.getHotels().size());
    }

    /******************************************************************************
     * Feature: GET /room returns collect result
     *   GET /room return all hotels and I might want to sorted price.
     *
     * Given I retrieve hotels from GET /room
     *     When I call GET /room
     *     And optionally, I can sort result by price in descending or ascending order
     *     Then I should get correctly hotels data with price in the correct order
     *
     */
    @Test
    public void orderB_testOneCallRoom() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/room?sort=DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        List<HotelDataByCity> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<HotelDataByCity>>() {
        });
        assertEquals(26, response.size());
        assertEquals(new BigDecimal(30000).setScale(2, RoundingMode.UNNECESSARY), response.get(0).getPrice());
        assertEquals("Amsterdam", response.get(0).getCity());
        assertEquals("Sweet Suite", response.get(0).getRoomName());
    }

    /******************************************************************************
     * Feature: GET /city can stop responding after the limited-rate gets higher than the threshold
     *   If the rate gets higher than the threshold on an endpoint, the API should stop responding.
     *
     *
     * Given The `/city` endpoint can receive maximum 10 requests every 5 seconds
     *     When I call GET /city at 11st time with 5 seconds
     *     Then I should get 429 http status.
     *
     */
    @Test
    public void orderC_testRateLimitCity_blockTheRestRequest() throws Exception {
        for (int i = 0; i < 15; i++) {
            if (i >= 10) {
                log.debug("Block request at index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                        .andDo(print())
                        .andExpect(status().is(429));

            } else {
                log.debug("index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
            }
        }
        sleep(5000); // sleep 5 seconds then can test next test case
    }


    /******************************************************************************
     * Feature: GET /city can stop responding after the limited-rate gets higher than the threshold
     *   If the rate gets higher than the threshold on an endpoint, the API should stop responding for 5 seconds on that endpoint ONLY, before allowing other requests
     *
     *
     * Given The `/city` endpoint can receive maximum 10 requests every 5 seconds
     *     When I call GET /city at 11st time with 5 seconds
     *     Then I should get 429 http status instead of hotel result ONLY for 5 seconds.
     *     Then I still can successfully call to `/room`.
     */
    @Test
    public void orderD_testRateLimitCity_blockFiveSeconds() throws Exception {
        for (int i = 0; i < 15; i++) {
            if (i == 10) {
                log.debug("Block request at index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                        .andDo(print())
                        .andExpect(status().is(429));
                mockMvc.perform(MockMvcRequestBuilders.get("/room"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
                sleep(5000);

            } else {
                log.debug("index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
            }
        }
        sleep(5000); // sleep 5 seconds then can test next test case
    }

    /******************************************************************************
     * Feature: GET /city can unblock stop responding after 5 seconds
     *   If the rate gets higher than the threshold on an endpoint, the API should stop responding for 5 seconds on that endpoint, before allowing other requests.
     *   After that, it can receive new requests.
     *
     * Given The `/city` endpoint can receive maximum 10 requests every 5 seconds
     *     When I call GET /city wait after blocking for 5 seconds
     *     Then I successfully send request to GET /city
     *
     */
    @Test
    public void orderE_testRateLimitCity_blockFiveSecondsAndThenUnbLock() throws Exception {
        for (int i = 0; i < 25; i++) {
            if (i == 10 || i == 21) {
                log.debug("Block request at index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                        .andDo(print())
                        .andExpect(status().is(429));
                sleep(5000);

            } else {
                log.debug("index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/city?city=Bangkok"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
            }
        }
    }

    /******************************************************************************
     * Feature: GET /room can stop responding after the limited-rate gets higher than the threshold
     *   If the rate gets higher than the threshold on an endpoint, the API should stop responding for 5 seconds on that endpoint, before allowing other requests
     *
     *
     * Given The `/room` endpoint can receive maximum 100 requests every 10 seconds
     *     When I call GET /room at 101st time with 10 seconds
     *     Then I should get 429 http status instead of hotel result for 5 seconds.
     */
    @Test
    public void orderF_testRateLimitRoom_blockFiveSeconds() throws Exception {
        for (int i = 0; i < 150; i++) {
            if (i == 100) {
                log.debug("Block request at index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/room?sort=DESC"))
                        .andDo(print())
                        .andExpect(status().is(429));
                sleep(5000);

            } else {
                log.debug("index: {}", i);
                mockMvc.perform(MockMvcRequestBuilders.get("/room?sort=DESC"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
            }
        }
    }

}
