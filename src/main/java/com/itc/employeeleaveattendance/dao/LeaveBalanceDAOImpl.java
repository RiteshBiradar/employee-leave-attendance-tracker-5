package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaveBalanceDAOImpl implements LeaveBalanceDAO {

    @Override
    public LeaveBalance findByEmployeeId(long employeeId) {
        final String sql =
            "SELECT BALANCE_ID, EMP_ID, CASUAL_BALANCE, SICK_BALANCE, EARNED_BALANCE " +
            "FROM LEAVE_BALANCE WHERE EMP_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find leave balance for employeeId=" + employeeId, e);
        }
        return null;
    }

    @Override
    public void updateBalance(LeaveBalance balance) {
        final String sql =
            "UPDATE LEAVE_BALANCE SET CASUAL_BALANCE = ?, SICK_BALANCE = ?, EARNED_BALANCE = ? " +
            "WHERE EMP_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, balance.getCasualBalance());
            ps.setDouble(2, balance.getSickBalance());
            ps.setDouble(3, balance.getEarnedBalance());
            ps.setLong(4, balance.getEmployeeId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update leave balance for employeeId=" + balance.getEmployeeId(), e);
        }
    }

    @Override
    public LeaveBalance findByEmployeeIdForUpdate(long employeeId, Connection connection) {
        final String sql =
            "SELECT BALANCE_ID, EMP_ID, CASUAL_BALANCE, SICK_BALANCE, EARNED_BALANCE " +
            "FROM LEAVE_BALANCE WHERE EMP_ID = ? FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find leave balance for update for employeeId=" + employeeId, e);
        }
        return null;
    }

    @Override
    public void deductBalance(long employeeId, LeaveType leaveType, int workingDays,
                               Connection connection) {
        String column = switch (leaveType) {
            case CASUAL -> "CASUAL_BALANCE";
            case SICK   -> "SICK_BALANCE";
            case EARNED -> "EARNED_BALANCE";
        };

        final String sql =
            "UPDATE LEAVE_BALANCE SET " + column + " = " + column + " - ? " +
            "WHERE EMP_ID = ? AND " + column + " >= ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, workingDays);
            ps.setLong(2, employeeId);
            ps.setInt(3, workingDays);
            if (ps.executeUpdate() != 1) {
                throw new InsufficientBalanceException("Insufficient leave balance.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while deducting leave balance for employeeId=" + employeeId, e);
        }
    }

    private LeaveBalance mapRow(ResultSet rs) throws SQLException {
        LeaveBalance balance = new LeaveBalance();
        balance.setBalanceId(rs.getLong("BALANCE_ID"));
        balance.setEmployeeId(rs.getLong("EMP_ID"));
        balance.setCasualBalance(rs.getDouble("CASUAL_BALANCE"));
        balance.setSickBalance(rs.getDouble("SICK_BALANCE"));
        balance.setEarnedBalance(rs.getDouble("EARNED_BALANCE"));
        return balance;
    }
}
