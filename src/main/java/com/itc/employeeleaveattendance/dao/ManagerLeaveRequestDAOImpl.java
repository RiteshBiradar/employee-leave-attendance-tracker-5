package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
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

public class ManagerLeaveRequestDAOImpl implements ManagerLeaveRequestDAO {

    @Override
    public LeaveRequest findByIdForUpdate(long requestId, Connection connection) {
        String sql = "SELECT LEAVE_REQUEST_ID, EMPLOYEE_ID, LEAVE_TYPE, START_DATE, "
                + "END_DATE, NUMBER_OF_DAYS, REASON, STATUS, APPLIED_ON "
                + "FROM LEAVE_REQUEST WHERE LEAVE_REQUEST_ID = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRequest(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding leave request.", exception);
        }
    }

    @Override
    public List<PendingLeaveRequestDTO> findPendingByManagerId(long managerId) {
        String sql = "SELECT lr.LEAVE_REQUEST_ID, lr.EMPLOYEE_ID, e.NAME AS EMPLOYEE_NAME, "
                + "lr.LEAVE_TYPE, lr.START_DATE, lr.END_DATE, lr.NUMBER_OF_DAYS, "
                + "lr.REASON, lr.STATUS FROM LEAVE_REQUEST lr "
                + "JOIN EMPLOYEE e ON lr.EMPLOYEE_ID = e.EMP_ID "
                + "WHERE e.MANAGER_ID = ? AND lr.STATUS = 'PENDING' "
                + "ORDER BY lr.START_DATE ASC";
        List<PendingLeaveRequestDTO> requests = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, managerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PendingLeaveRequestDTO request = new PendingLeaveRequestDTO();
                    request.setRequestId(resultSet.getLong("LEAVE_REQUEST_ID"));
                    request.setEmpId(resultSet.getLong("EMPLOYEE_ID"));
                    request.setEmployeeName(resultSet.getString("EMPLOYEE_NAME"));
                    request.setLeaveType(LeaveType.valueOf(resultSet.getString("LEAVE_TYPE")));
                    request.setStartDate(resultSet.getDate("START_DATE").toLocalDate());
                    request.setEndDate(resultSet.getDate("END_DATE").toLocalDate());
                    request.setWorkingDays(resultSet.getInt("NUMBER_OF_DAYS"));
                    request.setReason(resultSet.getString("REASON"));
                    request.setStatus(LeaveStatus.valueOf(resultSet.getString("STATUS")));
                    requests.add(request);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while finding pending requests.", exception);
        }
        return requests;
    }

    @Override
    public void updateStatus(long requestId, LeaveStatus status, Connection connection) {
        String sql = "UPDATE LEAVE_REQUEST SET STATUS = ? "
                + "WHERE LEAVE_REQUEST_ID = ? AND STATUS = 'PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, requestId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("Leave request is no longer pending.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while updating leave status.", exception);
        }
    }

    private LeaveRequest mapRequest(ResultSet resultSet) throws SQLException {
        LeaveRequest request = new LeaveRequest();
        request.setId(resultSet.getLong("LEAVE_REQUEST_ID"));
        request.setEmployeeId(resultSet.getLong("EMPLOYEE_ID"));
        request.setLeaveType(LeaveType.valueOf(resultSet.getString("LEAVE_TYPE")));
        request.setStartDate(resultSet.getDate("START_DATE").toLocalDate());
        request.setEndDate(resultSet.getDate("END_DATE").toLocalDate());
        request.setNumberOfDays(resultSet.getInt("NUMBER_OF_DAYS"));
        request.setReason(resultSet.getString("REASON"));
        request.setStatus(LeaveStatus.valueOf(resultSet.getString("STATUS")));
        return request;
    }
}
