package com.itc.employeeleaveattendance.model;

import com.itc.employeeleaveattendance.constant.LeaveType;

public class LeaveBalance {

    private long id;
    private long employeeId;
    private LeaveType leaveType;
    private int totalDays;
    private int usedDays;
    private int remainingDays;

    public LeaveBalance() {
    }

    public LeaveBalance(long id, long employeeId, LeaveType leaveType,
                        int totalDays, int usedDays, int remainingDays) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
        this.remainingDays = remainingDays;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(int usedDays) {
        this.usedDays = usedDays;
    }

    public int getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(int remainingDays) {
        this.remainingDays = remainingDays;
    }
}