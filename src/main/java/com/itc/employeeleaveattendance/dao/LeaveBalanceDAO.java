package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.model.LeaveBalance;

import java.sql.Connection;

public interface LeaveBalanceDAO {

    LeaveBalance findByEmployeeId(long employeeId);

    void updateBalance(LeaveBalance balance);

    LeaveBalance findByEmployeeIdForUpdate(long employeeId, Connection connection);

    void deductBalance(long employeeId, LeaveType leaveType, int workingDays,
                       Connection connection);
}