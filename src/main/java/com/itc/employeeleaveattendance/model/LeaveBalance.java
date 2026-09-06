package com.itc.employeeleaveattendance.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a row in the {@code LEAVE_BALANCE} table.
 * Stored and displayed on the employee dashboard.
 * Never stored in the session — loaded fresh per request.
 */
public class LeaveBalance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int    balanceId;
    private int    empId;
    private double casualBalance;
    private double sickBalance;
    private double earnedBalance;

    public LeaveBalance() {}

    public LeaveBalance(int balanceId, int empId,
                        double casualBalance, double sickBalance, double earnedBalance) {
        this.balanceId     = balanceId;
        this.empId         = empId;
        this.casualBalance = casualBalance;
        this.sickBalance   = sickBalance;
        this.earnedBalance = earnedBalance;
    }

    // --- Getters ---

    public int    getBalanceId()     { return balanceId; }
    public int    getEmpId()         { return empId; }
    public double getCasualBalance() { return casualBalance; }
    public double getSickBalance()   { return sickBalance; }
    public double getEarnedBalance() { return earnedBalance; }

    // --- Setters ---

    public void setBalanceId(int balanceId)         { this.balanceId = balanceId; }
    public void setEmpId(int empId)                 { this.empId = empId; }
    public void setCasualBalance(double v)          { this.casualBalance = v; }
    public void setSickBalance(double v)            { this.sickBalance = v; }
    public void setEarnedBalance(double v)          { this.earnedBalance = v; }
}
