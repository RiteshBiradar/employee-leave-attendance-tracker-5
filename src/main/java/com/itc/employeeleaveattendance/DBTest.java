package com.itc.employeeleaveattendance;

import com.itc.employeeleaveattendance.util.DBUtil;

import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.service.AuthService;

import java.util.Optional;

public class DBTest {
    public static void main(String[] args) {
        AuthService authService = new AuthService();
        try {
            System.out.println("Testing authentication...");
            Optional<Employee> emp = authService.authenticate(1, "alice123");
            if (emp.isPresent()) {
                System.out.println("SUCCESS: Logged in as " + emp.get().getName());
            } else {
                System.out.println("FAILED: Invalid credentials.");
            }
        } catch (Exception e) {
            System.out.println("FAILED: Authentication threw an exception:");
            e.printStackTrace();
        }
    }
}