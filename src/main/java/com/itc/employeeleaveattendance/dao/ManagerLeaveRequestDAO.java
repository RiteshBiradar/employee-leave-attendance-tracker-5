package com.itc.employeeleaveattendance.dao;

import com.itc.employeeleaveattendance.constant.LeaveStatus;
import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;
import com.itc.employeeleaveattendance.model.LeaveRequest;

import java.sql.Connection;
import java.util.List;

public interface ManagerLeaveRequestDAO {

    LeaveRequest findByIdForUpdate(long requestId, Connection connection);

    List<PendingLeaveRequestDTO> findPendingByManagerId(long managerId);

    void updateStatus(long requestId, LeaveStatus status, Connection connection);
}
