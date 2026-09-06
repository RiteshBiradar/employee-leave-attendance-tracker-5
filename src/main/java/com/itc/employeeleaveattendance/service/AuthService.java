package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.dao.EmployeeDAO;
import com.itc.employeeleaveattendance.model.Employee;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Business logic for authentication.
 * Validates credentials and returns the authenticated Employee.
 */
public class AuthService {

    private final EmployeeDAO employeeDAO;

    public AuthService() {
        this.employeeDAO = new EmployeeDAO();
    }

    // Package-private constructor for unit testing with a mock DAO
    AuthService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    /**
     * Authenticates an employee using EMP_ID and PASSWORD.
     * Both fields must match a single row in the {@code EMPLOYEE} table.
     *
     * @param empId    the employee ID submitted at login
     * @param password the password submitted at login (must not be blank)
     * @return Optional containing the Employee on success, or empty on failure
     * @throws IllegalArgumentException if password is null or blank
     * @throws SQLException             on database error
     */
    public Optional<Employee> authenticate(int empId, String password) throws SQLException {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return employeeDAO.findByEmpIdAndPassword(empId, password);
    }
}
