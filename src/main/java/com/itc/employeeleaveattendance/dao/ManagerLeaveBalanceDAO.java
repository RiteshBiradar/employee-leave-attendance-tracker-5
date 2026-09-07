package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.LeaveBalance;

import java.sql.Connection;

public interface ManagerLeaveBalanceDAO {

    LeaveBalance findByEmployeeIdForUpdate(long employeeId, Connection connection);

    void deductBalance(long employeeId, String leaveType, int workingDays, Connection connection);
}
