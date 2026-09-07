package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dao.ManagerEmployeeLookup;
import com.itc.employeeleaveattendance.dao.ManagerLeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.ManagerLeaveRequestDAO;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.exception.AuthorizationException;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.exception.InvalidLeaveRequestException;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveRequestServiceImplTest {

    @Test
    void retrievesPendingRequestsForManager() {
        FakeRequestDao requestDao = new FakeRequestDao();
        PendingLeaveRequestDTO request = new PendingLeaveRequestDTO();
        request.setRequestId(7L);
        request.setEmployeeName("Anita Desai");
        requestDao.pendingRequests = List.of(request);
        LeaveRequestServiceImpl service = serviceFor(requestDao, new FakeBalanceDao(),
                employeeWithManager(3L, 1L), new FakeConnection());

        List<PendingLeaveRequestDTO> result = service.getPendingRequestsForManager(1L);

        assertSame(requestDao.pendingRequests, result);
        assertEquals(7L, result.get(0).getRequestId());
    }

    @Test
    void approvesDirectReportAndDeductsWorkingDaysInTransaction() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.CASUAL, LeaveStatus.PENDING,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 9));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3L, 5, 0, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao,
                employeeWithManager(3L, 1L), connection);

        service.approveLeave(10L, 1L);

        assertEquals(LeaveStatus.APPROVED, requestDao.updatedStatus);
        assertEquals(3, balanceDao.deductedDays);
        assertEquals("CASUAL", balanceDao.deductedLeaveType);
        assertTrue(connection.committed);
        assertFalse(connection.rolledBack);
    }

    @Test
    void rejectsDirectReportWithoutDeductingBalance() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.SICK, LeaveStatus.PENDING,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 8));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3L, 0, 5, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao,
                employeeWithManager(3L, 1L), connection);

        service.rejectLeave(10L, 1L);

        assertEquals(LeaveStatus.REJECTED, requestDao.updatedStatus);
        assertEquals(0, balanceDao.deductedDays);
        assertTrue(connection.committed);
        assertFalse(connection.rolledBack);
    }

    @Test
    void rejectsApprovalForNonDirectReport() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.CASUAL, LeaveStatus.PENDING,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 7));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3L, 5, 0, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao,
                employeeWithManager(3L, 99L), connection);

        assertThrows(AuthorizationException.class, () -> service.approveLeave(10L, 1L));

        assertEquals(0, balanceDao.deductedDays);
        assertEquals(null, requestDao.updatedStatus);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rejectsRejectionForNonDirectReport() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.CASUAL, LeaveStatus.PENDING,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 7));
        LeaveRequestServiceImpl service = serviceFor(requestDao, new FakeBalanceDao(),
                employeeWithManager(3L, 99L), connection);

        assertThrows(AuthorizationException.class, () -> service.rejectLeave(10L, 1L));

        assertEquals(null, requestDao.updatedStatus);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rejectsApprovalWhenBalanceIsInsufficient() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.EARNED, LeaveStatus.PENDING,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 9));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3L, 0, 0, 2);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao,
                employeeWithManager(3L, 1L), connection);

        assertThrows(InsufficientBalanceException.class, () -> service.approveLeave(10L, 1L));

        assertEquals(0, balanceDao.deductedDays);
        assertEquals(null, requestDao.updatedStatus);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rejectsApprovalForAlreadyProcessedRequest() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.CASUAL, LeaveStatus.APPROVED,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 7));
        LeaveRequestServiceImpl service = serviceFor(requestDao, new FakeBalanceDao(),
                employeeWithManager(3L, 1L), connection);

        assertThrows(InvalidLeaveRequestException.class, () -> service.approveLeave(10L, 1L));

        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rollsBackWhenStatusUpdateFails() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3L, LeaveType.CASUAL, LeaveStatus.PENDING,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 7));
        requestDao.failStatusUpdate = true;
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3L, 5, 0, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao,
                employeeWithManager(3L, 1L), connection);

        assertThrows(RuntimeException.class, () -> service.approveLeave(10L, 1L));

        assertEquals(1, balanceDao.deductedDays);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    private LeaveRequestServiceImpl serviceFor(FakeRequestDao requestDao, FakeBalanceDao balanceDao,
                                                Employee employee, FakeConnection connection) {
        ManagerEmployeeLookup employeeLookup = employeeId -> employee;
        return new LeaveRequestServiceImpl(employeeLookup, balanceDao, requestDao,
                () -> connection.connection());
    }

    private Employee employeeWithManager(long empId, long managerId) {
        Employee employee = new Employee();
        employee.setEmpId((int) empId);
        employee.setManagerId((int) managerId);
        return employee;
    }

    private LeaveRequest leaveRequest(long employeeId, LeaveType leaveType, LeaveStatus status,
                                      LocalDate startDate, LocalDate endDate) {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setLeaveType(leaveType);
        request.setStatus(status);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }

    private LeaveBalance balance(long employeeId, double casual, double sick, double earned) {
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployeeId(employeeId);
        balance.setCasualBalance(casual);
        balance.setSickBalance(sick);
        balance.setEarnedBalance(earned);
        return balance;
    }

    private static class FakeRequestDao implements ManagerLeaveRequestDAO {
        private LeaveRequest request;
        private List<PendingLeaveRequestDTO> pendingRequests = List.of();
        private LeaveStatus updatedStatus;
        private boolean failStatusUpdate;

        @Override
        public LeaveRequest findByIdForUpdate(long requestId, Connection connection) {
            return request;
        }

        @Override
        public List<PendingLeaveRequestDTO> findPendingByManagerId(long managerId) {
            return pendingRequests;
        }

        @Override
        public void updateStatus(long requestId, LeaveStatus status, Connection connection) {
            if (failStatusUpdate) {
                throw new RuntimeException("Simulated status update failure");
            }
            updatedStatus = status;
        }
    }

    private static class FakeBalanceDao implements ManagerLeaveBalanceDAO {
        private LeaveBalance balance;
        private int deductedDays;
        private String deductedLeaveType;

        @Override
        public LeaveBalance findByEmployeeIdForUpdate(long employeeId, Connection connection) {
            return balance;
        }

        @Override
        public void deductBalance(long employeeId, String leaveType, int workingDays, Connection connection) {
            deductedDays += workingDays;
            deductedLeaveType = leaveType;
        }
    }

    private static class FakeConnection {
        private boolean committed;
        private boolean rolledBack;

        private Connection connection() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "commit" -> committed = true;
                    case "rollback" -> rolledBack = true;
                    case "close", "setAutoCommit" -> {
                    }
                    default -> {
                        if (method.getReturnType() == boolean.class) {
                            return false;
                        }
                        if (method.getReturnType() == int.class) {
                            return 0;
                        }
                        if (method.getReturnType() == long.class) {
                            return 0L;
                        }
                    }
                }
                return null;
            };
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, handler);
        }
    }
}
