package com.itc.employeeleaveattendance.exception;

/**
 * Thrown by {@code LeaveServiceImpl} when an employee's available leave balance
 * is insufficient to cover the requested number of working days.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
