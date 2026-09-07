package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.service.LeaveBalanceService;

public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceDAO leaveBalanceDAO;

    public LeaveBalanceServiceImpl(LeaveBalanceDAO leaveBalanceDAO) {
        this.leaveBalanceDAO = leaveBalanceDAO;
    }

    @Override
    public LeaveBalance getBalance(long employeeId, LeaveType leaveType) {
        return leaveBalanceDAO.findByEmployeeId(employeeId);
    }
}