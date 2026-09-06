package com.itc.employeeleaveattendance.dao.impl;

import com.itc.employeeleaveattendance.dao.EmployeeDao;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDaoImpl implements EmployeeDao {

    @Override
    public Employee findById(int empId) {
        String sql = "SELECT emp_id, name, email, role, manager_id, password "
                + "FROM employees WHERE emp_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, empId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setEmpId(resultSet.getInt("emp_id"));
                    employee.setName(resultSet.getString("name"));
                    employee.setEmail(resultSet.getString("email"));
                    employee.setRole(resultSet.getString("role"));
                    Number managerId = (Number) resultSet.getObject("manager_id");
                    employee.setManagerId(managerId == null ? null : managerId.intValue());
                    employee.setPassword(resultSet.getString("password"));
                    return employee;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding employee.", exception);
        }

        return null;
    }
}