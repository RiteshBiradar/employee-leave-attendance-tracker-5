package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.model.LeaveRequest;

import java.util.List;

public interface LeaveService {

    long applyLeave(LeaveRequest request);

    List<LeaveRequest> getLeaveHistory(long employeeId);
}