package com.worksphere.service;

import com.worksphere.dao.UserDAO;
import com.worksphere.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User-related business logic
 */
public class UserService {
    private final UserDAO userDAO;
    
    public UserService() {
        System.out.println("👤 Creating UserService...");
        this.userDAO = new UserDAO();
        System.out.println("👤 UserService created successfully.");
    }
    
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Create a new user with validation
     * @param username Username
     * @param email Email address
     * @param fullName Full name
     * @return Created user
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException if database operation fails
     */
    public User createUser(String username, String email, String fullName) throws SQLException {
        return createUser(username, email, fullName, false);
    }
    
    /**
     * Create a new user with validation and role specification
     * @param username Username
     * @param email Email address (can be null)
     * @param fullName Full name
     * @param isAdmin Whether the user should be an admin
     * @return Created user
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException if database operation fails
     */
    public User createUser(String username, String email, String fullName, boolean isAdmin) throws SQLException {
        // Validate input (allow null email)
        validateUserInput(username, email, fullName, true);
        
        // Check for duplicates
        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("Username '" + username + "' already exists");
        }
        
        if (email != null && userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email '" + email + "' already exists");
        }
        
        User user = new User(username, email, fullName, isAdmin);
        return userDAO.createUser(user);
    }
    
    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found
     * @throws SQLException if database operation fails
     */
    public Optional<User> findUserById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return userDAO.findById(id);
    }
    
    /**
     * Find user by username
     * @param username Username
     * @return Optional containing user if found
     * @throws SQLException if database operation fails
     */
    public Optional<User> findUserByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        return userDAO.findByUsername(username.trim());
    }
    
    /**
     * Find user by email
     * @param email Email
     * @return Optional containing user if found
     * @throws SQLException if database operation fails
     */
    public Optional<User> findUserByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return userDAO.findByEmail(email.trim());
    }
    
    // GUI Helper Methods
    /**
     * Get user by username - returns null if not found (for GUI compatibility)
     */
    public User getUserByUsername(String username) throws SQLException {
        Optional<User> userOpt = findUserByUsername(username);
        return userOpt.orElse(null);
    }
    
    /**
     * Get user by ID - returns null if not found (for GUI compatibility)
     */
    public User getUserById(int id) throws SQLException {
        Optional<User> userOpt = findUserById(id);
        return userOpt.orElse(null);
    }
    
    /**
     * Get all users
     * @return List of all users
     * @throws SQLException if database operation fails
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
    
    /**
     * Update user information
     * @param user User to update
     * @return true if update was successful
     * @throws SQLException if database operation fails
     */
    public boolean updateUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        validateUserInput(user.getUsername(), user.getEmail(), user.getFullName(), false);
        
        // Check if user exists
        Optional<User> existingUser = userDAO.findById(user.getId());
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " not found");
        }
        
        // Check for username conflicts (exclude current user)
        Optional<User> userWithSameUsername = userDAO.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && userWithSameUsername.get().getId() != user.getId()) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists");
        }
        
        // Check for email conflicts (exclude current user)
        Optional<User> userWithSameEmail = userDAO.findByEmail(user.getEmail());
        if (userWithSameEmail.isPresent() && userWithSameEmail.get().getId() != user.getId()) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' already exists");
        }
        
        return userDAO.updateUser(user);
    }
    
    /**
     * Delete user by ID
     * @param id User ID
     * @return true if deletion was successful
     * @throws SQLException if database operation fails
     */
    public boolean deleteUser(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Check if user exists
        Optional<User> user = userDAO.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        
        return userDAO.deleteUser(id);
    }
    
    /**
     * Check if username is available
     * @param username Username to check
     * @return true if username is available, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean isUsernameAvailable(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return !userDAO.usernameExists(username.trim());
    }
    
    /**
     * Check if email is available
     * @param email Email to check
     * @return true if email is available, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean isEmailAvailable(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !userDAO.emailExists(email.trim());
    }
    
    /**
     * Get total number of users
     * @return total number of users
     * @throws SQLException if database operation fails
     */
    public int getTotalUsers() throws SQLException {
        return userDAO.getTotalUsers();
    }
    
    /**
     * Validate user input
     * @param username Username
     * @param email Email
     * @param fullName Full name
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserInput(String username, String email, String fullName) {
        validateUserInput(username, email, fullName, false);
    }
    
    /**
     * Validate user input with optional email handling
     * @param username Username
     * @param email Email (can be null if allowNullEmail is true)
     * @param fullName Full name
     * @param allowNullEmail Whether email can be null
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserInput(String username, String email, String fullName, boolean allowNullEmail) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (!allowNullEmail && (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        
        // Validate username format (alphanumeric and underscore only)
        String cleanUsername = username.trim();
        if (!cleanUsername.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
        
        // Validate email format if provided
        if (email != null && !email.trim().isEmpty()) {
            String cleanEmail = email.trim();
            if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        
        // Validate full name length
        String cleanFullName = fullName.trim();
        if (cleanFullName.length() < 2 || cleanFullName.length() > 100) {
            throw new IllegalArgumentException("Full name must be between 2 and 100 characters long");
        }
    }
}
