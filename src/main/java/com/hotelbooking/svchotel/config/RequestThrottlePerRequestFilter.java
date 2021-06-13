package com.hotelbooking.svchotel.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RequestThrottlePerRequestFilter extends OncePerRequestFilter {
    @Autowired
    private Map<String, Integer> rateLimittingMaxRequest; // read max requests on specific endpoint from config
    @Autowired
    private Map<String, RateLimit> requestCountsPerPath;
    @Value("${rate-limitting.paths:#{null}}")
    private List<String> rateLimittingPaths = new ArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String path = httpServletRequest.getServletPath();
        if (isMaximumRequestsExceeded(path)) {
            httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpServletResponse.getWriter().write("Too many requests");
            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !rateLimittingPaths.stream().anyMatch(path::startsWith);
    }

    private boolean isMaximumRequestsExceeded(String key) {
        RateLimit rateLimit = requestCountsPerPath.get(key);
        synchronized (rateLimit) {

            int requests = rateLimit.getCount();

                log.debug("KEY: {}, CACHE: {}, MAX: {}, BLOCKED:{}", key, requests, rateLimittingMaxRequest.get(key), rateLimit.getBlockRequestUntilDatetime());

            if (requests > rateLimittingMaxRequest.get(key)) {
                log.info("BLOCKING");
                rateLimit.blockIncomingRequest();
                return true;
            }

            requests++;
            rateLimit.write(requests);
            return false;
        }
    }
}
