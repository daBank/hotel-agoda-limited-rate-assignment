package com.hotelbooking.svchotel.config;//package com.hotelbooking.svchotel.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ThrottleRateLimitConfig {

    @Autowired
    private Environment env;
    @Value("${rate-limitting.paths:#{null}}")
    private List<String> rateLimittingPaths = new ArrayList<>();
    @Value("${rate-limitting.max.requests.default:50}")
    private String defaultMaxRequest;
    @Value("${rate-limitting.specific-period.default:10}")
    private String defaultPeriod;

    @Bean
    public Map<String, Integer> rateLimittingMaxRequest() {
        Map<String, Integer> maxRequests = new HashMap<>();
        rateLimittingPaths.stream().forEach(path -> {
                    log.debug("PATH: {}", path);
                    String maxRequest = env.getProperty("rate-limitting.max.requests." + path);

                    maxRequests.put(path, StringUtils.isEmpty(maxRequest) ? Integer.valueOf(defaultMaxRequest) : Integer.valueOf(maxRequest));
                }
        );
        return maxRequests;
    }

    @Bean
    public Map<String, RateLimit> requestCountsPerPath() {
        Map<String, RateLimit> cacheMapping = new ConcurrentHashMap<>();
        rateLimittingPaths.stream().forEach(path -> {
            String period = env.getProperty("rate-limitting.specific-period." + path);
            Integer expirePeriod = StringUtils.isEmpty(period) ? Integer.valueOf(defaultPeriod) : Integer.valueOf(period);
            cacheMapping.put(path, new RateLimit().setKey(path)
                    .setCount(0)
                    .setExpiredAfterWrite(expirePeriod)
                    .setRefreshDatetime(ZonedDateTime.now())
                    .setLastAccessDatetime(ZonedDateTime.now()));
        });
        return cacheMapping;
    }

}
