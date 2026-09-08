package com.itc.employeeleaveattendance;

import org.junit.jupiter.api.Test;
import java.sql.*;

public class DescribeTableTest {
    @Test
    public void describeLeaveRequest() throws Exception {
        String url = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
        String user = "SYSTEM";
        String pass = "Ritesh@12345";
        
        System.out.println("========== DESCRIBE START ==========");
        System.out.println("Connecting...");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected!");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM LEAVE_REQUEST WHERE 1=0")) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                System.out.println("Columns in LEAVE_REQUEST:");
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println("- " + metaData.getColumnName(i) + " (" + metaData.getColumnTypeName(i) + ")");
                }
            } catch (SQLException e) {
                System.out.println("Query Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Conn Error: " + e.getMessage());
        }
        System.out.println("========== DESCRIBE END ==========");
    }
}
