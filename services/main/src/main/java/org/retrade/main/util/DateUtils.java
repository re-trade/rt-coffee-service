package org.retrade.main.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class DateUtils {
    public static LocalDateTime getPreviousFromDate(LocalDateTime fromDate, LocalDateTime toDate) {
        Duration duration = Duration.between(fromDate, toDate);
        return fromDate.minus(duration);
    }

    public static LocalDateTime getPreviousToDate(LocalDateTime fromDate) {
        return fromDate;
    }

    public static double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
}
