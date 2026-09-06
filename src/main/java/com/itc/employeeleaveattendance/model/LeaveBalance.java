package com.itc.employeeleaveattendance.model;

public class LeaveBalance {

    private long balanceId;
    private long employeeId;

    private double casualBalance;
    private double sickBalance;
    private double earnedBalance;

    public LeaveBalance() {
    }

    public LeaveBalance(long balanceId,
                        long employeeId,
                        double casualBalance,
                        double sickBalance,
                        double earnedBalance) {

        this.balanceId = balanceId;
        this.employeeId = employeeId;
        this.casualBalance = casualBalance;
        this.sickBalance = sickBalance;
        this.earnedBalance = earnedBalance;
    }

    public long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(long balanceId) {
        this.balanceId = balanceId;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public double getCasualBalance() {
        return casualBalance;
    }

    public void setCasualBalance(double casualBalance) {
        this.casualBalance = casualBalance;
    }

    public double getSickBalance() {
        return sickBalance;
    }

    public void setSickBalance(double sickBalance) {
        this.sickBalance = sickBalance;
    }

    public double getEarnedBalance() {
        return earnedBalance;
    }

    public void setEarnedBalance(double earnedBalance) {
        this.earnedBalance = earnedBalance;
    }
}