package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.model.LeaveRequest;

import java.sql.Connection;
import java.util.List;

public interface LeaveRequestDao {

    LeaveRequest findById(int requestId, Connection connection);

    List<PendingLeaveRequestDTO> findPendingByManagerId(int managerId);

    void updateStatus(int requestId, String status, Connection connection);
}