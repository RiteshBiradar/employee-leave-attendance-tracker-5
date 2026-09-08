package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.config.HolidayConfig;
import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.exception.OverlappingLeaveException;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.service.LeaveService;
import com.itc.employeeleaveattendance.util.DateUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for leave management.
 *
 * <p>{@link #applyLeave} performs the following validations in order before persisting:
 * <ol>
 *   <li>Calculate working days via {@link DateUtil#calculateWorkingDays}.</li>
 *   <li>Check for overlapping PENDING/APPROVED requests ({@link OverlappingLeaveException}).</li>
 *   <li>Check that the employee has enough balance ({@link InsufficientBalanceException}).</li>
 * </ol>
 *
 * <p>Balance deduction and approval/rejection logic are NOT part of this class yet.
 */
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestDAO leaveRequestDAO;
    private final LeaveBalanceDAO leaveBalanceDAO;

    public LeaveServiceImpl(LeaveRequestDAO leaveRequestDAO,
                            LeaveBalanceDAO leaveBalanceDAO) {
        this.leaveRequestDAO = leaveRequestDAO;
        this.leaveBalanceDAO  = leaveBalanceDAO;
    }

    // -----------------------------------------------------------------------
    // applyLeave — validate, stamp, persist
    // -----------------------------------------------------------------------

    @Override
    public long applyLeave(LeaveRequest request) {

        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("Leave start date cannot be in the past.");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        
        DayOfWeek startDay = request.getStartDate().getDayOfWeek();
        DayOfWeek endDay = request.getEndDate().getDayOfWeek();
        
        if (startDay == DayOfWeek.SATURDAY || startDay == DayOfWeek.SUNDAY || 
            endDay == DayOfWeek.SATURDAY || endDay == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Leave cannot be applied for weekends.");
        }

        if (HolidayConfig.isHoliday(request.getStartDate()) || 
            HolidayConfig.isHoliday(request.getEndDate())) {
            throw new IllegalArgumentException("Selected date is a mandatory holiday.");
        }
        
        if (request.getLeaveType() == LeaveType.CASUAL) {
            LocalDate current = request.getStartDate();
            while (!current.isAfter(request.getEndDate())) {
                if (current.getDayOfWeek() == DayOfWeek.FRIDAY || current.getDayOfWeek() == DayOfWeek.MONDAY) {
                    throw new IllegalArgumentException("Casual Leave cannot be applied on restricted weekday dates (Monday, Friday).");
                }
                current = current.plusDays(1);
            }
        }

        // 1. Calculate working days and stamp the request
        int workingDays = DateUtil.calculateWorkingDays(
                request.getStartDate(), request.getEndDate());
                
        if (workingDays <= 0) {
            throw new IllegalArgumentException("Leave cannot be applied for weekends.");
        }

        request.setNumberOfDays(workingDays);

        // 2. Overlap check — reject if an existing PENDING/APPROVED request clashes
        List<LeaveRequest> overlaps = leaveRequestDAO.findOverlapping(
                request.getEmployeeId(),
                request.getStartDate(),
                request.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new OverlappingLeaveException(
                    "You already have a leave request that overlaps with the selected dates " +
                    "(" + request.getStartDate() + " – " + request.getEndDate() + "). " +
                    "Please choose different dates or cancel the existing request.");
        }

        // 3. Balance check — reject if employee has insufficient leave for that type
        LeaveBalance balance = leaveBalanceDAO.findByEmployeeId(request.getEmployeeId());
        if (balance != null) {
            double available = availableBalance(balance, request.getLeaveType());
            if (workingDays > available) {
                throw new InsufficientBalanceException(
                        "Insufficient " + request.getLeaveType().name().toLowerCase() +
                        " leave balance. Requested: " + workingDays +
                        " day(s), Available: " + (int) available + " day(s).");
            }
        }

        // 4. Stamp system fields and persist
        request.setStatus(LeaveStatus.PENDING);
        request.setAppliedOn(LocalDateTime.now());
        return leaveRequestDAO.save(request);
    }

    // -----------------------------------------------------------------------
    // getLeaveHistory
    // -----------------------------------------------------------------------

    @Override
    public List<LeaveRequest> getLeaveHistory(long employeeId) {
        return leaveRequestDAO.findByEmployeeId(employeeId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the balance figure for the requested leave type.
     * Centralises the switch so the rest of the method stays readable.
     */
    private double availableBalance(LeaveBalance balance, LeaveType leaveType) {
        return switch (leaveType) {
            case CASUAL -> balance.getCasualBalance();
            case SICK   -> balance.getSickBalance();
            case EARNED -> balance.getEarnedBalance();
        };
    }
}