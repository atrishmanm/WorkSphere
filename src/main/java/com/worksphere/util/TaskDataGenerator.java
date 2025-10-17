package com.worksphere.util;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility to populate the database with realistic task data
 * for testing the leaderboard feature.
 */
public class TaskDataGenerator {
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final String[] TASK_TITLES = {
        "Backend API Development", "Database Schema Update", "Code Review - Feature X",
        "Unit Test Coverage", "Security Audit", "Documentation Update",
        "Frontend Components", "CSS Styling", "Bug Fix - Login Issue",
        "Performance Optimization", "Integration Testing", "Deploy to Staging",
        "Database Backup", "Server Monitoring", "Log Analysis",
        "Infrastructure Update", "CI/CD Pipeline", "Network Security",
        "API Refactoring", "User Dashboard", "Mobile Responsiveness",
        "Dark Mode", "Load Balancing", "Disaster Recovery",
        "Email Templates", "Push Notifications", "Accessibility",
        "Internationalization", "Cache Implementation", "Performance Metrics",
        "Search Feature", "Export Functionality", "Animations",
        "Icon Library", "SSL Certificates", "Database Optimization",
        "User Feedback", "Analytics Report", "Rate Limiting",
        "Error Handling", "Kubernetes Setup", "Auto-scaling",
        "Webhook Integration", "Payment Gateway", "User Onboarding",
        "Help Center", "Compliance Audit", "Data Encryption",
        "Microservices", "GraphQL API", "Progressive Web App",
        "Offline Mode", "Message Queue", "Background Jobs",
        "Real-time Updates", "Chat Feature", "Custom Themes",
        "Keyboard Shortcuts", "Container Registry", "Image Optimization",
        "Social Login", "Two-Factor Auth", "File Upload",
        "Image Gallery", "Cost Optimization", "Resource Tagging"
    };
    
    private static final String[] DESCRIPTIONS = {
        "Implement REST endpoints for user management",
        "Add new columns for analytics tracking",
        "Review pull requests from team",
        "Increase test coverage to 80%",
        "Review authentication flow",
        "Update API documentation",
        "Build reusable UI components",
        "Implement responsive design",
        "Fix authentication bug",
        "Optimize page load time"
    };
    
    private static final String[] PRIORITIES = {"LOW", "MEDIUM", "HIGH", "URGENT"};
    
    public static void main(String[] args) {
        String dbPath = System.getProperty("user.home") + "/.worksphere/worksphere.db";
        String url = "jdbc:sqlite:" + dbPath;
        
        System.out.println("WorkSphere Task Data Generator");
        System.out.println("==============================");
        System.out.println("Database: " + dbPath);
        System.out.println();
        
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ SQLite JDBC driver not found: " + e.getMessage());
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("✓ Connected to database");
            
            // Clear existing data
            clearExistingData(conn);
            System.out.println("✓ Cleared existing task data");
            
            // Generate tasks from Oct 15 to Nov 20
            LocalDate startDate = LocalDate.of(2025, 10, 15);
            LocalDate endDate = LocalDate.of(2025, 11, 20);
            
            int totalTasks = generateTasks(conn, startDate, endDate);
            
            System.out.println();
            System.out.println("✓ Successfully generated " + totalTasks + " completed tasks");
            System.out.println();
            
            // Show summary
            showSummary(conn);
            
            System.out.println();
            System.out.println("Please restart WorkSphere to see the updated leaderboard data.");
            
        } catch (SQLException e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void clearExistingData(Connection conn) throws SQLException {
        String[] queries = {
            "DELETE FROM subtasks",
            "DELETE FROM task_history",
            "DELETE FROM tasks",
            "DELETE FROM sqlite_sequence WHERE name='tasks'",
            "DELETE FROM sqlite_sequence WHERE name='subtasks'",
            "DELETE FROM sqlite_sequence WHERE name='task_history'"
        };
        
        for (String query : queries) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(query);
            }
        }
    }
    
    private static int generateTasks(Connection conn, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        
        int[] userIds = {2, 3, 4}; // jane_smith, john_doe, mike_wilson
        Random random = new Random();
        int taskIndex = 0;
        int totalTasks = 0;
        
        String insertSQL = "INSERT INTO tasks (title, description, priority, status, due_date, " +
                "assigned_to, created_by, created_at, updated_at, completed_at, " +
                "estimated_minutes, actual_minutes, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            LocalDate currentDate = startDate;
            
            while (!currentDate.isAfter(endDate)) {
                // Skip weekends
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    
                    // Each user gets 2 tasks per weekday
                    for (int userId : userIds) {
                        for (int taskNum = 0; taskNum < 2; taskNum++) {
                            String title = TASK_TITLES[taskIndex % TASK_TITLES.length];
                            String description = DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)];
                            String priority = PRIORITIES[random.nextInt(PRIORITIES.length)];
                            
                            // Determine task status - 60% completed, 20% in progress, 20% todo
                            int statusRoll = random.nextInt(100);
                            String status;
                            LocalDateTime completedAt = null;
                            Integer actualMinutes = null;
                            LocalDate createdDate;
                            
                            if (statusRoll < 60) {
                                // COMPLETED tasks - created 1-5 days before currentDate
                                status = "COMPLETED";
                                int daysAgo = 1 + random.nextInt(5); // 1-5 days ago
                                createdDate = currentDate.minusDays(daysAgo);
                                
                                // Skip if created date is before start date
                                if (createdDate.isBefore(startDate)) {
                                    createdDate = startDate;
                                }
                                
                                // Completed on currentDate
                                int endHour = 14 + random.nextInt(4); // 2-5 PM
                                completedAt = currentDate.atTime(endHour, random.nextInt(60));
                                
                                // Actual minutes will be set later based on estimated minutes
                                // We'll add realistic variance (80% - 120% of estimate)
                            } else if (statusRoll < 80) {
                                // IN_PROGRESS tasks - created 1-3 days ago
                                status = "IN_PROGRESS";
                                int daysAgo = 1 + random.nextInt(3); // 1-3 days ago
                                createdDate = currentDate.minusDays(daysAgo);
                                if (createdDate.isBefore(startDate)) {
                                    createdDate = startDate;
                                }
                            } else {
                                // TODO tasks - created recently (0-2 days ago)
                                status = "TODO";
                                int daysAgo = random.nextInt(3); // 0-2 days ago
                                createdDate = currentDate.minusDays(daysAgo);
                                if (createdDate.isBefore(startDate)) {
                                    createdDate = startDate;
                                }
                            }
                            
                            // Random creation time during the workday
                            int startHour = 8 + random.nextInt(2); // 8-9 AM
                            LocalDateTime createdAt = createdDate.atTime(startHour, 0);
                            
                            int estimatedMinutes = 240 + random.nextInt(480); // 4-12 hours (1-3 days of work)
                            
                            // For completed tasks, set actual time with realistic variance
                            if (status.equals("COMPLETED")) {
                                // Actual time varies from 80% to 120% of estimate
                                // This gives us time efficiency around 80% - 120% (realistic range)
                                double variance = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
                                actualMinutes = (int) (estimatedMinutes * variance);
                            }
                            
                            int categoryId = (random.nextInt(6)) + 1; // 1-6
                            
                            pstmt.setString(1, title);
                            pstmt.setString(2, description);
                            pstmt.setString(3, priority);
                            pstmt.setString(4, status);
                            pstmt.setString(5, currentDate.toString());
                            pstmt.setInt(6, userId);
                            pstmt.setInt(7, 1); // created_by admin
                            pstmt.setString(8, createdAt.format(DATETIME_FORMATTER));
                            pstmt.setString(9, createdAt.format(DATETIME_FORMATTER)); // updated_at = created_at initially
                            
                            // Set completed_at (null for non-completed tasks)
                            if (completedAt != null) {
                                pstmt.setString(10, completedAt.format(DATETIME_FORMATTER));
                            } else {
                                pstmt.setNull(10, java.sql.Types.VARCHAR);
                            }
                            
                            pstmt.setInt(11, estimatedMinutes);
                            
                            // Set actual_minutes (null for non-completed tasks)
                            if (actualMinutes != null) {
                                pstmt.setInt(12, actualMinutes);
                            } else {
                                pstmt.setNull(12, java.sql.Types.INTEGER);
                            }
                            
                            pstmt.setInt(13, categoryId);
                            
                            pstmt.executeUpdate();
                            
                            taskIndex++;
                            totalTasks++;
                        }
                    }
                    
                    System.out.print(".");
                    if (totalTasks % 30 == 0) {
                        System.out.println(" " + totalTasks + " tasks");
                    }
                }
                
                currentDate = currentDate.plusDays(1);
            }
        }
        
        return totalTasks;
    }
    
    private static void showSummary(Connection conn) throws SQLException {
        System.out.println("Task Summary:");
        System.out.println("-------------");
        
        // Total tasks
        String totalQuery = "SELECT COUNT(*) as total FROM tasks";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(totalQuery)) {
            if (rs.next()) {
                System.out.println("Total tasks: " + rs.getInt("total"));
            }
        }
        
        // By status
        String statusQuery = "SELECT status, COUNT(*) as count FROM tasks GROUP BY status";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(statusQuery)) {
            while (rs.next()) {
                System.out.println("  " + rs.getString("status") + ": " + rs.getInt("count"));
            }
        }
        
        // By user
        System.out.println();
        System.out.println("Completed tasks by user:");
        String userQuery = "SELECT u.username, COUNT(t.id) as task_count " +
                "FROM tasks t JOIN users u ON t.assigned_to = u.id " +
                "WHERE t.status = 'COMPLETED' GROUP BY u.username";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(userQuery)) {
            while (rs.next()) {
                System.out.println("  " + rs.getString("username") + ": " + rs.getInt("task_count") + " tasks");
            }
        }
    }
}
