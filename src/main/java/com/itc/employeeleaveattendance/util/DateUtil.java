package com.itc.employeeleaveattendance.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Utility methods for date calculations used across the leave module.
 */
public final class DateUtil {

    private DateUtil() {
        // Utility class — no instances
    }

    /**
     * Counts working days (Mon–Fri) between {@code startDate} and {@code endDate},
     * inclusive of both endpoints.
     *
     * <p>Saturdays and Sundays are excluded. No public-holiday logic is applied.
     *
     * @param startDate the first day of the leave period (inclusive)
     * @param endDate   the last  day of the leave period (inclusive)
     * @return number of working days; 0 if both dates fall on weekends
     * @throws IllegalArgumentException if either date is null, or if endDate is
     *                                  strictly before startDate
     */
    public static int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate must not be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "endDate (" + endDate + ") must not be before startDate (" + startDate + ")");
        }

        int workingDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        return workingDays;
    }
}
