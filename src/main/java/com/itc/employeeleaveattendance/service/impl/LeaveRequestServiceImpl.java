package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.dao.EmployeeDAO;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAOImpl;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAOImpl;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.exception.AuthorizationException;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.exception.InvalidLeaveRequestException;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.service.ConnectionProvider;
import com.itc.employeeleaveattendance.service.LeaveRequestService;
import com.itc.employeeleaveattendance.util.DBUtil;
import com.itc.employeeleaveattendance.util.DateUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final EmployeeDAO employeeDAO;
    private final LeaveBalanceDAO leaveBalanceDAO;
    private final LeaveRequestDAO leaveRequestDAO;
    private final ConnectionProvider connectionProvider;

    public LeaveRequestServiceImpl() {
        this(new EmployeeDAO(), new LeaveBalanceDAOImpl(), new LeaveRequestDAOImpl(),
                DBUtil::getConnection);
    }

    public LeaveRequestServiceImpl(EmployeeDAO employeeDAO, LeaveBalanceDAO leaveBalanceDAO,
                                   LeaveRequestDAO leaveRequestDAO,
                                   ConnectionProvider connectionProvider) {
        this.employeeDAO = employeeDAO;
        this.leaveBalanceDAO = leaveBalanceDAO;
        this.leaveRequestDAO = leaveRequestDAO;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<PendingLeaveRequestDTO> getPendingRequestsForManager(long managerId) {
        if (managerId > Integer.MAX_VALUE || managerId < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Manager ID is outside the supported range.");
        }
        return leaveRequestDAO.findPendingByManagerId((int) managerId);
    }

    @Override
    public void approveLeave(long requestId, long managerId) {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            LeaveRequest request = findAuthorizedPendingRequest(requestId, managerId, connection);
            int workingDays = DateUtil.calculateWorkingDays(request.getStartDate(), request.getEndDate());
            LeaveBalance balance = leaveBalanceDAO.findByEmployeeIdForUpdate(
                    request.getEmployeeId(), connection);
            validateBalance(balance, request, workingDays);
            leaveBalanceDAO.deductBalance(request.getEmployeeId(), request.getLeaveType(),
                    workingDays, connection);
            leaveRequestDAO.updateStatus(requestId, LeaveStatus.APPROVED, connection);
            connection.commit();
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Database error during leave approval.", exception);
        } catch (RuntimeException exception) {
            rollback(connection);
            throw exception;
        } finally {
            close(connection);
        }
    }

    @Override
    public void rejectLeave(long requestId, long managerId) {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            findAuthorizedPendingRequest(requestId, managerId, connection);
            leaveRequestDAO.updateStatus(requestId, LeaveStatus.REJECTED, connection);
            connection.commit();
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Database error during leave rejection.", exception);
        } catch (RuntimeException exception) {
            rollback(connection);
            throw exception;
        } finally {
            close(connection);
        }
    }

    private LeaveRequest findAuthorizedPendingRequest(long requestId, long managerId,
                                                       Connection connection) {
        LeaveRequest request = leaveRequestDAO.findByIdForUpdate(requestId, connection);
        if (request == null) {
            throw new InvalidLeaveRequestException("Leave request was not found.");
        }
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveRequestException("Leave request is already "
                    + request.getStatus() + ".");
        }
        if (managerId > Integer.MAX_VALUE || managerId < Integer.MIN_VALUE) {
            throw new AuthorizationException("Manager identity is invalid.");
        }
        try {
            Optional<Employee> employee = employeeDAO.findById((int) request.getEmployeeId());
            if (employee.isEmpty() || employee.get().getManagerId() == null
                    || employee.get().getManagerId() != managerId) {
                throw new AuthorizationException("You are not the manager of this employee.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error while validating manager.", exception);
        }
        return request;
    }

    private void validateBalance(LeaveBalance balance, LeaveRequest request, int workingDays) {
        if (balance == null) {
            throw new InsufficientBalanceException("Leave balance was not found.");
        }
        double available = switch (request.getLeaveType()) {
            case CASUAL -> balance.getCasualBalance();
            case SICK -> balance.getSickBalance();
            case EARNED -> balance.getEarnedBalance();
        };
        if (available < workingDays) {
            throw new InsufficientBalanceException("Insufficient leave balance.");
        }
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
