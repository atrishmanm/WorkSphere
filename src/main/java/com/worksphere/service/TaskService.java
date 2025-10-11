package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.dao.UserDAO;
import com.worksphere.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Task-related business logic
 */
public class TaskService {
    private final TaskDAO taskDAO;
    private final UserDAO userDAO;
    
    public TaskService() {
        System.out.println("📋 Creating TaskService...");
        this.taskDAO = new TaskDAO();
        this.userDAO = new UserDAO();
        System.out.println("📋 TaskService created successfully.");
    }
    
    public TaskService(TaskDAO taskDAO, UserDAO userDAO) {
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
    }
    
    /**
     * Create a new task with validation
     * @param title Task title
     * @param description Task description
     * @param priority Task priority
     * @param dueDate Due date (can be null)
     * @param assignedToId ID of user to assign task to (can be null)
     * @param createdById ID of user creating the task
     * @return Created task
     * @throws SQLException if database operation fails
     */
    public Task createTask(String title, String description, Priority priority, 
                          LocalDate dueDate, Integer assignedToId, int createdById) throws SQLException {
        
        // Validate input
        validateTaskInput(title, description, createdById);
        
        // Validate assigned user exists
        if (assignedToId != null) {
            Optional<User> assignedUser = userDAO.findById(assignedToId);
            if (assignedUser.isEmpty()) {
                throw new IllegalArgumentException("Assigned user with ID " + assignedToId + " not found");
            }
        }
        
        // Validate creator exists
        Optional<User> creator = userDAO.findById(createdById);
        if (creator.isEmpty()) {
            throw new IllegalArgumentException("Creator user with ID " + createdById + " not found");
        }
        
        // Validate due date
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        
        Task task = new Task(title, description, priority, TaskStatus.TODO, dueDate, assignedToId, createdById);
        return taskDAO.createTask(task);
    }
    
    /**
     * Find task by ID
     * @param id Task ID
     * @return Optional containing task if found
     * @throws SQLException if database operation fails
     */
    public Optional<Task> findTaskById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }
        return taskDAO.findById(id);
    }
    
    // GUI Helper Methods
    /**
     * Get task by ID - returns null if not found (for GUI compatibility)
     */
    public Task getTaskById(int id) throws SQLException {
        Optional<Task> taskOpt = findTaskById(id);
        return taskOpt.orElse(null);
    }
    
    /**
     * Create task with GUI-compatible signature
     */
    public Task createTask(String title, String description, Priority priority, String assignedToUsername, LocalDate dueDate) throws SQLException {
        // Find assigned user by username if provided
        Integer assignedToId = null;
        if (assignedToUsername != null && !assignedToUsername.trim().isEmpty() && !"Unassigned".equals(assignedToUsername.trim())) {
            Optional<User> assignedUser = userDAO.findByUsername(assignedToUsername.trim());
            if (assignedUser.isPresent()) {
                assignedToId = assignedUser.get().getId();
            }
        }
        
        // Get the admin user ID (in a real app, this would come from the logged-in user context)
        int createdById = 1; // Default to admin user
        try {
            // Try to get the first admin user from the database
            Optional<User> adminUser = userDAO.findByUsername("admin");
            if (adminUser.isPresent()) {
                createdById = adminUser.get().getId();
            }
        } catch (SQLException e) {
            // If we can't find admin user, use default ID 1
            createdById = 1;
        }
        
        return createTask(title, description, priority, dueDate, assignedToId, createdById);
    }
    
    /**
     * Get all tasks
     * @return List of all tasks
     * @throws SQLException if database operation fails
     */
    public List<Task> getAllTasks() throws SQLException {
        return taskDAO.findAll();
    }
    
    /**
     * Get tasks by status
     * @param status Task status
     * @return List of tasks with the specified status
     * @throws SQLException if database operation fails
     */
    public List<Task> getTasksByStatus(TaskStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return taskDAO.findByStatus(status);
    }
    
    /**
     * Get tasks assigned to a specific user
     * @param userId User ID
     * @return List of tasks assigned to the user
     * @throws SQLException if database operation fails
     */
    public List<Task> getTasksByAssignedUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Validate user exists
        Optional<User> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }
        
        return taskDAO.findByAssignedUser(userId);
    }
    
    /**
     * Get tasks created by a specific user
     * @param userId User ID
     * @return List of tasks created by the user
     * @throws SQLException if database operation fails
     */
    public List<Task> getTasksByCreator(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Validate user exists
        Optional<User> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }
        
        return taskDAO.findByCreator(userId);
    }
    
    /**
     * Get tasks visible to a specific user (assigned to them OR created by them)
     * This is for non-admin users who can only see their own tasks
     * @param userId User ID
     * @return List of tasks visible to the user
     * @throws SQLException if database operation fails
     */
    public List<Task> getTasksForUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Validate user exists
        Optional<User> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }
        
        return taskDAO.findTasksForUser(userId);
    }
    
    /**
     * Get tasks by priority
     * @param priority Task priority
     * @return List of tasks with the specified priority
     * @throws SQLException if database operation fails
     */
    public List<Task> getTasksByPriority(Priority priority) throws SQLException {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        return taskDAO.findByPriority(priority);
    }
    
    /**
     * Get overdue tasks
     * @return List of overdue tasks
     * @throws SQLException if database operation fails
     */
    public List<Task> getOverdueTasks() throws SQLException {
        return taskDAO.findOverdueTasks();
    }
    
    /**
     * Update task
     * @param task Task to update
     * @return true if update was successful
     * @throws SQLException if database operation fails
     */
    public boolean updateTask(Task task) throws SQLException {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        validateTaskInput(task.getTitle(), task.getDescription(), task.getCreatedBy());
        
        // Check if task exists
        Optional<Task> existingTask = taskDAO.findById(task.getId());
        if (existingTask.isEmpty()) {
            throw new IllegalArgumentException("Task with ID " + task.getId() + " not found");
        }
        
        // Validate assigned user exists
        if (task.getAssignedTo() != null) {
            Optional<User> assignedUser = userDAO.findById(task.getAssignedTo());
            if (assignedUser.isEmpty()) {
                throw new IllegalArgumentException("Assigned user with ID " + task.getAssignedTo() + " not found");
            }
        }
        
        return taskDAO.updateTask(task);
    }
    
    /**
     * Update task status
     * @param taskId Task ID
     * @param newStatus New status
     * @return true if update was successful
     * @throws SQLException if database operation fails
     */
    public boolean updateTaskStatus(int taskId, TaskStatus newStatus) throws SQLException {
        if (taskId <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }
        
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Check if task exists
        Optional<Task> task = taskDAO.findById(taskId);
        if (task.isEmpty()) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found");
        }
        
        return taskDAO.updateTaskStatus(taskId, newStatus);
    }
    
    /**
     * Assign task to user
     * @param taskId Task ID
     * @param userId User ID (null to unassign)
     * @return true if assignment was successful
     * @throws SQLException if database operation fails
     */
    public boolean assignTask(int taskId, Integer userId) throws SQLException {
        if (taskId <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }
        
        // Check if task exists
        Optional<Task> taskOpt = taskDAO.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found");
        }
        
        // Validate user exists (if not null)
        if (userId != null) {
            if (userId <= 0) {
                throw new IllegalArgumentException("User ID must be positive");
            }
            
            Optional<User> user = userDAO.findById(userId);
            if (user.isEmpty()) {
                throw new IllegalArgumentException("User with ID " + userId + " not found");
            }
        }
        
        Task task = taskOpt.get();
        task.setAssignedTo(userId);
        return taskDAO.updateTask(task);
    }
    
    /**
     * Delete task by ID
     * @param id Task ID
     * @return true if deletion was successful
     * @throws SQLException if database operation fails
     */
    public boolean deleteTask(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }
        
        // Check if task exists
        Optional<Task> task = taskDAO.findById(id);
        if (task.isEmpty()) {
            throw new IllegalArgumentException("Task with ID " + id + " not found");
        }
        
        return taskDAO.deleteTask(id);
    }
    
    /**
     * Move task to next status (TODO -> IN_PROGRESS -> COMPLETED)
     * @param taskId Task ID
     * @return true if status was updated
     * @throws SQLException if database operation fails
     */
    public boolean moveTaskToNextStatus(int taskId) throws SQLException {
        Optional<Task> taskOpt = findTaskById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found");
        }
        
        Task task = taskOpt.get();
        TaskStatus currentStatus = task.getStatus();
        TaskStatus nextStatus;
        
        switch (currentStatus) {
            case TODO:
                nextStatus = TaskStatus.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                nextStatus = TaskStatus.COMPLETED;
                break;
            case COMPLETED:
                throw new IllegalArgumentException("Task is already completed");
            default:
                throw new IllegalArgumentException("Unknown task status: " + currentStatus);
        }
        
        return updateTaskStatus(taskId, nextStatus);
    }
    
    /**
     * Move task to previous status (COMPLETED -> IN_PROGRESS -> TODO)
     * @param taskId Task ID
     * @return true if status was updated
     * @throws SQLException if database operation fails
     */
    public boolean moveTaskToPreviousStatus(int taskId) throws SQLException {
        Optional<Task> taskOpt = findTaskById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found");
        }
        
        Task task = taskOpt.get();
        TaskStatus currentStatus = task.getStatus();
        TaskStatus previousStatus;
        
        switch (currentStatus) {
            case COMPLETED:
                previousStatus = TaskStatus.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                previousStatus = TaskStatus.TODO;
                break;
            case TODO:
                throw new IllegalArgumentException("Task is already in the first status");
            default:
                throw new IllegalArgumentException("Unknown task status: " + currentStatus);
        }
        
        return updateTaskStatus(taskId, previousStatus);
    }
    
    /**
     * Get task count by status
     * @param status Task status
     * @return number of tasks with the specified status
     * @throws SQLException if database operation fails
     */
    public int getTaskCountByStatus(TaskStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return taskDAO.getTaskCountByStatus(status);
    }
    
    /**
     * Get task statistics
     * @return TaskStatistics object containing various counts
     * @throws SQLException if database operation fails
     */
    public TaskStatistics getTaskStatistics() throws SQLException {
        int todoCount = getTaskCountByStatus(TaskStatus.TODO);
        int inProgressCount = getTaskCountByStatus(TaskStatus.IN_PROGRESS);
        int completedCount = getTaskCountByStatus(TaskStatus.COMPLETED);
        int overdueCount = getOverdueTasks().size();
        
        return new TaskStatistics(todoCount, inProgressCount, completedCount, overdueCount);
    }
    
    /**
     * Validate task input
     * @param title Task title
     * @param description Task description
     * @param createdById Creator user ID
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTaskInput(String title, String description, int createdById) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        
        if (title.trim().length() > 200) {
            throw new IllegalArgumentException("Task title cannot exceed 200 characters");
        }
        
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("Task description cannot exceed 1000 characters");
        }
        
        if (createdById <= 0) {
            throw new IllegalArgumentException("Creator user ID must be positive");
        }
    }
    
    /**
     * Inner class for task statistics
     */
    public static class TaskStatistics {
        private final int todoCount;
        private final int inProgressCount;
        private final int completedCount;
        private final int overdueCount;
        
        public TaskStatistics(int todoCount, int inProgressCount, int completedCount, int overdueCount) {
            this.todoCount = todoCount;
            this.inProgressCount = inProgressCount;
            this.completedCount = completedCount;
            this.overdueCount = overdueCount;
        }
        
        public int getTodoCount() { return todoCount; }
        public int getInProgressCount() { return inProgressCount; }
        public int getCompletedCount() { return completedCount; }
        public int getOverdueCount() { return overdueCount; }
        public int getTotalCount() { return todoCount + inProgressCount + completedCount; }
        
        @Override
        public String toString() {
            return String.format("TaskStatistics{todo=%d, inProgress=%d, completed=%d, overdue=%d, total=%d}",
                    todoCount, inProgressCount, completedCount, overdueCount, getTotalCount());
        }
    }
}
