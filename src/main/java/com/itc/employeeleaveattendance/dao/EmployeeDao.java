package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.Employee;

public interface EmployeeDao {

    Employee findById(int empId);
}