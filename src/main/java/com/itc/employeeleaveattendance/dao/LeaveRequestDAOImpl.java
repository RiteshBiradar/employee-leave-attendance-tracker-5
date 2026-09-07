package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.util.DBUtil;
import com.itc.employeeleaveattendance.util.DateUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link LeaveRequestDAO} backed by Oracle via {@link DBUtil}.
 *
 * <p>Uses canonical Oracle schema:
 * LEAVE_REQUEST: REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, STATUS, REASON, APPLIED_ON.
 */
public class LeaveRequestDAOImpl implements LeaveRequestDAO {

    @Override
    public long save(LeaveRequest request) {
        final String sql =
            "INSERT INTO LEAVE_REQUEST " +
            "  (EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "   REASON, STATUS, APPLIED_ON) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"REQUEST_ID"})) {

            ps.setLong(1, request.getEmployeeId());
            ps.setString(2, request.getLeaveType().name());
            ps.setDate(3, Date.valueOf(request.getStartDate()));
            ps.setDate(4, Date.valueOf(request.getEndDate()));
            ps.setString(5, request.getReason());
            ps.setString(6, request.getStatus().name());
            ps.setObject(7, request.getAppliedOn());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save leave request", e);
        }
        throw new RuntimeException("Save succeeded but no generated key was returned");
    }

    @Override
    public LeaveRequest findById(long id) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       REASON, STATUS, APPLIED_ON " +
            "FROM   LEAVE_REQUEST " +
            "WHERE  REQUEST_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find leave request by id=" + id, e);
        }
        return null;
    }

    @Override
    public LeaveRequest findByIdForUpdate(long id, Connection connection) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       STATUS, REASON, APPLIED_ON " +
            "FROM LEAVE_REQUEST " +
            "WHERE REQUEST_ID = ? " +
            "FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find leave request for update by id=" + id, e);
        }
        return null;
    }

    @Override
    public List<LeaveRequest> findByEmployeeId(long employeeId) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       REASON, STATUS, APPLIED_ON " +
            "FROM   LEAVE_REQUEST " +
            "WHERE  EMP_ID = ? " +
            "ORDER BY APPLIED_ON DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<LeaveRequest> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch leave history for employeeId=" + employeeId, e);
        }
    }

    @Override
    public List<LeaveRequest> findOverlapping(long employeeId,
                                              LocalDate startDate,
                                              LocalDate endDate) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       REASON, STATUS, APPLIED_ON " +
            "FROM   LEAVE_REQUEST " +
            "WHERE  EMP_ID = ? " +
            "  AND  STATUS     IN ('PENDING', 'APPROVED') " +
            "  AND  START_DATE <= ? " +
            "  AND  END_DATE   >= ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, employeeId);
            ps.setDate(2, Date.valueOf(endDate));
            ps.setDate(3, Date.valueOf(startDate));

            try (ResultSet rs = ps.executeQuery()) {
                List<LeaveRequest> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check overlapping leaves for employeeId=" + employeeId, e);
        }
    }

    @Override
    public void updateStatus(long id, LeaveStatus status) {
        final String sql =
            "UPDATE LEAVE_REQUEST SET STATUS = ? WHERE REQUEST_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update status for leave request id=" + id, e);
        }
    }

    @Override
    public void updateStatus(long id, LeaveStatus status, Connection connection) {
        final String sql =
            "UPDATE LEAVE_REQUEST SET STATUS = ? WHERE REQUEST_ID = ? AND STATUS = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("Leave request is no longer pending.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating leave status for requestId=" + id, e);
        }
    }

    @Override
    public List<PendingLeaveRequestDTO> findPendingByManagerId(int managerId) {
        final String sql =
            "SELECT lr.REQUEST_ID, " +
            "       lr.EMP_ID, " +
            "       e.NAME, " +
            "       lr.LEAVE_TYPE, " +
            "       lr.START_DATE, " +
            "       lr.END_DATE, " +
            "       lr.REASON, " +
            "       lr.STATUS " +
            "FROM LEAVE_REQUEST lr " +
            "JOIN EMPLOYEE e ON e.EMP_ID = lr.EMP_ID " +
            "WHERE e.MANAGER_ID = ? " +
            "  AND lr.STATUS = 'PENDING' " +
            "ORDER BY lr.START_DATE";

        List<PendingLeaveRequestDTO> requests = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, managerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PendingLeaveRequestDTO dto = new PendingLeaveRequestDTO();
                    dto.setRequestId(rs.getLong("REQUEST_ID"));
                    dto.setEmpId(rs.getLong("EMP_ID"));
                    dto.setEmployeeName(rs.getString("NAME"));
                    dto.setLeaveType(LeaveType.valueOf(rs.getString("LEAVE_TYPE")));
                    LocalDate start = rs.getDate("START_DATE").toLocalDate();
                    LocalDate end = rs.getDate("END_DATE").toLocalDate();
                    dto.setStartDate(start);
                    dto.setEndDate(end);
                    dto.setWorkingDays(DateUtil.calculateWorkingDays(start, end));
                    dto.setReason(rs.getString("REASON"));
                    dto.setStatus(LeaveStatus.valueOf(rs.getString("STATUS")));
                    requests.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding pending requests for managerId=" + managerId, e);
        }
        return requests;
    }

    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(rs.getLong("REQUEST_ID"));
        lr.setEmployeeId(rs.getLong("EMP_ID"));
        lr.setLeaveType(LeaveType.valueOf(rs.getString("LEAVE_TYPE")));
        LocalDate startDate = rs.getDate("START_DATE").toLocalDate();
        LocalDate endDate = rs.getDate("END_DATE").toLocalDate();
        lr.setStartDate(startDate);
        lr.setEndDate(endDate);
        lr.setNumberOfDays(DateUtil.calculateWorkingDays(startDate, endDate));
        lr.setReason(rs.getString("REASON"));
        lr.setStatus(LeaveStatus.valueOf(rs.getString("STATUS")));

        Object ts = rs.getObject("APPLIED_ON");
        if (ts instanceof java.sql.Timestamp sqlTs) {
            lr.setAppliedOn(sqlTs.toLocalDateTime());
        } else if (ts instanceof LocalDateTime ldt) {
            lr.setAppliedOn(ldt);
        }
        return lr;
    }
}
