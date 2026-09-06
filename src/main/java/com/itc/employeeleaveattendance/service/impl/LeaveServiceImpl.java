package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.service.LeaveService;

import java.time.LocalDateTime;
import java.util.List;

public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestDAO leaveRequestDAO;

    public LeaveServiceImpl(LeaveRequestDAO leaveRequestDAO) {
        this.leaveRequestDAO = leaveRequestDAO;
    }

    @Override
    public long applyLeave(LeaveRequest request) {
        request.setStatus(LeaveStatus.PENDING);
        request.setAppliedOn(LocalDateTime.now());
        return leaveRequestDAO.save(request);
    }

    @Override
    public List<LeaveRequest> getLeaveHistory(long employeeId) {
        return leaveRequestDAO.findByEmployeeId(employeeId);
    }
}