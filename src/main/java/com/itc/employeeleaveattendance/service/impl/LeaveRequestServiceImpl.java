package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dao.ManagerEmployeeLookup;
import com.itc.employeeleaveattendance.dao.ManagerEmployeeLookupImpl;
import com.itc.employeeleaveattendance.dao.ManagerLeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.ManagerLeaveBalanceDAOImpl;
import com.itc.employeeleaveattendance.dao.ManagerLeaveRequestDAO;
import com.itc.employeeleaveattendance.dao.ManagerLeaveRequestDAOImpl;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.exception.AuthorizationException;
import com.itc.employeeleaveattendance.exception.InvalidLeaveRequestException;
import com.itc.employeeleaveattendance.exception.LeaveBalanceException;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.service.LeaveRequestService;
import com.itc.employeeleaveattendance.service.ConnectionProvider;
import com.itc.employeeleaveattendance.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final ManagerEmployeeLookup employeeLookup;
    private final ManagerLeaveBalanceDAO leaveBalanceDao;
    private final ManagerLeaveRequestDAO leaveRequestDao;
    private final ConnectionProvider connectionProvider;

    public LeaveRequestServiceImpl() {
        this(new ManagerEmployeeLookupImpl(), new ManagerLeaveBalanceDAOImpl(), new ManagerLeaveRequestDAOImpl(),
                DBUtil::getConnection);
    }

    public LeaveRequestServiceImpl(ManagerEmployeeLookup employeeLookup,
                                   ManagerLeaveBalanceDAO leaveBalanceDao,
                                   ManagerLeaveRequestDAO leaveRequestDao) {
        this(employeeLookup, leaveBalanceDao, leaveRequestDao, DBUtil::getConnection);
    }

    public LeaveRequestServiceImpl(ManagerEmployeeLookup employeeLookup,
                                   ManagerLeaveBalanceDAO leaveBalanceDao,
                                   ManagerLeaveRequestDAO leaveRequestDao,
                                   ConnectionProvider connectionProvider) {
        this.employeeLookup = employeeLookup;
        this.leaveBalanceDao = leaveBalanceDao;
        this.leaveRequestDao = leaveRequestDao;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<PendingLeaveRequestDTO> getPendingRequestsForManager(long managerId) {
        return leaveRequestDao.findPendingByManagerId(managerId);
    }

    @Override
    public void approveLeave(long requestId, long managerId) {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            LeaveRequest request = findAuthorizedPendingRequest(requestId, managerId, connection);
            int workingDays = countWorkingDays(request.getStartDate(), request.getEndDate());
            LeaveBalance balance = leaveBalanceDao.findByEmployeeIdForUpdate(request.getEmployeeId(), connection);
            if (balance == null) {
                throw new LeaveBalanceException("Leave balance was not found.");
            }
            validateBalance(balance, request.getLeaveType(), workingDays);
            leaveBalanceDao.deductBalance(request.getEmployeeId(), request.getLeaveType().name(), workingDays, connection);
            leaveRequestDao.updateStatus(requestId, LeaveStatus.APPROVED, connection);
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
            leaveRequestDao.updateStatus(requestId, LeaveStatus.REJECTED, connection);
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
        LeaveRequest request = leaveRequestDao.findByIdForUpdate(requestId, connection);
        if (request == null) {
            throw new InvalidLeaveRequestException("Leave request was not found.");
        }
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveRequestException("Leave request is already " + request.getStatus() + ".");
        }
        Employee employee = employeeLookup.findById(request.getEmployeeId());
        if (employee == null || employee.getManagerId() == null
                || employee.getManagerId() != managerId) {
            throw new AuthorizationException("You are not the manager of this employee.");
        }
        return request;
    }

    private void validateBalance(LeaveBalance balance, LeaveType leaveType, int workingDays) {
        double available = switch (leaveType) {
            case CASUAL -> balance.getCasualBalance();
            case SICK -> balance.getSickBalance();
            case EARNED -> balance.getEarnedBalance();
        };
        if (available < workingDays) {
            throw new InsufficientBalanceException("Insufficient leave balance.");
        }
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