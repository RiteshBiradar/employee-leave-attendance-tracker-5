package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDBC implementation of {@link LeaveRequestDAO} backed by Oracle via {@link DBUtil}.
 *
 * <p>All SQL is contained within this class; no business logic here.
 */
public class LeaveRequestDAOImpl implements LeaveRequestDAO {

    // -----------------------------------------------------------------------
    // Save
    // -----------------------------------------------------------------------

    @Override
    public long save(LeaveRequest request) {
        long nextId = 1001;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement psId = conn.prepareStatement("SELECT NVL(MAX(REQUEST_ID), 1000) + 1 FROM LEAVE_REQUEST");
             ResultSet rsId = psId.executeQuery()) {
            if (rsId.next()) {
                nextId = rsId.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate REQUEST_ID", e);
        }

        final String sql =
            "INSERT INTO LEAVE_REQUEST " +
            "  (REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "   REASON, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, nextId);
            ps.setLong  (2, request.getEmployeeId());
            ps.setString(3, request.getLeaveType().name());
            ps.setDate  (4, Date.valueOf(request.getStartDate()));
            ps.setDate  (5, Date.valueOf(request.getEndDate()));
            ps.setString(6, request.getReason());
            ps.setString(7, request.getStatus().name());

            ps.executeUpdate();
            return nextId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save leave request", e);
        }
    }

    // -----------------------------------------------------------------------
    // Find by primary key
    // -----------------------------------------------------------------------

    @Override
    public LeaveRequest findById(long id) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       REASON, STATUS " +
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

    // -----------------------------------------------------------------------
    // Find all requests for one employee (leave history)
    // -----------------------------------------------------------------------

    @Override
    public List<LeaveRequest> findByEmployeeId(long employeeId) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       REASON, STATUS " +
            "FROM   LEAVE_REQUEST " +
            "WHERE  EMP_ID = ? " +
            "ORDER BY REQUEST_ID DESC";

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

    // -----------------------------------------------------------------------
    // Overlap check — PENDING or APPROVED requests whose date range intersects
    // -----------------------------------------------------------------------

    /**
     * Returns all PENDING or APPROVED leave requests for {@code employeeId} whose
     * date range overlaps [{@code startDate}, {@code endDate}].
     *
     * <p>Two ranges [A,B] and [C,D] overlap when A &le; D AND C &le; B.
     */
    @Override
    public List<LeaveRequest> findOverlapping(long employeeId,
                                              LocalDate startDate,
                                              LocalDate endDate) {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, " +
            "       REASON, STATUS " +
            "FROM   LEAVE_REQUEST " +
            "WHERE  EMP_ID = ? " +
            "  AND  STATUS      IN ('PENDING', 'APPROVED') " +
            "  AND  START_DATE  <= ? " +   // existing.START <= new.END
            "  AND  END_DATE    >= ?";     // existing.END   >= new.START

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

    // -----------------------------------------------------------------------
    // Update status (for manager approval / rejection — not used yet)
    // -----------------------------------------------------------------------

    @Override
    public void updateStatus(long id, LeaveStatus status) {
        final String sql =
            "UPDATE LEAVE_REQUEST SET STATUS = ? WHERE REQUEST_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong  (2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update status for leave request id=" + id, e);
        }
    }

    // -----------------------------------------------------------------------
    // Row mapping
    // -----------------------------------------------------------------------

    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setId          (rs.getLong  ("REQUEST_ID"));
        lr.setEmployeeId  (rs.getLong  ("EMP_ID"));
        lr.setLeaveType   (LeaveType.valueOf(rs.getString("LEAVE_TYPE")));
        lr.setStartDate   (rs.getDate  ("START_DATE").toLocalDate());
        lr.setEndDate     (rs.getDate  ("END_DATE").toLocalDate());
        lr.setReason      (rs.getString("REASON"));
        lr.setStatus      (LeaveStatus.valueOf(rs.getString("STATUS")));

        return lr;
    }
}
