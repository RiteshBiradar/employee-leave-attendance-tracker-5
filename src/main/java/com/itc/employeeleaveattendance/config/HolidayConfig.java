package com.itc.employeeleaveattendance.config;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for mandatory holidays.
 */
public class HolidayConfig {

    public static final Set<LocalDate> MANDATORY_HOLIDAYS;

    static {
        Set<LocalDate> holidays = new HashSet<>();
        // Sample mandatory holidays for 2026
        holidays.add(LocalDate.of(2026, Month.JANUARY, 26)); // Republic Day
        holidays.add(LocalDate.of(2026, Month.AUGUST, 15));  // Independence Day
        holidays.add(LocalDate.of(2026, Month.OCTOBER, 2));  // Gandhi Jayanti
        
        // Add more holidays as needed for other years or dates
        
        MANDATORY_HOLIDAYS = Collections.unmodifiableSet(holidays);
    }

    /**
     * Checks if a given date is a mandatory holiday.
     * @param date the date to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return MANDATORY_HOLIDAYS.contains(date);
    }
}
