package com.worksphere.util;

import java.sql.*;

public class DataInspector {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Inspecting Task Data");
            System.out.println("===================\n");
            
            // Check completed tasks
            String query = "SELECT COUNT(*) as total, " +
                          "SUM(CASE WHEN actual_minutes > 0 THEN 1 ELSE 0 END) as with_actual, " +
                          "AVG(CASE WHEN actual_minutes > 0 THEN estimated_minutes END) as avg_est, " +
                          "AVG(CASE WHEN actual_minutes > 0 THEN actual_minutes END) as avg_actual, " +
                          "SUM(CASE WHEN actual_minutes > 0 THEN estimated_minutes ELSE 0 END) as total_est, " +
                          "SUM(CASE WHEN actual_minutes > 0 THEN actual_minutes ELSE 0 END) as total_actual " +
                          "FROM tasks WHERE status = 'COMPLETED'";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int withActual = rs.getInt("with_actual");
                    double avgEst = rs.getDouble("avg_est");
                    double avgActual = rs.getDouble("avg_actual");
                    int totalEst = rs.getInt("total_est");
                    int totalActual = rs.getInt("total_actual");
                    
                    System.out.println("Completed Tasks: " + total);
                    System.out.println("Tasks with actual_minutes > 0: " + withActual);
                    System.out.println("Average estimated minutes: " + String.format("%.2f", avgEst));
                    System.out.println("Average actual minutes: " + String.format("%.2f", avgActual));
                    System.out.println("Total estimated minutes: " + totalEst);
                    System.out.println("Total actual minutes: " + totalActual);
                    
                    if (totalActual > 0) {
                        double efficiency = (double) totalEst / totalActual;
                        System.out.println("\nTime Efficiency: " + String.format("%.2f%%", efficiency * 100));
                    }
                }
            }
            
            // Show a few sample tasks
            System.out.println("\n\nSample Tasks (first 5 completed):");
            System.out.println("==================================");
            String sampleQuery = "SELECT title, estimated_minutes, actual_minutes, created_at, completed_at " +
                               "FROM tasks WHERE status = 'COMPLETED' LIMIT 5";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sampleQuery)) {
                while (rs.next()) {
                    System.out.println("\nTitle: " + rs.getString("title"));
                    System.out.println("  Estimated: " + rs.getInt("estimated_minutes") + " min");
                    System.out.println("  Actual: " + rs.getInt("actual_minutes") + " min");
                    System.out.println("  Created: " + rs.getString("created_at"));
                    System.out.println("  Completed: " + rs.getString("completed_at"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error inspecting data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
