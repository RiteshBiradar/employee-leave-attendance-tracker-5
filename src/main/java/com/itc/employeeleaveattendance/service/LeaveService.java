package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for employee leave operations.
 * Enforces that an employee can only access their own leave records.
 */
public class LeaveService {

    private final LeaveRequestDAO leaveRequestDAO;
    private final LeaveBalanceDAO leaveBalanceDAO;

    public LeaveService() {
        this.leaveRequestDAO = new LeaveRequestDAO();
        this.leaveBalanceDAO = new LeaveBalanceDAO();
    }

    // Package-private constructor for unit testing with mock DAOs
    LeaveService(LeaveRequestDAO leaveRequestDAO, LeaveBalanceDAO leaveBalanceDAO) {
        this.leaveRequestDAO = leaveRequestDAO;
        this.leaveBalanceDAO = leaveBalanceDAO;
    }

    /**
     * Returns the leave history for the given employee.
     * The empId MUST come from the authenticated session, not from user input,
     * to guarantee data isolation.
     *
     * @param empId the authenticated employee's ID (from session)
     * @return list of that employee's leave requests, newest first
     * @throws SQLException on database error
     */
    public List<LeaveRequest> getLeaveHistory(int empId) throws SQLException {
        return leaveRequestDAO.findByEmpId(empId);
    }

    /**
     * Returns the leave balance for the given employee.
     * The empId MUST come from the authenticated session.
     *
     * @param empId the authenticated employee's ID (from session)
     * @return Optional containing the LeaveBalance, or empty if not seeded for this employee
     * @throws SQLException on database error
     */
    public Optional<LeaveBalance> getLeaveBalance(int empId) throws SQLException {
        return leaveBalanceDAO.findByEmpId(empId);
    }
}
