package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.model.LeaveRequest;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestDAO {

    long save(LeaveRequest request);

    LeaveRequest findById(long id);

    LeaveRequest findByIdForUpdate(long id, Connection connection);

    List<LeaveRequest> findByEmployeeId(long employeeId);

    List<LeaveRequest> findOverlapping(long employeeId, LocalDate startDate, LocalDate endDate);

    void updateStatus(long id, LeaveStatus status);

    void updateStatus(long id, LeaveStatus status, Connection connection);

    List<PendingLeaveRequestDTO> findPendingByManagerId(int managerId);
}