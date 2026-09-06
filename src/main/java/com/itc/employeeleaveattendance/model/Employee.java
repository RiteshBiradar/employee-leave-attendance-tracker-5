package com.itc.employeeleaveattendance.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Domain model for the {@code employees} table.
 * Stored in the HTTP session after successful authentication.
 */
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int    empId;
    private String name;
    private String email;
    private String role;       // "EMPLOYEE" or "MANAGER"
    private Integer managerId; // nullable

    public Employee() {}

    public Employee(int empId, String name, String email, String role, Integer managerId) {
        this.empId     = empId;
        this.name      = name;
        this.email     = email;
        this.role      = role;
        this.managerId = managerId;
    }

    // --- Getters ---

    public int getEmpId()         { return empId; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getRole()       { return role; }
    public Integer getManagerId() { return managerId; }

    // --- Setters ---

    public void setEmpId(int empId)           { this.empId = empId; }
    public void setName(String name)          { this.name = name; }
    public void setEmail(String email)        { this.email = email; }
    public void setRole(String role)          { this.role = role; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    @Override
    public String toString() {
        return "Employee{empId=" + empId + ", name='" + name + "', role='" + role + "'}";
    }
}
