package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO for the {@code LEAVE_BALANCE} table.
 * All SQL is confined to this class; no business logic here.
 */
public class LeaveBalanceDAO {

    /**
     * Returns the leave balance row for the given employee.
     * Each employee has exactly one balance row (UNIQUE constraint on EMP_ID).
     *
     * @param empId the authenticated employee's ID (from session)
     * @return Optional containing the LeaveBalance, or empty if not yet seeded
     * @throws SQLException on database error
     */
    public Optional<LeaveBalance> findByEmpId(int empId) throws SQLException {
        final String sql =
            "SELECT BALANCE_ID, EMP_ID, CASUAL_BALANCE, SICK_BALANCE, EARNED_BALANCE " +
            "FROM   LEAVE_BALANCE " +
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

    // Maps the current ResultSet row to a LeaveBalance object
    private LeaveBalance mapRow(ResultSet rs) throws SQLException {
        return new LeaveBalance(
            rs.getInt("BALANCE_ID"),
            rs.getInt("EMP_ID"),
            rs.getDouble("CASUAL_BALANCE"),
            rs.getDouble("SICK_BALANCE"),
            rs.getDouble("EARNED_BALANCE")
        );
    }
}
