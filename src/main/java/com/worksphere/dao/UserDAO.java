package com.worksphere.dao;

import com.worksphere.model.User;
import com.worksphere.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entity
 */
public class UserDAO {
    
    private static final String INSERT_USER = 
        "INSERT INTO users (username, email, full_name, is_admin) VALUES (?, ?, ?, ?)";
    
    private static final String SELECT_USER_BY_ID = 
        "SELECT id, username, email, full_name, is_admin, created_at, updated_at FROM users WHERE id = ?";
    
    private static final String SELECT_USER_BY_USERNAME = 
        "SELECT id, username, email, full_name, is_admin, created_at, updated_at FROM users WHERE username = ?";
    
    private static final String SELECT_USER_BY_EMAIL = 
        "SELECT id, username, email, full_name, is_admin, created_at, updated_at FROM users WHERE email = ?";
    
    private static final String SELECT_ALL_USERS = 
        "SELECT id, username, email, full_name, is_admin, created_at, updated_at FROM users ORDER BY username";
    
    private static final String UPDATE_USER = 
        "UPDATE users SET username = ?, email = ?, full_name = ?, is_admin = ? WHERE id = ?";
    
    private static final String DELETE_USER = 
        "DELETE FROM users WHERE id = ?";
    
    private static final String COUNT_USERS = 
        "SELECT COUNT(*) FROM users";
    
    /**
     * Create a new user
     * @param user User to create
     * @return the created user with ID set
     * @throws SQLException if database operation fails
     */
    public User createUser(User user) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getFullName());
            statement.setBoolean(4, user.isAdmin());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            return user;
        }
    }
    
    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found, empty otherwise
     * @throws SQLException if database operation fails
     */
    public Optional<User> findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find user by username
     * @param username Username
     * @return Optional containing user if found, empty otherwise
     * @throws SQLException if database operation fails
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_USERNAME)) {
            
            statement.setString(1, username);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find user by email
     * @param email Email
     * @return Optional containing user if found, empty otherwise
     * @throws SQLException if database operation fails
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_EMAIL)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get all users
     * @return List of all users
     * @throws SQLException if database operation fails
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_USERS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }
        
        return users;
    }
    
    /**
     * Update user
     * @param user User to update
     * @return true if update was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateUser(User user) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getFullName());
            statement.setBoolean(4, user.isAdmin());
            statement.setInt(5, user.getId());
            
            return statement.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete user by ID
     * @param id User ID
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteUser(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {
            
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean usernameExists(String username) throws SQLException {
        return findByUsername(username).isPresent();
    }
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean emailExists(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }
    
    /**
     * Get total number of users
     * @return total number of users
     * @throws SQLException if database operation fails
     */
    public int getTotalUsers() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_USERS);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Map ResultSet to User object
     * @param resultSet ResultSet
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setFullName(resultSet.getString("full_name"));
        user.setAdmin(resultSet.getBoolean("is_admin"));
        
        // Handle timestamps more safely for SQLite with comprehensive parsing
        try {
            // Try multiple approaches to get the timestamp
            LocalDateTime createdDateTime = null;
            
            // Method 1: Try getTimestamp first
            try {
                Timestamp createdTimestamp = resultSet.getTimestamp("created_at");
                if (createdTimestamp != null) {
                    createdDateTime = createdTimestamp.toLocalDateTime();
                }
            } catch (SQLException e) {
                // Method 1 failed, try other methods
            }
            
            // Method 2: Try getString and parse manually if Method 1 failed
            if (createdDateTime == null) {
                String createdAtStr = resultSet.getString("created_at");
                if (createdAtStr != null && !createdAtStr.trim().isEmpty()) {
                    createdDateTime = parseTimestampString(createdAtStr);
                }
            }
            
            // Set the result or use current time as fallback
            user.setCreatedAt(createdDateTime != null ? createdDateTime : LocalDateTime.now());
            
        } catch (Exception e) {
            // Ultimate fallback
            user.setCreatedAt(LocalDateTime.now());
        }
        
        try {
            // Try multiple approaches to get the timestamp
            LocalDateTime updatedDateTime = null;
            
            // Method 1: Try getTimestamp first
            try {
                Timestamp updatedTimestamp = resultSet.getTimestamp("updated_at");
                if (updatedTimestamp != null) {
                    updatedDateTime = updatedTimestamp.toLocalDateTime();
                }
            } catch (SQLException e) {
                // Method 1 failed, try other methods
            }
            
            // Method 2: Try getString and parse manually if Method 1 failed
            if (updatedDateTime == null) {
                String updatedAtStr = resultSet.getString("updated_at");
                if (updatedAtStr != null && !updatedAtStr.trim().isEmpty()) {
                    updatedDateTime = parseTimestampString(updatedAtStr);
                }
            }
            
            // Set the result or use current time as fallback
            user.setUpdatedAt(updatedDateTime != null ? updatedDateTime : LocalDateTime.now());
            
        } catch (Exception e) {
            // Ultimate fallback
            user.setUpdatedAt(LocalDateTime.now());
        }
        
        return user;
    }
    
    /**
     * Helper method to parse timestamp strings in various formats
     */
    private LocalDateTime parseTimestampString(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            return null;
        }
        
        String cleanStr = timestampStr.trim();
        
        try {
            // Format 1: "YYYY-MM-DD HH:MM:SS" (SQLite default)
            if (cleanStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(cleanStr.replace(" ", "T"));
            }
            
            // Format 2: "YYYY-MM-DD HH:MM:SS.SSS" (with milliseconds)
            if (cleanStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")) {
                return LocalDateTime.parse(cleanStr.replace(" ", "T"));
            }
            
            // Format 3: ISO format "YYYY-MM-DDTHH:MM:SS"
            if (cleanStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(cleanStr);
            }
            
            // Format 4: Try direct Timestamp.valueOf parsing
            try {
                Timestamp ts = Timestamp.valueOf(cleanStr);
                return ts.toLocalDateTime();
            } catch (Exception e) {
                // Continue to next method
            }
            
            // Format 5: Try parsing without time part, use start of day
            if (cleanStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(cleanStr).atStartOfDay();
            }
            
        } catch (Exception e) {
            // All parsing methods failed
        }
        
        return null;
    }
}
