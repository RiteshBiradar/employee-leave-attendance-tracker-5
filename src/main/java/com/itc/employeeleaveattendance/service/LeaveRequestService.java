package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;

import java.util.List;

public interface LeaveRequestService {

    List<PendingLeaveRequestDTO> getPendingRequestsForManager(int managerId);

    void approveLeave(int requestId, int managerId);

    void rejectLeave(int requestId, int managerId);
}