package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.LeaveBalance;

import java.sql.Connection;

public interface LeaveBalanceDao {

    LeaveBalance findByEmpId(int empId, Connection connection);

    void deductBalance(int empId, String leaveType, int workingDays, Connection connection);
}