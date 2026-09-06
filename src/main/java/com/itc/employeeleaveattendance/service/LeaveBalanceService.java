package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.model.LeaveBalance;

public interface LeaveBalanceService {

    LeaveBalance getBalance(long employeeId, LeaveType leaveType);
}