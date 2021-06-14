package com.hotelbooking.svchotel.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class RateLimit {
    private String key;
    private Integer count;
    private ZonedDateTime refreshDatetime;
    private ZonedDateTime lastAccessDatetime;
    private Integer expiredAfterWrite;
    private ZonedDateTime blockRequestUntilDatetime;

    public boolean isExpired() {
        return ZonedDateTime.now().compareTo(refreshDatetime.plus(expiredAfterWrite, ChronoUnit.SECONDS)) > 0;
    }

    public ZonedDateTime getExpiredDatetime() {
        return refreshDatetime.plus(this.expiredAfterWrite, ChronoUnit.SECONDS);
    }

    public boolean toRefreshRateCount() {
        return (this.blockRequestUntilDatetime != null && this.blockRequestUntilDatetime.compareTo(ZonedDateTime.now()) < 0) || this.isExpired();
    }
    public int write(int count) {
        if (this.toRefreshRateCount()) {

            this.count = count;
            this.refreshDatetime = ZonedDateTime.now();
            this.lastAccessDatetime = ZonedDateTime.now();
            this.blockRequestUntilDatetime = null;
        } else {
            this.count = count;
            this.lastAccessDatetime = ZonedDateTime.now();
        }
        return count;
    }

    public boolean isAllowIncomingRequest() {
        return blockRequestUntilDatetime == null || this.blockRequestUntilDatetime.compareTo(ZonedDateTime.now()) < 0;
    }

    public void blockIncomingRequest() {
        if (this.blockRequestUntilDatetime == null) {
            // If the rate gets higher than the threshold on an endpoint, the API should stop responding for 5
            // seconds on that endpoint ONLY, before allowing other requests
            this.blockRequestUntilDatetime = ZonedDateTime.now().plus(5, ChronoUnit.SECONDS);
        }
    }

    public Integer getCount() {
        if (this.toRefreshRateCount()) {
            return this.write(0);
        }
        return this.count;
    }
}
