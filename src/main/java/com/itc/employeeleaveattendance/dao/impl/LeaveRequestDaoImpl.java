package com.itc.employeeleaveattendance.dao.impl;

import com.itc.employeeleaveattendance.dao.LeaveRequestDao;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestDaoImpl implements LeaveRequestDao {

    @Override
    public LeaveRequest findById(int requestId, Connection connection) {
        String sql = "SELECT request_id, emp_id, leave_type, start_date, end_date, "
                + "status, reason FROM leave_requests WHERE request_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRequest(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding leave request.", exception);
        }
        return null;
    }

    @Override
    public List<PendingLeaveRequestDTO> findPendingByManagerId(int managerId) {
        String sql = "SELECT lr.request_id, lr.emp_id, e.name AS employee_name, "
                + "lr.leave_type, lr.start_date, lr.end_date, lr.reason, lr.status "
                + "FROM leave_requests lr JOIN employees e ON lr.emp_id = e.emp_id "
                + "WHERE e.manager_id = ? AND lr.status = 'PENDING' ORDER BY lr.start_date ASC";
        List<PendingLeaveRequestDTO> requests = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PendingLeaveRequestDTO request = new PendingLeaveRequestDTO();
                    request.setRequestId(resultSet.getInt("request_id"));
                    request.setEmpId(resultSet.getInt("emp_id"));
                    request.setEmployeeName(resultSet.getString("employee_name"));
                    request.setLeaveType(resultSet.getString("leave_type"));
                    LocalDate startDate = resultSet.getDate("start_date").toLocalDate();
                    LocalDate endDate = resultSet.getDate("end_date").toLocalDate();
                    request.setStartDate(startDate);
                    request.setEndDate(endDate);
                    request.setWorkingDays(countWorkingDays(startDate, endDate));
                    request.setReason(resultSet.getString("reason"));
                    request.setStatus(resultSet.getString("status"));
                    requests.add(request);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding pending requests.", exception);
        }
        return requests;
    }

    @Override
    public void updateStatus(int requestId, String status, Connection connection) {
        String sql = "UPDATE leave_requests SET status = ? "
                + "WHERE request_id = ? AND status = 'PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, requestId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("Leave request is no longer pending.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while updating leave status.", exception);
        }
    }

    private LeaveRequest mapRequest(ResultSet resultSet) throws SQLException {
        LeaveRequest request = new LeaveRequest();
        request.setRequestId(resultSet.getInt("request_id"));
        request.setEmpId(resultSet.getInt("emp_id"));
        request.setLeaveType(resultSet.getString("leave_type"));
        request.setStartDate(resultSet.getDate("start_date").toLocalDate());
        request.setEndDate(resultSet.getDate("end_date").toLocalDate());
        request.setStatus(resultSet.getString("status"));
        request.setReason(resultSet.getString("reason"));
        return request;
    }

    private int countWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() < 6) {
                workingDays++;
            }
        }
        return workingDays;
    }
}