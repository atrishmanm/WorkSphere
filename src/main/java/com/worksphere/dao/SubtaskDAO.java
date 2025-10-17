package com.worksphere.dao;

import com.worksphere.model.Subtask;
import com.worksphere.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subtask operations
 */
public class SubtaskDAO {
    
    /**
     * Create a new subtask
     * @param subtask The subtask to create
     * @return The ID of the created subtask, or -1 if creation failed
     */
    public int createSubtask(Subtask subtask) {
        String sql = "INSERT INTO subtasks (task_id, title, completed, order_index, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, subtask.getTaskId());
            pstmt.setString(2, subtask.getTitle());
            pstmt.setBoolean(3, subtask.isCompleted());
            pstmt.setInt(4, subtask.getOrderIndex());
            pstmt.setObject(5, subtask.getCreatedAt());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get a subtask by ID
     * @param id The subtask ID
     * @return The subtask, or null if not found
     */
    public Subtask getSubtaskById(int id) {
        String sql = "SELECT * FROM subtasks WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractSubtaskFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all subtasks for a task
     * @param taskId The task ID
     * @return List of subtasks
     */
    public List<Subtask> getSubtasksByTaskId(int taskId) {
        List<Subtask> subtasks = new ArrayList<>();
        String sql = "SELECT * FROM subtasks WHERE task_id = ? ORDER BY order_index";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subtasks.add(extractSubtaskFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return subtasks;
    }
    
    /**
     * Update a subtask
     * @param subtask The subtask to update
     * @return true if update was successful
     */
    public boolean updateSubtask(Subtask subtask) {
        String sql = "UPDATE subtasks SET title = ?, completed = ?, order_index = ?, " +
                    "completed_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subtask.getTitle());
            pstmt.setBoolean(2, subtask.isCompleted());
            pstmt.setInt(3, subtask.getOrderIndex());
            pstmt.setObject(4, subtask.getCompletedAt());
            pstmt.setInt(5, subtask.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Toggle subtask completion status
     * @param id The subtask ID
     * @return true if toggle was successful
     */
    public boolean toggleSubtaskCompletion(int id) {
        Subtask subtask = getSubtaskById(id);
        if (subtask != null) {
            subtask.toggleCompleted();
            return updateSubtask(subtask);
        }
        return false;
    }
    
    /**
     * Delete a subtask
     * @param id The subtask ID
     * @return true if deletion was successful
     */
    public boolean deleteSubtask(int id) {
        String sql = "DELETE FROM subtasks WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete all subtasks for a task
     * @param taskId The task ID
     * @return true if deletion was successful
     */
    public boolean deleteSubtasksByTaskId(int taskId) {
        String sql = "DELETE FROM subtasks WHERE task_id = ?";
        
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
     * Get count of completed subtasks for a task
     * @param taskId The task ID
     * @return Number of completed subtasks
     */
    public int getCompletedCount(int taskId) {
        String sql = "SELECT COUNT(*) FROM subtasks WHERE task_id = ? AND completed = 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get total count of subtasks for a task
     * @param taskId The task ID
     * @return Total number of subtasks
     */
    public int getTotalCount(int taskId) {
        String sql = "SELECT COUNT(*) FROM subtasks WHERE task_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Extract a Subtask object from a ResultSet
     * @param rs The ResultSet
     * @return Subtask object
     * @throws SQLException if database access error occurs
     */
    private Subtask extractSubtaskFromResultSet(ResultSet rs) throws SQLException {
        Subtask subtask = new Subtask();
        subtask.setId(rs.getInt("id"));
        subtask.setTaskId(rs.getInt("task_id"));
        subtask.setTitle(rs.getString("title"));
        subtask.setCompleted(rs.getBoolean("completed"));
        subtask.setOrderIndex(rs.getInt("order_index"));
        
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            subtask.setCreatedAt(createdTimestamp.toLocalDateTime());
        }
        
        Timestamp completedTimestamp = rs.getTimestamp("completed_at");
        if (completedTimestamp != null) {
            subtask.setCompletedAt(completedTimestamp.toLocalDateTime());
        }
        
        return subtask;
    }
}
