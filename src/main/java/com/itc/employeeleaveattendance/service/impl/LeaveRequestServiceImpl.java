package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.dao.EmployeeDao;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDao;
import com.itc.employeeleaveattendance.dao.LeaveRequestDao;
import com.itc.employeeleaveattendance.dao.impl.EmployeeDaoImpl;
import com.itc.employeeleaveattendance.dao.impl.LeaveBalanceDaoImpl;
import com.itc.employeeleaveattendance.dao.impl.LeaveRequestDaoImpl;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.exception.AuthorizationException;
import com.itc.employeeleaveattendance.exception.InvalidLeaveRequestException;
import com.itc.employeeleaveattendance.exception.LeaveBalanceException;
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

    private final EmployeeDao employeeDao;
    private final LeaveBalanceDao leaveBalanceDao;
    private final LeaveRequestDao leaveRequestDao;
    private final ConnectionProvider connectionProvider;

    public LeaveRequestServiceImpl() {
        this(new EmployeeDaoImpl(), new LeaveBalanceDaoImpl(), new LeaveRequestDaoImpl(),
                DBUtil::getConnection);
    }

    public LeaveRequestServiceImpl(EmployeeDao employeeDao, LeaveBalanceDao leaveBalanceDao,
                                   LeaveRequestDao leaveRequestDao) {
        this(employeeDao, leaveBalanceDao, leaveRequestDao, DBUtil::getConnection);
    }

    public LeaveRequestServiceImpl(EmployeeDao employeeDao, LeaveBalanceDao leaveBalanceDao,
                                   LeaveRequestDao leaveRequestDao,
                                   ConnectionProvider connectionProvider) {
        this.employeeDao = employeeDao;
        this.leaveBalanceDao = leaveBalanceDao;
        this.leaveRequestDao = leaveRequestDao;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<PendingLeaveRequestDTO> getPendingRequestsForManager(int managerId) {
        return leaveRequestDao.findPendingByManagerId(managerId);
    }

    @Override
    public void approveLeave(int requestId, int managerId) {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            LeaveRequest request = findAuthorizedPendingRequest(requestId, managerId, connection);
            int workingDays = countWorkingDays(request.getStartDate(), request.getEndDate());
            LeaveBalance balance = leaveBalanceDao.findByEmpId(request.getEmpId(), connection);
            if (balance == null) {
                throw new LeaveBalanceException("Leave balance was not found.");
            }
            validateBalance(balance, request.getLeaveType(), workingDays);
            leaveBalanceDao.deductBalance(request.getEmpId(), request.getLeaveType(), workingDays, connection);
            leaveRequestDao.updateStatus(requestId, "APPROVED", connection);
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
    public void rejectLeave(int requestId, int managerId) {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            findAuthorizedPendingRequest(requestId, managerId, connection);
            leaveRequestDao.updateStatus(requestId, "REJECTED", connection);
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

    private LeaveRequest findAuthorizedPendingRequest(int requestId, int managerId,
                                                       Connection connection) {
        LeaveRequest request = leaveRequestDao.findById(requestId, connection);
        if (request == null) {
            throw new InvalidLeaveRequestException("Leave request was not found.");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidLeaveRequestException("Leave request is already " + request.getStatus() + ".");
        }
        Employee employee = employeeDao.findById(request.getEmpId());
        if (employee == null || employee.getManagerId() == null
                || employee.getManagerId() != managerId) {
            throw new AuthorizationException("You are not the manager of this employee.");
        }
        return request;
    }

    private void validateBalance(LeaveBalance balance, String leaveType, int workingDays) {
        int available = switch (leaveType.toUpperCase()) {
            case "CASUAL" -> balance.getCasualBalance();
            case "SICK" -> balance.getSickBalance();
            case "EARNED" -> balance.getEarnedBalance();
            default -> throw new LeaveBalanceException("Unsupported leave type: " + leaveType);
        };
        if (available < workingDays) {
            throw new LeaveBalanceException("Insufficient leave balance.");
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