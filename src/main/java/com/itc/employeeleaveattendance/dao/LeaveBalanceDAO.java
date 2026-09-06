package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.LeaveBalance;

public interface LeaveBalanceDAO {

    LeaveBalance findByEmployeeId(long employeeId);

    void updateBalance(LeaveBalance balance);
}