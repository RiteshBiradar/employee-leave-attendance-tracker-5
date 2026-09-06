package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the {@code LEAVE_REQUEST} table.
 * All SQL is confined to this class; no business logic here.
 */
public class LeaveRequestDAO {

    /**
     * Returns all leave requests belonging to the given employee.
     * The WHERE EMP_ID = ? clause enforces per-employee data isolation at the DB level.
     *
     * @param empId the authenticated employee's ID (taken from session, never from user input)
     * @return list of that employee's leave requests, ordered newest-first
     * @throws SQLException on database error
     */
    public List<LeaveRequest> findByEmpId(int empId) throws SQLException {
        final String sql =
            "SELECT REQUEST_ID, EMP_ID, LEAVE_TYPE, START_DATE, END_DATE, STATUS, REASON " +
            "FROM   LEAVE_REQUEST " +
            "WHERE  EMP_ID = ? " +
            "ORDER  BY START_DATE DESC";

        List<LeaveRequest> results = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    // Maps the current ResultSet row to a LeaveRequest object
    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        return new LeaveRequest(
            rs.getInt("REQUEST_ID"),
            rs.getInt("EMP_ID"),
            rs.getString("LEAVE_TYPE"),
            rs.getDate("START_DATE").toLocalDate(),
            rs.getDate("END_DATE").toLocalDate(),
            rs.getString("STATUS"),
            rs.getString("REASON")
        );
    }
}
