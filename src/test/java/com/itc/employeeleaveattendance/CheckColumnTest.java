package com.itc.employeeleaveattendance;

import org.junit.jupiter.api.Test;
import java.sql.*;

public class CheckColumnTest {
    @Test
    public void checkCol() throws Exception {
        String url = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
        String user = "SYSTEM";
        String pass = "Ritesh@12345";
        
        System.out.println("========== CHECK START ==========");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME, DATA_DEFAULT, IDENTITY_COLUMN FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'LEAVE_REQUEST'")) {
                while (rs.next()) {
                    System.out.println(rs.getString(1) + " | DEFAULT: " + rs.getString(2) + " | IDENTITY: " + rs.getString(3));
                }
            } catch (SQLException e) {
                System.out.println("Query Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Conn Error: " + e.getMessage());
        }
        System.out.println("========== CHECK END ==========");
    }
}
