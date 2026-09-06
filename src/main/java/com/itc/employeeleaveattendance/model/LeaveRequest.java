package com.itc.employeeleaveattendance.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Domain model for the {@code leave_requests} table.
 */
public class LeaveRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int       requestId;
    private int       empId;
    private String    leaveType;  // "Casual", "Sick", "Earned"
    private LocalDate startDate;
    private LocalDate endDate;
    private String    status;     // "PENDING", "APPROVED", "REJECTED"
    private String    reason;

    public LeaveRequest() {}

    public LeaveRequest(int requestId, int empId, String leaveType,
                        LocalDate startDate, LocalDate endDate,
                        String status, String reason) {
        this.requestId = requestId;
        this.empId     = empId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate   = endDate;
        this.status    = status;
        this.reason    = reason;
    }

    // --- Getters ---

    public int       getRequestId() { return requestId; }
    public int       getEmpId()     { return empId; }
    public String    getLeaveType() { return leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate()   { return endDate; }
    public String    getStatus()    { return status; }
    public String    getReason()    { return reason; }

    // --- Setters ---

    public void setRequestId(int requestId)     { this.requestId = requestId; }
    public void setEmpId(int empId)             { this.empId = empId; }
    public void setLeaveType(String leaveType)  { this.leaveType = leaveType; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate)   { this.endDate = endDate; }
    public void setStatus(String status)        { this.status = status; }
    public void setReason(String reason)        { this.reason = reason; }
}
