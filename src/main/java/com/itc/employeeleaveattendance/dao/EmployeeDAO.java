package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO for the {@code EMPLOYEE} table.
 * All SQL is confined to this class; no business logic here.
 */
public class EmployeeDAO {

    /**
     * Finds an employee by EMP_ID AND PASSWORD.
     * Used for authentication: both fields must match the same row.
     * Password is stored as-is in the DB (VARCHAR2 100).
     *
     * @param empId    the employee ID submitted at login
     * @param password the password submitted at login
     * @return an Optional containing the Employee (without password), or empty if not found
     * @throws SQLException on database error
     */
    public Optional<Employee> findByEmpIdAndPassword(int empId, String password) throws SQLException {
        final String sql =
            "SELECT EMP_ID, NAME, EMAIL, ROLE, MANAGER_ID " +
            "FROM   EMPLOYEE " +
            "WHERE  EMP_ID = ? AND PASSWORD = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds an employee by primary key alone.
     * Used to reload employee details from the session EMP_ID when needed.
     *
     * @param empId the employee's primary key
     * @return an Optional containing the Employee, or empty if not found
     * @throws SQLException on database error
     */
    public Optional<Employee> findById(int empId) throws SQLException {
        final String sql =
            "SELECT EMP_ID, NAME, EMAIL, ROLE, MANAGER_ID " +
            "FROM   EMPLOYEE " +
            "WHERE  EMP_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    // Maps the current ResultSet row to an Employee object.
    // PASSWORD is intentionally excluded — never stored in the session.
    private Employee mapRow(ResultSet rs) throws SQLException {
        int     empId     = rs.getInt("EMP_ID");
        String  name      = rs.getString("NAME");
        String  email     = rs.getString("EMAIL");
        String  role      = rs.getString("ROLE");
        int     mgrRaw    = rs.getInt("MANAGER_ID");
        Integer managerId = rs.wasNull() ? null : mgrRaw;

        return new Employee(empId, name, email, role, managerId);
    }
}
