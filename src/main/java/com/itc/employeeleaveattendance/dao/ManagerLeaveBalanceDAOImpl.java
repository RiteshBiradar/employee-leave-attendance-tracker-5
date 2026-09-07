package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.exception.LeaveBalanceException;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManagerLeaveBalanceDAOImpl implements ManagerLeaveBalanceDAO {

    @Override
    public LeaveBalance findByEmployeeIdForUpdate(long employeeId, Connection connection) {
        String sql = "SELECT BALANCE_ID, EMPLOYEE_ID, CASUAL_BALANCE, SICK_BALANCE, EARNED_BALANCE "
                + "FROM LEAVE_BALANCE WHERE EMPLOYEE_ID = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                LeaveBalance balance = new LeaveBalance();
                balance.setBalanceId(resultSet.getLong("BALANCE_ID"));
                balance.setEmployeeId(resultSet.getLong("EMPLOYEE_ID"));
                balance.setCasualBalance(resultSet.getDouble("CASUAL_BALANCE"));
                balance.setSickBalance(resultSet.getDouble("SICK_BALANCE"));
                balance.setEarnedBalance(resultSet.getDouble("EARNED_BALANCE"));
                return balance;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding leave balance.", exception);
        }
    }

    @Override
    public void deductBalance(long employeeId, String leaveType, int workingDays, Connection connection) {
        String column = switch (leaveType.toUpperCase()) {
            case "CASUAL" -> "CASUAL_BALANCE";
            case "SICK" -> "SICK_BALANCE";
            case "EARNED" -> "EARNED_BALANCE";
            default -> throw new LeaveBalanceException("Unsupported leave type: " + leaveType);
        };
        String sql = "UPDATE LEAVE_BALANCE SET " + column + " = " + column
                + " - ? WHERE EMPLOYEE_ID = ? AND " + column + " >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workingDays);
            statement.setLong(2, employeeId);
            statement.setInt(3, workingDays);
            if (statement.executeUpdate() != 1) {
                throw new LeaveBalanceException("Insufficient leave balance.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while deducting leave balance.", exception);
        }
    }
}
