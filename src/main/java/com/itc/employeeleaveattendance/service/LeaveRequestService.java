package com.itc.employeeleaveattendance.service;

import com.itc.employeeleaveattendance.dto.PendingLeaveRequestDTO;

import java.util.List;

public interface LeaveRequestService {

    List<PendingLeaveRequestDTO> getPendingRequestsForManager(long managerId);

    void approveLeave(long requestId, long managerId);

    void rejectLeave(long requestId, long managerId);
}