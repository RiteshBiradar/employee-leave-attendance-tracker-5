package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.Employee;

import java.sql.SQLException;

public class ManagerEmployeeLookupImpl implements ManagerEmployeeLookup {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    public Employee findById(long employeeId) {
        if (employeeId > Integer.MAX_VALUE || employeeId < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Employee ID is outside the supported range.");
        }
        try {
            return employeeDAO.findById((int) employeeId).orElse(null);
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding employee.", exception);
        }
    }
}
