package com.itc.employeeleaveattendance.service.impl;

import com.itc.employeeleaveattendance.dao.EmployeeDao;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDao;
import com.itc.employeeleaveattendance.dao.LeaveRequestDao;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.exception.AuthorizationException;
import com.itc.employeeleaveattendance.exception.InvalidLeaveRequestException;
import com.itc.employeeleaveattendance.exception.LeaveBalanceException;
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
        request.setRequestId(7);
        request.setEmployeeName("Anita Desai");
        requestDao.pendingRequests = List.of(request);
        LeaveRequestServiceImpl service = serviceFor(requestDao, new FakeBalanceDao(), employeeWithManager(3, 1),
                new FakeConnection());

        List<PendingLeaveRequestDTO> result = service.getPendingRequestsForManager(1);

        assertSame(requestDao.pendingRequests, result);
        assertEquals(7, result.get(0).getRequestId());
    }

    @Test
    void approvesDirectReportAndDeductsWorkingDaysInTransaction() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "CASUAL", "PENDING", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 9));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3, 5, 0, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao, employeeWithManager(3, 1), connection);

        service.approveLeave(10, 1);

        assertEquals("APPROVED", requestDao.updatedStatus);
        assertEquals(3, balanceDao.deductedDays);
        assertEquals("CASUAL", balanceDao.deductedLeaveType);
        assertTrue(connection.committed);
        assertFalse(connection.rolledBack);
    }

    @Test
    void rejectsDirectReportWithoutDeductingBalance() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "SICK", "PENDING", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 8));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3, 0, 5, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao, employeeWithManager(3, 1), connection);

        service.rejectLeave(10, 1);

        assertEquals("REJECTED", requestDao.updatedStatus);
        assertEquals(0, balanceDao.deductedDays);
        assertTrue(connection.committed);
        assertFalse(connection.rolledBack);
    }

    @Test
    void rejectsApprovalForNonDirectReport() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "CASUAL", "PENDING", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 7));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3, 5, 0, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao, employeeWithManager(3, 99), connection);

        assertThrows(AuthorizationException.class, () -> service.approveLeave(10, 1));

        assertEquals(0, balanceDao.deductedDays);
        assertEquals(null, requestDao.updatedStatus);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rejectsRejectionForNonDirectReport() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "CASUAL", "PENDING", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 7));
        LeaveRequestServiceImpl service = serviceFor(requestDao, new FakeBalanceDao(), employeeWithManager(3, 99),
                connection);

        assertThrows(AuthorizationException.class, () -> service.rejectLeave(10, 1));

        assertEquals(null, requestDao.updatedStatus);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rejectsApprovalWhenBalanceIsInsufficient() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "EARNED", "PENDING", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 9));
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3, 0, 0, 2);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao, employeeWithManager(3, 1), connection);

        assertThrows(LeaveBalanceException.class, () -> service.approveLeave(10, 1));

        assertEquals(0, balanceDao.deductedDays);
        assertEquals(null, requestDao.updatedStatus);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rejectsApprovalForAlreadyProcessedRequest() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "CASUAL", "APPROVED", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 7));
        LeaveRequestServiceImpl service = serviceFor(requestDao, new FakeBalanceDao(), employeeWithManager(3, 1),
                connection);

        assertThrows(InvalidLeaveRequestException.class, () -> service.approveLeave(10, 1));

        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    @Test
    void rollsBackWhenStatusUpdateFails() {
        FakeConnection connection = new FakeConnection();
        FakeRequestDao requestDao = new FakeRequestDao();
        requestDao.request = leaveRequest(3, "CASUAL", "PENDING", LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 7));
        requestDao.failStatusUpdate = true;
        FakeBalanceDao balanceDao = new FakeBalanceDao();
        balanceDao.balance = balance(3, 5, 0, 0);
        LeaveRequestServiceImpl service = serviceFor(requestDao, balanceDao, employeeWithManager(3, 1), connection);

        assertThrows(RuntimeException.class, () -> service.approveLeave(10, 1));

        assertEquals(1, balanceDao.deductedDays);
        assertTrue(connection.rolledBack);
        assertFalse(connection.committed);
    }

    private LeaveRequestServiceImpl serviceFor(FakeRequestDao requestDao, FakeBalanceDao balanceDao,
                                                Employee employee, FakeConnection connection) {
        EmployeeDao employeeDao = empId -> employee;
        return new LeaveRequestServiceImpl(employeeDao, balanceDao, requestDao, () -> connection.connection());
    }

    private Employee employeeWithManager(int empId, int managerId) {
        Employee employee = new Employee();
        employee.setEmpId(empId);
        employee.setManagerId(managerId);
        return employee;
    }

    private LeaveRequest leaveRequest(int empId, String leaveType, String status,
                                      LocalDate startDate, LocalDate endDate) {
        LeaveRequest request = new LeaveRequest();
        request.setEmpId(empId);
        request.setLeaveType(leaveType);
        request.setStatus(status);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }

    private LeaveBalance balance(int empId, int casual, int sick, int earned) {
        LeaveBalance balance = new LeaveBalance();
        balance.setEmpId(empId);
        balance.setCasualBalance(casual);
        balance.setSickBalance(sick);
        balance.setEarnedBalance(earned);
        return balance;
    }

    private static class FakeRequestDao implements LeaveRequestDao {
        private LeaveRequest request;
        private List<PendingLeaveRequestDTO> pendingRequests = List.of();
        private String updatedStatus;
        private boolean failStatusUpdate;

        @Override
        public LeaveRequest findById(int requestId, Connection connection) {
            return request;
        }

        @Override
        public List<PendingLeaveRequestDTO> findPendingByManagerId(int managerId) {
            return pendingRequests;
        }

        @Override
        public void updateStatus(int requestId, String status, Connection connection) {
            if (failStatusUpdate) {
                throw new RuntimeException("Simulated status update failure");
            }
            updatedStatus = status;
        }
    }

    private static class FakeBalanceDao implements LeaveBalanceDao {
        private LeaveBalance balance;
        private int deductedDays;
        private String deductedLeaveType;

        @Override
        public LeaveBalance findByEmpId(int empId, Connection connection) {
            return balance;
        }

        @Override
        public void deductBalance(int empId, String leaveType, int workingDays, Connection connection) {
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
                    case "close" -> {
                    }
                    case "setAutoCommit" -> {
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
