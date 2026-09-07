package com.itc.employeeleaveattendance;

import org.junit.jupiter.api.Test;
import java.sql.*;

public class CheckSequenceTest {
    @Test
    public void testSequences() throws Exception {
        String url = "jdbc:oracle:thin:@192.168.1.45:1521/FREEPDB1";
        String user = "SYSTEM";
        String pass = "Ritesh@12345";
        
        System.out.println("========== CHECK START ==========");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SEQUENCE_NAME FROM USER_SEQUENCES")) {
                System.out.println("Sequences:");
                while (rs.next()) {
                    System.out.println("- " + rs.getString(1));
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
