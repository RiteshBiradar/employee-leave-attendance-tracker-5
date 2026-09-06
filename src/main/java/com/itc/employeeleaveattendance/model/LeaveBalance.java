package com.itc.employeeleaveattendance.model;

public class LeaveBalance {

    private int empId;
    private int casualBalance;
    private int sickBalance;
    private int earnedBalance;

    public LeaveBalance() {
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public int getCasualBalance() {
        return casualBalance;
    }

    public void setCasualBalance(int casualBalance) {
        this.casualBalance = casualBalance;
    }

    public int getSickBalance() {
        return sickBalance;
    }

    public void setSickBalance(int sickBalance) {
        this.sickBalance = sickBalance;
    }

    public int getEarnedBalance() {
        return earnedBalance;
    }

    public void setEarnedBalance(int earnedBalance) {
        this.earnedBalance = earnedBalance;
    }
}