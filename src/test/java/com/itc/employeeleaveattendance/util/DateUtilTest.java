package com.itc.employeeleaveattendance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DateUtil#calculateWorkingDays(LocalDate, LocalDate)}.
 *
 * All tests are deterministic and require no external resources.
 * Dates are fixed to specific calendar weeks to make assertions unambiguous.
 */
@DisplayName("DateUtil — calculateWorkingDays")
class DateUtilTest {

    // Monday 2025-01-06 → Friday 2025-01-10 (pure weekday range, no weekend)
    @Test
    @DisplayName("Mon–Fri weekday range → 5 working days")
    void calculateWorkingDays_weekdayRange_returnsCorrectDays() {
        LocalDate start = LocalDate.of(2025, 1, 6);  // Monday
        LocalDate end   = LocalDate.of(2025, 1, 10); // Friday

        int result = DateUtil.calculateWorkingDays(start, end);

        assertEquals(5, result, "A Mon-Fri range should produce exactly 5 working days");
    }

    // Friday 2025-01-10 → Monday 2025-01-13
    // Sat 11 and Sun 12 must be excluded → 2 working days (Fri + Mon)
    @Test
    @DisplayName("Fri–Mon spanning a weekend → 2 working days (Sat+Sun excluded)")
    void calculateWorkingDays_weekendRange_excludesWeekend() {
        LocalDate start = LocalDate.of(2025, 1, 10); // Friday
        LocalDate end   = LocalDate.of(2025, 1, 13); // Monday

        int result = DateUtil.calculateWorkingDays(start, end);

        assertEquals(2, result, "Sat and Sun must be excluded; only Fri and Mon count");
    }

    // Monday 2025-01-06 → Friday 2025-01-17 (two full Mon–Fri weeks)
    @Test
    @DisplayName("Two full Mon–Fri weeks → 10 working days")
    void calculateWorkingDays_multipleWeeks_returnsCorrectDays() {
        LocalDate start = LocalDate.of(2025, 1, 6);  // Monday  week 1
        LocalDate end   = LocalDate.of(2025, 1, 17); // Friday  week 2

        int result = DateUtil.calculateWorkingDays(start, end);

        assertEquals(10, result, "Two Mon-Fri weeks should produce exactly 10 working days");
    }

    // Same-day (Monday) → should count as 1 working day
    @Test
    @DisplayName("Same weekday start and end → 1 working day")
    void calculateWorkingDays_sameDay_returnsOne() {
        LocalDate date = LocalDate.of(2025, 1, 6); // Monday

        int result = DateUtil.calculateWorkingDays(date, date);

        assertEquals(1, result, "A single weekday should count as 1 working day");
    }

    @Test
    @DisplayName("Null startDate → IllegalArgumentException")
    void calculateWorkingDays_nullStartDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DateUtil.calculateWorkingDays(null, LocalDate.of(2025, 1, 10)),
                "Null startDate must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Null endDate → IllegalArgumentException")
    void calculateWorkingDays_nullEndDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DateUtil.calculateWorkingDays(LocalDate.of(2025, 1, 6), null),
                "Null endDate must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("endDate before startDate → IllegalArgumentException")
    void calculateWorkingDays_endBeforeStart_throwsException() {
        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end   = LocalDate.of(2025, 1, 6);  // before start

        assertThrows(IllegalArgumentException.class,
                () -> DateUtil.calculateWorkingDays(start, end),
                "endDate before startDate must throw IllegalArgumentException");
    }
}
