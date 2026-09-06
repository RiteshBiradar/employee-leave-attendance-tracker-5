package com.itc.employeeleaveattendance.dao.impl;

import com.itc.employeeleaveattendance.dao.LeaveBalanceDao;
import com.itc.employeeleaveattendance.exception.LeaveBalanceException;
import com.itc.employeeleaveattendance.model.LeaveBalance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaveBalanceDaoImpl implements LeaveBalanceDao {

    @Override
    public LeaveBalance findByEmpId(int empId, Connection connection) {
        String sql = "SELECT emp_id, casual_balance, sick_balance, earned_balance "
                + "FROM leave_balances WHERE emp_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, empId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LeaveBalance balance = new LeaveBalance();
                    balance.setEmpId(resultSet.getInt("emp_id"));
                    balance.setCasualBalance(resultSet.getInt("casual_balance"));
                    balance.setSickBalance(resultSet.getInt("sick_balance"));
                    balance.setEarnedBalance(resultSet.getInt("earned_balance"));
                    return balance;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding leave balance.", exception);
        }
        return null;
    }

    @Override
    public void deductBalance(int empId, String leaveType, int workingDays, Connection connection) {
        String column = switch (leaveType.toUpperCase()) {
            case "CASUAL" -> "casual_balance";
            case "SICK" -> "sick_balance";
            case "EARNED" -> "earned_balance";
            default -> throw new LeaveBalanceException("Unsupported leave type: " + leaveType);
        };
        String sql = "UPDATE leave_balances SET " + column + " = " + column
                + " - ? WHERE emp_id = ? AND " + column + " >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workingDays);
            statement.setInt(2, empId);
            statement.setInt(3, workingDays);
            if (statement.executeUpdate() != 1) {
                throw new LeaveBalanceException("Insufficient leave balance.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while deducting leave balance.", exception);
        }
    }
}