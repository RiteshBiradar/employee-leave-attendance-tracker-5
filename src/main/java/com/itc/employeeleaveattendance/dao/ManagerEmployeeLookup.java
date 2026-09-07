package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.Employee;

public interface ManagerEmployeeLookup {

    Employee findById(long employeeId);
}
