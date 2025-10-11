package com.worksphere.dao;

import com.worksphere.model.Category;
import com.worksphere.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category operations
 */
public class CategoryDAO {
    
    /**
     * Create a new category
     * @param category Category to create
     * @return Generated category ID
     */
    public int createCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, description, color, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getColor());
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setTimestamp(5, Timestamp.valueOf(now));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    category.setId(id);
                    category.setCreatedAt(now);
                    category.setUpdatedAt(now);
                    return id;
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Get category by ID
     * @param id Category ID
     * @return Category object or null if not found
     */
    public Category getCategoryById(int id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all categories
     * @return List of all categories
     */
    public List<Category> getAllCategories() throws SQLException {
        String sql = "SELECT * FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        
        return categories;
    }
    
    /**
     * Update an existing category
     * @param category Category to update
     * @return true if update was successful
     */
    public boolean updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, description = ?, color = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getColor());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, category.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Delete a category
     * @param id Category ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteCategory(int id) throws SQLException {
        // First, update all tasks using this category to have no category
        String updateTasksSql = "UPDATE tasks SET category_id = NULL WHERE category_id = ?";
        String deleteCategorySql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Update tasks first
                try (PreparedStatement updateStmt = conn.prepareStatement(updateTasksSql)) {
                    updateStmt.setInt(1, id);
                    updateStmt.executeUpdate();
                }
                
                // Then delete category
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteCategorySql)) {
                    deleteStmt.setInt(1, id);
                    int affectedRows = deleteStmt.executeUpdate();
                    
                    conn.commit();
                    return affectedRows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Get categories used by tasks (categories that have at least one task)
     * @return List of categories in use
     */
    public List<Category> getCategoriesInUse() throws SQLException {
        String sql = "SELECT DISTINCT c.* FROM categories c " +
                    "INNER JOIN tasks t ON c.id = t.category_id " +
                    "ORDER BY c.name";
        
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        
        return categories;
    }
    
    /**
     * Get task count for each category
     * @return List of categories with task counts
     */
    public List<CategoryTaskCount> getCategoryTaskCounts() throws SQLException {
        String sql = "SELECT c.*, " +
                    "COUNT(t.id) as task_count, " +
                    "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed_count " +
                    "FROM categories c " +
                    "LEFT JOIN tasks t ON c.id = t.category_id " +
                    "GROUP BY c.id, c.name, c.description, c.color, c.created_at, c.updated_at " +
                    "ORDER BY c.name";
        
        List<CategoryTaskCount> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = mapResultSetToCategory(rs);
                int taskCount = rs.getInt("task_count");
                int completedCount = rs.getInt("completed_count");
                results.add(new CategoryTaskCount(category, taskCount, completedCount));
            }
        }
        
        return results;
    }
    
    /**
     * Map ResultSet to Category object
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("color"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
    
    /**
     * Helper class for category statistics
     */
    public static class CategoryTaskCount {
        private final Category category;
        private final int taskCount;
        private final int completedCount;
        
        public CategoryTaskCount(Category category, int taskCount, int completedCount) {
            this.category = category;
            this.taskCount = taskCount;
            this.completedCount = completedCount;
        }
        
        public Category getCategory() { return category; }
        public int getTaskCount() { return taskCount; }
        public int getCompletedCount() { return completedCount; }
        public int getActiveCount() { return taskCount - completedCount; }
        public double getCompletionRate() { 
            return taskCount > 0 ? (double) completedCount / taskCount : 0.0; 
        }
    }
}