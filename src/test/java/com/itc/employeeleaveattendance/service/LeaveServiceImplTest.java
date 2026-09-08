package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.exception.OverlappingLeaveException;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.service.impl.LeaveServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveServiceImpl#applyLeave(LeaveRequest)}.
 *
 * <p>Uses Mockito mocks for both DAOs — no database connection required.
 *
 * <p>Test window: Monday 2027-01-06 → Friday 2027-01-10 = 5 working days.
 */
@DisplayName("LeaveServiceImpl — applyLeave validation")
class LeaveServiceImplTest {

    // -----------------------------------------------------------------------
    // Shared fixtures
    // -----------------------------------------------------------------------

    private static final long EMP_ID = 10L;

    // Fixed weekday range: Tue 2027-02-02 → Thu 2027-02-04 = 3 working days
    private static final LocalDate START = LocalDate.of(2027, 2, 2);
    private static final LocalDate END   = LocalDate.of(2027, 2, 4);
    private static final int       DAYS  = 3;

    private LeaveRequestDAO mockLeaveRequestDAO;
    private LeaveBalanceDAO mockLeaveBalanceDAO;
    private LeaveService    leaveService;

    @BeforeEach
    void setUp() {
        mockLeaveRequestDAO = mock(LeaveRequestDAO.class);
        mockLeaveBalanceDAO = mock(LeaveBalanceDAO.class);
        leaveService = new LeaveServiceImpl(mockLeaveRequestDAO, mockLeaveBalanceDAO);
    }

    /** Builds a minimal CASUAL leave request for the shared date window. */
    private LeaveRequest buildRequest() {
        LeaveRequest req = new LeaveRequest();
        req.setEmployeeId(EMP_ID);
        req.setLeaveType(LeaveType.CASUAL);
        req.setStartDate(START);
        req.setEndDate(END);
        req.setReason("Unit test reason");
        return req;
    }

    // -----------------------------------------------------------------------
    // TC-01 — Insufficient balance
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-01 · Insufficient balance → InsufficientBalanceException; save() never called")
    void applyLeave_insufficientBalance_throwsException() {
        // Arrange — balance has only 2 casual days; request needs 5
        LeaveBalance thinBalance = new LeaveBalance(1L, EMP_ID, 2.0, 5.0, 10.0);
        when(mockLeaveBalanceDAO.findByEmployeeId(EMP_ID)).thenReturn(thinBalance);
        when(mockLeaveRequestDAO.findOverlapping(eq(EMP_ID), eq(START), eq(END)))
                .thenReturn(Collections.emptyList());

        LeaveRequest request = buildRequest();

        // Act + Assert
        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> leaveService.applyLeave(request),
                "Should throw InsufficientBalanceException when balance < requested days");

        assertNotNull(ex.getMessage(), "Exception message must not be null");
        verify(mockLeaveRequestDAO, never()).save(any(LeaveRequest.class));
    }

    // -----------------------------------------------------------------------
    // TC-02 — Overlapping leave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-02 · Overlapping PENDING request → OverlappingLeaveException; save() never called")
    void applyLeave_overlappingLeave_throwsException() {
        // Arrange — an existing PENDING request overlaps the requested window
        LeaveRequest existing = new LeaveRequest(
                99L, EMP_ID, LeaveType.CASUAL,
                LocalDate.of(2027, 2, 2), LocalDate.of(2027, 2, 2),
                1, "Existing request", LeaveStatus.PENDING, LocalDateTime.now());

        when(mockLeaveRequestDAO.findOverlapping(eq(EMP_ID), eq(START), eq(END)))
                .thenReturn(List.of(existing));

        // Balance is sufficient — but the overlap check runs first, so this
        // stub may not be reached; we stub it anyway to avoid NPE if ordering changes.
        LeaveBalance richBalance = new LeaveBalance(1L, EMP_ID, 20.0, 20.0, 20.0);
        when(mockLeaveBalanceDAO.findByEmployeeId(EMP_ID)).thenReturn(richBalance);

        LeaveRequest request = buildRequest();

        // Act + Assert
        OverlappingLeaveException ex = assertThrows(OverlappingLeaveException.class,
                () -> leaveService.applyLeave(request),
                "Should throw OverlappingLeaveException when existing request overlaps");

        assertNotNull(ex.getMessage(), "Exception message must not be null");
        verify(mockLeaveRequestDAO, never()).save(any(LeaveRequest.class));
    }

    // -----------------------------------------------------------------------
    // TC-03 — Happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-03 · Valid request, sufficient balance, no overlap → saved with PENDING status")
    void applyLeave_happyPath_savedWithPendingStatus() {
        // Arrange — 20 casual days available; no overlap
        LeaveBalance richBalance = new LeaveBalance(1L, EMP_ID, 20.0, 20.0, 20.0);
        when(mockLeaveBalanceDAO.findByEmployeeId(EMP_ID)).thenReturn(richBalance);
        when(mockLeaveRequestDAO.findOverlapping(eq(EMP_ID), eq(START), eq(END)))
                .thenReturn(Collections.emptyList());

        long generatedId = 42L;
        when(mockLeaveRequestDAO.save(any(LeaveRequest.class))).thenReturn(generatedId);

        LeaveRequest request = buildRequest();

        // Act
        long returnedId = leaveService.applyLeave(request);

        // Assert — save called once
        verify(mockLeaveRequestDAO, times(1)).save(request);

        // Assert — returned ID matches the stub
        assertEquals(generatedId, returnedId,
                "applyLeave must return the ID produced by LeaveRequestDAO.save()");

        // Assert — service stamped the correct status
        assertEquals(LeaveStatus.PENDING, request.getStatus(),
                "Status must be set to PENDING before persisting");

        // Assert — working days calculated correctly (Mon–Fri = 5)
        assertEquals(DAYS, request.getNumberOfDays(),
                "numberOfDays must equal the working-day count for Mon–Fri");

        // Assert — appliedOn was stamped
        assertNotNull(request.getAppliedOn(),
                "appliedOn must be set by the service before saving");
    }
}
