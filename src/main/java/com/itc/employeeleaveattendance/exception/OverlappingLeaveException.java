package com.itc.employeeleaveattendance.exception;

/**
 * Thrown by {@code LeaveServiceImpl} when the requested leave period overlaps
 * with an existing PENDING or APPROVED leave request for the same employee.
 */
public class OverlappingLeaveException extends RuntimeException {

    public OverlappingLeaveException(String message) {
        super(message);
    }
}
