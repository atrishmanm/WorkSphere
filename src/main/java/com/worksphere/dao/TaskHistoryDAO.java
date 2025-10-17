package com.worksphere.dao;

import com.worksphere.model.TaskHistory;
import com.worksphere.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TaskHistory operations
 */
public class TaskHistoryDAO {
    
    /**
     * Create a new task history entry
     * @param history The task history to create
     * @return The ID of the created history entry, or -1 if creation failed
     */
    public int createHistory(TaskHistory history) {
        String sql = "INSERT INTO task_history (task_id, user_id, action, field_changed, " +
                    "old_value, new_value, timestamp) VALUES (?, ?, ?, ?, ?, ?, datetime('now'))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, history.getTaskId());
            pstmt.setInt(2, history.getUserId());
            pstmt.setString(3, history.getAction());
            pstmt.setString(4, history.getFieldChanged());
            pstmt.setString(5, history.getOldValue());
            pstmt.setString(6, history.getNewValue());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // SQLite workaround: query for last inserted row id
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating task history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get history for a specific task
     * @param taskId The task ID
     * @return List of history entries
     */
    public List<TaskHistory> getHistoryByTaskId(int taskId) {
        List<TaskHistory> history = new ArrayList<>();
        String sql = "SELECT h.*, u.username FROM task_history h " +
                    "LEFT JOIN users u ON h.user_id = u.id " +
                    "WHERE h.task_id = ? ORDER BY h.timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(extractHistoryFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Get history by user
     * @param userId The user ID
     * @return List of history entries
     */
    public List<TaskHistory> getHistoryByUserId(int userId) {
        List<TaskHistory> history = new ArrayList<>();
        String sql = "SELECT h.*, u.username FROM task_history h " +
                    "LEFT JOIN users u ON h.user_id = u.id " +
                    "WHERE h.user_id = ? ORDER BY h.timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(extractHistoryFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Get recent history across all tasks
     * @param limit Maximum number of entries to return
     * @return List of recent history entries
     */
    public List<TaskHistory> getRecentHistory(int limit) {
        List<TaskHistory> history = new ArrayList<>();
        String sql = "SELECT h.*, u.username FROM task_history h " +
                    "LEFT JOIN users u ON h.user_id = u.id " +
                    "ORDER BY h.timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(extractHistoryFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Delete history for a specific task
     * @param taskId The task ID
     * @return true if deletion was successful
     */
    public boolean deleteHistoryByTaskId(int taskId) {
        String sql = "DELETE FROM task_history WHERE task_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            return pstmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Log a task creation
     * @param taskId The task ID
     * @param userId The user ID who created the task
     * @return The history entry ID
     */
    public int logTaskCreation(int taskId, int userId) {
        TaskHistory history = new TaskHistory(taskId, userId, "CREATED", null, null, null);
        return createHistory(history);
    }
    
    /**
     * Log a field change
     * @param taskId The task ID
     * @param userId The user ID who made the change
     * @param fieldName The field that was changed
     * @param oldValue The old value
     * @param newValue The new value
     * @return The history entry ID
     */
    public int logFieldChange(int taskId, int userId, String fieldName, 
                             String oldValue, String newValue) {
        TaskHistory history = new TaskHistory(taskId, userId, "UPDATED", 
                                            fieldName, oldValue, newValue);
        return createHistory(history);
    }
    
    /**
     * Log a status change
     * @param taskId The task ID
     * @param userId The user ID who changed the status
     * @param oldStatus The old status
     * @param newStatus The new status
     * @return The history entry ID
     */
    public int logStatusChange(int taskId, int userId, String oldStatus, String newStatus) {
        TaskHistory history = new TaskHistory(taskId, userId, "STATUS_CHANGED", 
                                            "status", oldStatus, newStatus);
        return createHistory(history);
    }
    
    /**
     * Log task completion
     * @param taskId The task ID
     * @param userId The user ID who completed the task
     * @return The history entry ID
     */
    public int logTaskCompletion(int taskId, int userId) {
        TaskHistory history = new TaskHistory(taskId, userId, "COMPLETED", null, null, null);
        return createHistory(history);
    }
    
    /**
     * Extract a TaskHistory object from a ResultSet
     * @param rs The ResultSet
     * @return TaskHistory object
     * @throws SQLException if database access error occurs
     */
    private TaskHistory extractHistoryFromResultSet(ResultSet rs) throws SQLException {
        TaskHistory history = new TaskHistory();
        history.setId(rs.getInt("id"));
        history.setTaskId(rs.getInt("task_id"));
        history.setUserId(rs.getInt("user_id"));
        history.setAction(rs.getString("action"));
        history.setFieldChanged(rs.getString("field_changed"));
        history.setOldValue(rs.getString("old_value"));
        history.setNewValue(rs.getString("new_value"));
        history.setUsername(rs.getString("username"));
        
        // SQLite-compatible timestamp reading
        try {
            String timestampStr = rs.getString("timestamp");
            if (timestampStr != null && !timestampStr.isEmpty()) {
                // SQLite format: "2025-10-17 22:56:05" or with milliseconds
                timestampStr = timestampStr.replace('T', ' '); // Handle ISO format
                
                // Remove nanoseconds if present (SQLite sometimes adds them)
                if (timestampStr.length() > 23) {
                    timestampStr = timestampStr.substring(0, 23);
                }
                
                history.setTimestamp(java.time.LocalDateTime.parse(timestampStr.replace(' ', 'T')));
            }
        } catch (Exception e) {
            System.err.println("Error parsing timestamp: " + e.getMessage());
            // Set to current time if parsing fails
            history.setTimestamp(java.time.LocalDateTime.now());
        }
        
        return history;
    }
}
