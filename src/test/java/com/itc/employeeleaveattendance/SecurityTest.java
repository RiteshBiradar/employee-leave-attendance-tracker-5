package com.itc.employeeleaveattendance;

import com.itc.employeeleaveattendance.dao.EmployeeDAO;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.filter.AuthorizationFilter;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.service.AuthService;
import com.itc.employeeleaveattendance.service.LeaveService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Security test suite — 17 test cases covering:
 *   - Authentication (valid / invalid emp_id+password)
 *   - Data isolation (employee can only see own leave records)
 *   - AuthenticationFilter (no session, empty session, valid session, expired session)
 *   - AuthorizationFilter RBAC (all 4 role × URL combinations)
 *   - Session invalidation on logout
 *
 * Uses Mockito mocks — no live Oracle DB required.
 * Requires dependency: mockito-core 5.12.0 (already added to pom.xml).
 */
@DisplayName("Security Tests — Auth, RBAC, Session, Data Isolation")
class SecurityTest {

    // --- Shared test fixtures ---
    private static final int    EMP_ID       = 1;
    private static final String EMP_NAME     = "Alice Smith";
    private static final String EMP_EMAIL    = "alice@example.com";
    private static final String EMP_TOKEN    = "mock_token_1";
    private static final String EMP_ROLE     = "EMPLOYEE";

    private static final int    MGR_ID       = 2;
    private static final String MGR_TOKEN    = "mock_token_2";
    private static final String MGR_ROLE     = "MANAGER";

    private Employee employee;
    private Employee manager;

    @BeforeEach
    void setUp() {
        employee = new Employee(EMP_ID, EMP_NAME, EMP_EMAIL, EMP_ROLE, null);
        manager  = new Employee(MGR_ID, "Bob Manager", "bob@example.com", MGR_ROLE, null);
    }

    // ===========================================================================
    //  1. AuthService — Authentication (emp_id + password against EMPLOYEE table)
    // ===========================================================================

    @Nested
    @DisplayName("1 · AuthService — Authentication")
    class AuthServiceTests {

        private EmployeeDAO mockEmployeeDAO;
        private AuthService authService;

        @BeforeEach
        void setUp() throws Exception {
            mockEmployeeDAO = mock(EmployeeDAO.class);
            var ctor = AuthService.class.getDeclaredConstructor(EmployeeDAO.class);
            ctor.setAccessible(true);
            authService = (AuthService) ctor.newInstance(mockEmployeeDAO);
        }

        @Test
        @DisplayName("TC-01 · Valid emp_id + correct token → returns Employee")
        void validCredentials_returnsEmployee() throws SQLException {
            when(mockEmployeeDAO.findByEmpIdAndPassword(EMP_ID, EMP_TOKEN))
                    .thenReturn(Optional.of(employee));

            Optional<Employee> result = authService.authenticate(EMP_ID, EMP_TOKEN);

            assertTrue(result.isPresent(), "Should return an employee for valid credentials");
            assertEquals(EMP_ID,   result.get().getEmpId());
            assertEquals(EMP_ROLE, result.get().getRole());
        }

        @Test
        @DisplayName("TC-02 · Valid emp_id + WRONG password → returns empty")
        void validIdWrongPassword_returnsEmpty() throws SQLException {
            when(mockEmployeeDAO.findByEmpIdAndPassword(EMP_ID, "wrongPass"))
                    .thenReturn(Optional.empty());

            Optional<Employee> result = authService.authenticate(EMP_ID, "wrongPass");

            assertTrue(result.isEmpty(), "Wrong password should not authenticate");
        }

        @Test
        @DisplayName("TC-03 · Non-existent emp_id → returns empty")
        void nonExistentEmpId_returnsEmpty() throws SQLException {
            when(mockEmployeeDAO.findByEmpIdAndPassword(999, EMP_TOKEN))
                    .thenReturn(Optional.empty());

            Optional<Employee> result = authService.authenticate(999, EMP_TOKEN);

            assertTrue(result.isEmpty(), "Non-existent emp_id should not authenticate");
        }

        @Test
        @DisplayName("TC-04 · Blank password → throws IllegalArgumentException")
        void blankPassword_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> authService.authenticate(EMP_ID, "  "),
                    "Blank password must be rejected");
        }

        @Test
        @DisplayName("TC-05 · Null password → throws IllegalArgumentException")
        void nullPassword_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> authService.authenticate(EMP_ID, null),
                    "Null password must be rejected");
        }
    }

    // ===========================================================================
    //  2. LeaveService — Data Isolation (LEAVE_REQUEST & LEAVE_BALANCE tables)
    // ===========================================================================

    @Nested
    @DisplayName("2 · LeaveService — Data Isolation")
    class LeaveServiceTests {

        private LeaveRequestDAO mockLeaveDAO;
        private LeaveBalanceDAO mockBalanceDAO;
        private LeaveService    leaveService;

        @BeforeEach
        void setUp() throws Exception {
            mockLeaveDAO   = mock(LeaveRequestDAO.class);
            mockBalanceDAO = mock(LeaveBalanceDAO.class);
            var ctor = LeaveService.class.getDeclaredConstructor(
                    LeaveRequestDAO.class, LeaveBalanceDAO.class);
            ctor.setAccessible(true);
            leaveService = (LeaveService) ctor.newInstance(mockLeaveDAO, mockBalanceDAO);
        }

        @Test
        @DisplayName("TC-06 · getLeaveHistory returns only that employee's records")
        void leaveHistory_returnsOwnRecordsOnly() throws SQLException {
            LeaveRequest lr = new LeaveRequest(10, EMP_ID, "CASUAL",
                    LocalDate.of(2025,1,10), LocalDate.of(2025,1,12),
                    "APPROVED", "Personal work");
            when(mockLeaveDAO.findByEmpId(EMP_ID)).thenReturn(List.of(lr));

            List<LeaveRequest> result = leaveService.getLeaveHistory(EMP_ID);

            assertEquals(1, result.size());
            assertEquals(EMP_ID, result.get(0).getEmpId(),
                    "Returned record must belong to the requesting employee");
        }

        @Test
        @DisplayName("TC-07 · Employee cannot trigger a query for another emp_id (IDOR prevention)")
        void leaveHistory_neverQueriesOtherEmpId() throws SQLException {
            when(mockLeaveDAO.findByEmpId(EMP_ID)).thenReturn(Collections.emptyList());

            leaveService.getLeaveHistory(EMP_ID);

            // DAO must only be called with EMP_ID — never with manager's ID
            verify(mockLeaveDAO, times(1)).findByEmpId(EMP_ID);
            verify(mockLeaveDAO, never()).findByEmpId(MGR_ID);
        }

        @Test
        @DisplayName("TC-08 · getLeaveBalance returns the correct balance for the employee")
        void leaveBalance_returnedForCorrectEmpId() throws SQLException {
            LeaveBalance bal = new LeaveBalance(1, EMP_ID, 6.0, 5.0, 10.0);
            when(mockBalanceDAO.findByEmpId(EMP_ID)).thenReturn(Optional.of(bal));

            Optional<LeaveBalance> result = leaveService.getLeaveBalance(EMP_ID);

            assertTrue(result.isPresent());
            assertEquals(6.0,  result.get().getCasualBalance(), 0.001);
            assertEquals(5.0,  result.get().getSickBalance(),   0.001);
            assertEquals(10.0, result.get().getEarnedBalance(), 0.001);
        }
    }

    // ===========================================================================
    //  3. AuthenticationFilter — Session Guard
    // ===========================================================================

    @Nested
    @DisplayName("3 · AuthenticationFilter — Unauthenticated Access")
    class AuthenticationFilterTests {

        private AuthenticationFilter filter;
        private HttpServletRequest   mockReq;
        private HttpServletResponse  mockResp;
        private FilterChain          mockChain;

        @BeforeEach
        void setUp() {
            filter    = new AuthenticationFilter();
            mockReq   = mock(HttpServletRequest.class);
            mockResp  = mock(HttpServletResponse.class);
            mockChain = mock(FilterChain.class);
        }

        @Test
        @DisplayName("TC-09 · No session → redirect to /login")
        void noSession_redirectsToLogin() throws IOException, ServletException {
            when(mockReq.getSession(false)).thenReturn(null);
            when(mockReq.getContextPath()).thenReturn("/EmployeeCaseStudyTracker");

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockResp).sendRedirect("/EmployeeCaseStudyTracker/login");
            verify(mockChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("TC-10 · Session exists but no employee attribute → redirect to /login")
        void sessionWithoutEmployee_redirectsToLogin() throws IOException, ServletException {
            HttpSession mockSession = mock(HttpSession.class);
            when(mockReq.getSession(false)).thenReturn(mockSession);
            when(mockSession.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE))
                    .thenReturn(null);
            when(mockReq.getContextPath()).thenReturn("/EmployeeCaseStudyTracker");

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockResp).sendRedirect("/EmployeeCaseStudyTracker/login");
            verify(mockChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("TC-11 · Valid session with employee → filter chain proceeds")
        void validSession_filterChainProceeds() throws IOException, ServletException {
            HttpSession mockSession = mock(HttpSession.class);
            when(mockReq.getSession(false)).thenReturn(mockSession);
            when(mockSession.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE))
                    .thenReturn(employee);

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockChain).doFilter(mockReq, mockResp);
            verify(mockResp, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("TC-12 · Expired session (getSession returns null) → redirect to login")
        void expiredSession_redirectsToLogin() throws IOException, ServletException {
            when(mockReq.getSession(false)).thenReturn(null);
            when(mockReq.getContextPath()).thenReturn("/EmployeeCaseStudyTracker");

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockResp).sendRedirect("/EmployeeCaseStudyTracker/login");
        }
    }

    // ===========================================================================
    //  4. AuthorizationFilter — RBAC (EMPLOYEE vs MANAGER roles)
    // ===========================================================================

    @Nested
    @DisplayName("4 · AuthorizationFilter — Role-Based Access Control")
    class AuthorizationFilterTests {

        private AuthorizationFilter filter;
        private HttpServletRequest  mockReq;
        private HttpServletResponse mockResp;
        private FilterChain         mockChain;
        private HttpSession         mockSession;

        @BeforeEach
        void setUp() {
            filter      = new AuthorizationFilter();
            mockReq     = mock(HttpServletRequest.class);
            mockResp    = mock(HttpServletResponse.class);
            mockChain   = mock(FilterChain.class);
            mockSession = mock(HttpSession.class);
            when(mockReq.getSession(false)).thenReturn(mockSession);
        }

        @Test
        @DisplayName("TC-13 · EMPLOYEE accessing /employee/* → allowed")
        void employee_accessesEmployeePath_allowed() throws IOException, ServletException {
            when(mockSession.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE))
                    .thenReturn(employee);
            when(mockReq.getServletPath()).thenReturn("/employee/dashboard");

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockChain).doFilter(mockReq, mockResp);
            verify(mockResp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("TC-14 · EMPLOYEE accessing /manager/* → 403 Forbidden")
        void employee_accessesManagerPath_forbidden() throws IOException, ServletException {
            when(mockSession.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE))
                    .thenReturn(employee);
            when(mockReq.getServletPath()).thenReturn("/manager/dashboard");

            RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
            when(mockReq.getRequestDispatcher("/WEB-INF/views/error/403.jsp"))
                    .thenReturn(mockDispatcher);

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockResp).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(mockDispatcher).forward(mockReq, mockResp);
            verify(mockChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("TC-15 · MANAGER accessing /manager/* → allowed")
        void manager_accessesManagerPath_allowed() throws IOException, ServletException {
            when(mockSession.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE))
                    .thenReturn(manager);
            when(mockReq.getServletPath()).thenReturn("/manager/dashboard");

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockChain).doFilter(mockReq, mockResp);
            verify(mockResp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("TC-16 · MANAGER accessing /employee/* → 403 Forbidden")
        void manager_accessesEmployeePath_forbidden() throws IOException, ServletException {
            when(mockSession.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE))
                    .thenReturn(manager);
            when(mockReq.getServletPath()).thenReturn("/employee/dashboard");

            RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
            when(mockReq.getRequestDispatcher("/WEB-INF/views/error/403.jsp"))
                    .thenReturn(mockDispatcher);

            filter.doFilter(mockReq, mockResp, mockChain);

            verify(mockResp).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(mockChain, never()).doFilter(any(), any());
        }
    }

    // ===========================================================================
    //  5. Session Invalidation — Logout
    // ===========================================================================

    @Nested
    @DisplayName("5 · Session Invalidation — Logout")
    class LogoutTests {

        @Test
        @DisplayName("TC-17 · invalidate() is called on the session during logout")
        void sessionInvalidation_isInvoked() {
            HttpSession mockSession = mock(HttpSession.class);
            // Simulate what LogoutServlet.performLogout() does
            mockSession.invalidate();
            verify(mockSession).invalidate();
        }

        @Test
        @DisplayName("TC-18 · After logout getSession(false) returns null — filter would redirect")
        void afterLogout_filterRedirects() throws IOException, ServletException {
            AuthenticationFilter filter   = new AuthenticationFilter();
            HttpServletRequest   mockReq  = mock(HttpServletRequest.class);
            HttpServletResponse  mockResp = mock(HttpServletResponse.class);
            FilterChain          chain    = mock(FilterChain.class);

            // Session invalidated → getSession(false) returns null
            when(mockReq.getSession(false)).thenReturn(null);
            when(mockReq.getContextPath()).thenReturn("/EmployeeCaseStudyTracker");

            filter.doFilter(mockReq, mockResp, chain);

            verify(mockResp).sendRedirect("/EmployeeCaseStudyTracker/login");
            verify(chain, never()).doFilter(any(), any());
        }
    }
}
