package com.worksphere.dao;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Task entity
 */
public class TaskDAO {
    
    private static final String INSERT_TASK = 
        "INSERT INTO tasks (title, description, priority, status, due_date, assigned_to, created_by, " +
        "estimated_minutes, category_id, recurrence_rule, parent_task_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_TASK_BY_ID = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, t.completed_at, " +
        "t.estimated_minutes, t.actual_minutes, t.category_id, t.recurrence_rule, " +
        "t.parent_task_id, t.last_worked_at, " +
        "u1.username as assigned_username, u2.username as created_username, " +
        "c.name as category_name, c.color as category_color " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "LEFT JOIN categories c ON t.category_id = c.id " +
        "WHERE t.id = ?";
    
    private static final String SELECT_ALL_TASKS = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, t.completed_at, " +
        "t.estimated_minutes, t.actual_minutes, t.category_id, t.recurrence_rule, " +
        "t.parent_task_id, t.last_worked_at, " +
        "u1.username as assigned_username, u2.username as created_username, " +
        "c.name as category_name, c.color as category_color " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "LEFT JOIN categories c ON t.category_id = c.id " +
        "ORDER BY t.created_at DESC";
    
    private static final String SELECT_TASKS_BY_STATUS = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, t.completed_at, " +
        "t.estimated_minutes, t.actual_minutes, t.category_id, t.recurrence_rule, " +
        "t.parent_task_id, t.last_worked_at, " +
        "u1.username as assigned_username, u2.username as created_username, " +
        "c.name as category_name, c.color as category_color " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "LEFT JOIN categories c ON t.category_id = c.id " +
        "WHERE t.status = ? ORDER BY t.created_at DESC";
    
    private static final String UPDATE_TASK = 
        "UPDATE tasks SET title = ?, description = ?, priority = ?, status = ?, due_date = ?, " +
        "assigned_to = ?, estimated_minutes = ?, category_id = ?, recurrence_rule = ?, " +
        "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String UPDATE_TASK_TIME = 
        "UPDATE tasks SET actual_minutes = ?, last_worked_at = CURRENT_TIMESTAMP, " +
        "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String UPDATE_TASK_COMPLETION = 
        "UPDATE tasks SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP, " +
        "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String SELECT_TASKS_BY_ASSIGNED_USER = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, " +
        "u1.username as assigned_username, u2.username as created_username " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "WHERE t.assigned_to = ? ORDER BY t.created_at DESC";
    
    private static final String SELECT_TASKS_BY_CREATOR = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, " +
        "u1.username as assigned_username, u2.username as created_username " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "WHERE t.created_by = ? ORDER BY t.created_at DESC";
    
    private static final String SELECT_TASKS_FOR_USER = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, " +
        "u1.username as assigned_username, u2.username as created_username " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "WHERE t.assigned_to = ? OR t.created_by = ? ORDER BY t.created_at DESC";
    
    private static final String SELECT_TASKS_BY_PRIORITY = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, " +
        "u1.username as assigned_username, u2.username as created_username " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "WHERE t.priority = ? ORDER BY t.created_at DESC";
    
    private static final String SELECT_OVERDUE_TASKS = 
        "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
        "t.assigned_to, t.created_by, t.created_at, t.updated_at, " +
        "u1.username as assigned_username, u2.username as created_username " +
        "FROM tasks t " +
        "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
        "LEFT JOIN users u2 ON t.created_by = u2.id " +
        "WHERE t.due_date < date('now') AND t.status != 'COMPLETED' ORDER BY t.due_date ASC";
    
    private static final String UPDATE_TASK_STATUS = 
        "UPDATE tasks SET status = ? WHERE id = ?";
    
    private static final String DELETE_TASK = 
        "DELETE FROM tasks WHERE id = ?";
    
    private static final String COUNT_TASKS_BY_STATUS = 
        "SELECT COUNT(*) FROM tasks WHERE status = ?";
    
    /**
     * Create a new task
     * @param task Task to create
     * @return the created task with ID set
     * @throws SQLException if database operation fails
     */
    public Task createTask(Task task) throws SQLException {
        // COMPREHENSIVE LOGGING FOR TASK CREATION DEBUGGING
        System.out.println("🚨 TASK CREATION DETECTED:");
        System.out.println("  📝 Title: " + task.getTitle());
        System.out.println("  📝 Description: " + task.getDescription());
        System.out.println("  📝 Priority: " + task.getPriority());
        System.out.println("  📝 Status: " + task.getStatus());
        System.out.println("  📝 AssignedTo: " + task.getAssignedTo());
        System.out.println("  📝 CreatedBy: " + task.getCreatedBy());
        System.out.println("  📝 CategoryId: " + task.getCategoryId());
        System.out.println("  📝 EstimatedMinutes: " + task.getEstimatedMinutes());
        
        // Print the full stack trace to see WHO is calling this
        System.out.println("  📍 CALL STACK:");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < Math.min(stackTrace.length, 10); i++) {
            System.out.println("    " + i + ". " + stackTrace[i].getClassName() + "." + stackTrace[i].getMethodName() + "(" + stackTrace[i].getFileName() + ":" + stackTrace[i].getLineNumber() + ")");
        }
        System.out.println("  ⏰ Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("🚨 END TASK CREATION DEBUG");
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_TASK)) {
            
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getPriority().name());
            statement.setString(4, task.getStatus().name());
            
            if (task.getDueDate() != null) {
                statement.setDate(5, Date.valueOf(task.getDueDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }
            
            if (task.getAssignedTo() != null) {
                statement.setInt(6, task.getAssignedTo());
            } else {
                statement.setNull(6, Types.INTEGER);
            }
            
            statement.setInt(7, task.getCreatedBy());
            statement.setInt(8, task.getEstimatedMinutes());
            
            if (task.getCategoryId() > 0) {
                statement.setInt(9, task.getCategoryId());
            } else {
                statement.setNull(9, Types.INTEGER);
            }
            
            statement.setString(10, task.getRecurrenceRule());
            
            if (task.getParentTaskId() != null) {
                statement.setInt(11, task.getParentTaskId());
            } else {
                statement.setNull(11, Types.INTEGER);
            }
            
            System.out.println("🔥 EXECUTING INSERT STATEMENT NOW!");
            int affectedRows = statement.executeUpdate();
            System.out.println("💾 INSERT COMPLETED - Affected rows: " + affectedRows);
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }
            
            // Get the generated ID using SQLite's last_insert_rowid() function
            try (PreparedStatement idStatement = connection.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet resultSet = idStatement.executeQuery()) {
                if (resultSet.next()) {
                    int taskId = resultSet.getInt(1);
                    task.setId(taskId);
                    
                    // Insert tags if any
                    if (task.getTags() != null && !task.getTags().isEmpty()) {
                        insertTaskTags(connection, taskId, task.getTags());
                    }
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
            
            return task;
        }
    }
    
    /**
     * Find task by ID
     * @param id Task ID
     * @return Optional containing task if found, empty otherwise
     * @throws SQLException if database operation fails
     */
    public Optional<Task> findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TASK_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToTask(resultSet));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get all tasks
     * @return List of all tasks
     * @throws SQLException if database operation fails
     */
    public List<Task> findAll() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_TASKS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                tasks.add(mapResultSetToTask(resultSet));
            }
        }
        
        return tasks;
    }
    
    /**
     * Find tasks by status
     * @param status Task status
     * @return List of tasks with the specified status
     * @throws SQLException if database operation fails
     */
    public List<Task> findByStatus(TaskStatus status) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TASKS_BY_STATUS)) {
            
            statement.setString(1, status.name());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapResultSetToTask(resultSet));
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Find tasks assigned to a specific user
     * @param userId User ID
     * @return List of tasks assigned to the user
     * @throws SQLException if database operation fails
     */
    public List<Task> findByAssignedUser(int userId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TASKS_BY_ASSIGNED_USER)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapResultSetToTask(resultSet));
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Find tasks created by a specific user
     * @param userId User ID
     * @return List of tasks created by the user
     * @throws SQLException if database operation fails
     */
    public List<Task> findByCreator(int userId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TASKS_BY_CREATOR)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapResultSetToTask(resultSet));
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Find tasks visible to a specific user (assigned to them OR created by them)
     * This is for non-admin users who can only see their own tasks
     * @param userId User ID
     * @return List of tasks visible to the user
     * @throws SQLException if database operation fails
     */
    public List<Task> findTasksForUser(int userId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TASKS_FOR_USER)) {
            
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapResultSetToTask(resultSet));
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Find tasks by priority
     * @param priority Task priority
     * @return List of tasks with the specified priority
     * @throws SQLException if database operation fails
     */
    public List<Task> findByPriority(Priority priority) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TASKS_BY_PRIORITY)) {
            
            statement.setString(1, priority.name());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapResultSetToTask(resultSet));
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Find overdue tasks
     * @return List of overdue tasks
     * @throws SQLException if database operation fails
     */
    public List<Task> findOverdueTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_OVERDUE_TASKS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                tasks.add(mapResultSetToTask(resultSet));
            }
        }
        
        return tasks;
    }
    
    /**
     * Update task
     * @param task Task to update
     * @return true if update was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateTask(Task task) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_TASK)) {
            
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getPriority().name());
            statement.setString(4, task.getStatus().name());
            
            if (task.getDueDate() != null) {
                statement.setDate(5, Date.valueOf(task.getDueDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }
            
            if (task.getAssignedTo() != null) {
                statement.setInt(6, task.getAssignedTo());
            } else {
                statement.setNull(6, Types.INTEGER);
            }
            
            statement.setInt(7, task.getEstimatedMinutes());
            
            if (task.getCategoryId() > 0) {
                statement.setInt(8, task.getCategoryId());
            } else {
                statement.setNull(8, Types.INTEGER);
            }
            
            statement.setString(9, task.getRecurrenceRule());
            statement.setInt(10, task.getId());
            
            boolean result = statement.executeUpdate() > 0;
            
            // Update tags
            if (result) {
                updateTaskTags(task.getId(), task.getTags());
            }
            
            return result;
        }
    }
    
    /**
     * Update task status
     * @param taskId Task ID
     * @param status New status
     * @return true if update was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateTaskStatus(int taskId, TaskStatus status) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_TASK_STATUS)) {
            
            statement.setString(1, status.name());
            statement.setInt(2, taskId);
            
            return statement.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete task by ID
     * @param id Task ID
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteTask(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_TASK)) {
            
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
    
    /**
     * Get count of tasks by status
     * @param status Task status
     * @return number of tasks with the specified status
     * @throws SQLException if database operation fails
     */
    public int getTaskCountByStatus(TaskStatus status) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_TASKS_BY_STATUS)) {
            
            statement.setString(1, status.name());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Map ResultSet to Task object
     * @param resultSet ResultSet
     * @return Task object
     * @throws SQLException if mapping fails
     */
    private Task mapResultSetToTask(ResultSet resultSet) throws SQLException {
        Task task = new Task();
        
        task.setId(resultSet.getInt("id"));
        task.setTitle(resultSet.getString("title"));
        task.setDescription(resultSet.getString("description"));
        task.setPriority(Priority.valueOf(resultSet.getString("priority")));
        task.setStatus(TaskStatus.valueOf(resultSet.getString("status")));
        
        // Handle due_date more safely for SQLite
        try {
            Date dueDate = resultSet.getDate("due_date");
            if (dueDate != null) {
                task.setDueDate(dueDate.toLocalDate());
            }
        } catch (SQLException e) {
            // Try parsing as string if getDate fails
            try {
                String dueDateStr = resultSet.getString("due_date");
                if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                    task.setDueDate(LocalDate.parse(dueDateStr));
                }
            } catch (Exception ex) {
                // Skip due date if all parsing methods fail
                task.setDueDate(null);
            }
        }
        
        // Handle assigned_to safely
        try {
            int assignedTo = resultSet.getInt("assigned_to");
            if (!resultSet.wasNull()) {
                task.setAssignedTo(assignedTo);
            }
        } catch (SQLException e) {
            // Skip assigned_to if parsing fails
            task.setAssignedTo(null);
        }
        
        // Handle created_by safely  
        try {
            task.setCreatedBy(resultSet.getInt("created_by"));
        } catch (SQLException e) {
            // Use a default value if created_by fails
            task.setCreatedBy(1); // Default user ID
        }
        
        // Handle new enhanced fields
        try {
            task.setEstimatedMinutes(resultSet.getInt("estimated_minutes"));
        } catch (SQLException e) {
            task.setEstimatedMinutes(0);
        }
        
        try {
            task.setActualMinutes(resultSet.getInt("actual_minutes"));
        } catch (SQLException e) {
            task.setActualMinutes(0);
        }
        
        try {
            int categoryId = resultSet.getInt("category_id");
            if (!resultSet.wasNull()) {
                task.setCategoryId(categoryId);
                // Create category object for display
                String categoryName = resultSet.getString("category_name");
                String categoryColor = resultSet.getString("category_color");
                if (categoryName != null) {
                    com.worksphere.model.Category category = new com.worksphere.model.Category();
                    category.setId(categoryId);
                    category.setName(categoryName);
                    category.setColor(categoryColor);
                    task.setCategory(category);
                }
            }
        } catch (SQLException e) {
            task.setCategoryId(0);
        }
        
        try {
            task.setRecurrenceRule(resultSet.getString("recurrence_rule"));
        } catch (SQLException e) {
            task.setRecurrenceRule(null);
        }
        
        try {
            int parentTaskId = resultSet.getInt("parent_task_id");
            if (!resultSet.wasNull()) {
                task.setParentTaskId(parentTaskId);
            }
        } catch (SQLException e) {
            task.setParentTaskId(null);
        }
        
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
            task.setCreatedAt(createdDateTime != null ? createdDateTime : LocalDateTime.now());
            
        } catch (Exception e) {
            // Ultimate fallback
            task.setCreatedAt(LocalDateTime.now());
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
            task.setUpdatedAt(updatedDateTime != null ? updatedDateTime : LocalDateTime.now());
            
        } catch (Exception e) {
            // Ultimate fallback
            task.setUpdatedAt(LocalDateTime.now());
        }
        
        // Handle completed_at timestamp
        try {
            Timestamp completedTimestamp = resultSet.getTimestamp("completed_at");
            if (completedTimestamp != null) {
                task.setCompletedAt(completedTimestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Try string parsing
            try {
                String completedAtStr = resultSet.getString("completed_at");
                if (completedAtStr != null && !completedAtStr.trim().isEmpty()) {
                    task.setCompletedAt(parseTimestampString(completedAtStr));
                }
            } catch (Exception ex) {
                task.setCompletedAt(null);
            }
        }
        
        // Handle last_worked_at timestamp
        try {
            Timestamp lastWorkedTimestamp = resultSet.getTimestamp("last_worked_at");
            if (lastWorkedTimestamp != null) {
                task.setLastWorkedAt(lastWorkedTimestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Try string parsing
            try {
                String lastWorkedAtStr = resultSet.getString("last_worked_at");
                if (lastWorkedAtStr != null && !lastWorkedAtStr.trim().isEmpty()) {
                    task.setLastWorkedAt(parseTimestampString(lastWorkedAtStr));
                }
            } catch (Exception ex) {
                task.setLastWorkedAt(null);
            }
        }
        
        // Set usernames for display
        task.setAssignedToUsername(resultSet.getString("assigned_username"));
        task.setCreatedByUsername(resultSet.getString("created_username"));
        
        // Load tags for this task
        try (Connection conn = DatabaseConnection.getConnection()) {
            task.setTags(loadTaskTags(conn, task.getId()));
        } catch (SQLException e) {
            // If loading tags fails, set empty list
            task.setTags(new ArrayList<>());
        }
        
        return task;
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
    
    /**
     * Insert tags for a task
     */
    private void insertTaskTags(Connection connection, int taskId, List<String> tags) throws SQLException {
        String sql = "INSERT OR IGNORE INTO task_tags (task_id, tag_name) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String tag : tags) {
                if (tag != null && !tag.trim().isEmpty()) {
                    stmt.setInt(1, taskId);
                    stmt.setString(2, tag.trim());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }
    
    /**
     * Load tags for a task
     */
    private List<String> loadTaskTags(Connection connection, int taskId) throws SQLException {
        String sql = "SELECT tag_name FROM task_tags WHERE task_id = ? ORDER BY tag_name";
        List<String> tags = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("tag_name"));
                }
            }
        }
        return tags;
    }
    
    /**
     * Update tags for a task
     */
    public void updateTaskTags(int taskId, List<String> tags) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Delete existing tags
            String deleteSql = "DELETE FROM task_tags WHERE task_id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, taskId);
                deleteStmt.executeUpdate();
            }
            
            // Insert new tags
            if (tags != null && !tags.isEmpty()) {
                insertTaskTags(connection, taskId, tags);
            }
        }
    }
    
    /**
     * Find tasks by category
     */
    public List<Task> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
                    "t.assigned_to, t.created_by, t.created_at, t.updated_at, t.completed_at, " +
                    "t.estimated_minutes, t.actual_minutes, t.category_id, t.recurrence_rule, " +
                    "t.parent_task_id, t.last_worked_at, " +
                    "u1.username as assigned_username, u2.username as created_username, " +
                    "c.name as category_name, c.color as category_color " +
                    "FROM tasks t " +
                    "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
                    "LEFT JOIN users u2 ON t.created_by = u2.id " +
                    "LEFT JOIN categories c ON t.category_id = c.id " +
                    "WHERE t.category_id = ? ORDER BY t.created_at DESC";
        
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }
    
    /**
     * Find tasks by tag
     */
    public List<Task> findByTag(String tag) throws SQLException {
        String sql = "SELECT DISTINCT t.id, t.title, t.description, t.priority, t.status, t.due_date, " +
                    "t.assigned_to, t.created_by, t.created_at, t.updated_at, t.completed_at, " +
                    "t.estimated_minutes, t.actual_minutes, t.category_id, t.recurrence_rule, " +
                    "t.parent_task_id, t.last_worked_at, " +
                    "u1.username as assigned_username, u2.username as created_username, " +
                    "c.name as category_name, c.color as category_color " +
                    "FROM tasks t " +
                    "LEFT JOIN users u1 ON t.assigned_to = u1.id " +
                    "LEFT JOIN users u2 ON t.created_by = u2.id " +
                    "LEFT JOIN categories c ON t.category_id = c.id " +
                    "INNER JOIN task_tags tt ON t.id = tt.task_id " +
                    "WHERE tt.tag_name = ? ORDER BY t.created_at DESC";
        
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }
    
    /**
     * Update task time worked
     */
    public boolean updateTaskTime(int taskId, int actualMinutes) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_TASK_TIME)) {
            
            stmt.setInt(1, actualMinutes);
            stmt.setInt(2, taskId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Mark task as completed
     */
    public boolean markTaskCompleted(int taskId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_TASK_COMPLETION)) {
            
            stmt.setInt(1, taskId);
            return stmt.executeUpdate() > 0;
        }
    }
}
