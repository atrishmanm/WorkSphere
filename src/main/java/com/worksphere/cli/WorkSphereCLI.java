package com.worksphere.cli;

import com.worksphere.model.*;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;
import com.worksphere.util.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Command Line Interface for the WorkSphere
 */
public class WorkSphereCLI {
    private final Scanner scanner;
    private final TaskService taskService;
    private final UserService userService;
    private User currentUser;
    
    public WorkSphereCLI() {
        this.scanner = new Scanner(System.in);
        this.taskService = new TaskService();
        this.userService = new UserService();
    }
    
    /**
     * Main entry point for the CLI application
     */
    public void start() {
        System.out.println("=".repeat(60));
        System.out.println("    WELCOME TO WorkSphere");
        System.out.println("=".repeat(60));
        
        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            System.err.println("❌ Cannot connect to database. Please check your configuration.");
            System.err.println("Update the database settings in src/main/resources/application.properties");
            return;
        }
        
        System.out.println("✅ Database connection successful!");
        System.out.println();
        
        try {
            // User login/selection
            selectUser();
            
            if (currentUser != null) {
                System.out.println("Welcome, " + currentUser.getFullName() + "!");
                showMainMenu();
            }
        } catch (Exception e) {
            System.err.println("❌ An error occurred: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * User selection/login process
     */
    private void selectUser() throws SQLException {
        while (currentUser == null) {
            System.out.println("\n--- USER SELECTION ---");
            System.out.println("1. Login with existing user");
            System.out.println("2. Create new user");
            System.out.println("3. List all users");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    loginUser();
                    break;
                case "2":
                    createNewUser();
                    break;
                case "3":
                    listAllUsers();
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        }
    }
    
    /**
     * Login with existing user
     */
    private void loginUser() throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("❌ Username cannot be empty.");
            return;
        }
        
        Optional<User> user = userService.findUserByUsername(username);
        if (user.isPresent()) {
            currentUser = user.get();
            System.out.println("✅ Login successful!");
        } else {
            System.out.println("❌ User not found. Please try again or create a new user.");
        }
    }
    
    /**
     * Create new user
     */
    private void createNewUser() throws SQLException {
        System.out.println("\n--- CREATE NEW USER ---");
        
        System.out.print("Enter username (3-20 chars, letters/numbers/underscore only): ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();
        
        try {
            User newUser = userService.createUser(username, email, fullName);
            System.out.println("✅ User created successfully! ID: " + newUser.getId());
            currentUser = newUser;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
    
    /**
     * List all users
     */
    private void listAllUsers() throws SQLException {
        List<User> users = userService.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        
        System.out.println("\n--- ALL USERS ---");
        System.out.printf("%-5s %-20s %-30s %-30s%n", "ID", "Username", "Email", "Full Name");
        System.out.println("-".repeat(85));
        
        for (User user : users) {
            System.out.printf("%-5d %-20s %-30s %-30s%n", 
                user.getId(), 
                user.getUsername(), 
                user.getEmail(), 
                user.getFullName());
        }
    }
    
    /**
     * Show main menu
     */
    private void showMainMenu() throws SQLException {
        while (true) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("MAIN MENU - Logged in as: " + currentUser.getUsername());
            System.out.println("=".repeat(60));
            
            // Show task statistics
            showTaskStatistics();
            
            System.out.println("\n--- TASK MANAGEMENT ---");
            System.out.println("1. View all tasks");
            System.out.println("2. View tasks by status");
            System.out.println("3. View my assigned tasks");
            System.out.println("4. View tasks by priority");
            System.out.println("5. View overdue tasks");
            System.out.println("6. Create new task");
            System.out.println("7. Edit task");
            System.out.println("8. Update task status");
            System.out.println("9. Assign task to user");
            System.out.println("10. Delete task");
            
            System.out.println("\n--- USER MANAGEMENT ---");
            System.out.println("11. View all users");
            System.out.println("12. Create new user");
            
            System.out.println("\n--- OTHER ---");
            System.out.println("13. Switch user");
            System.out.println("0. Exit");
            
            System.out.print("\nChoose an option: ");
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1": viewAllTasks(); break;
                    case "2": viewTasksByStatus(); break;
                    case "3": viewMyTasks(); break;
                    case "4": viewTasksByPriority(); break;
                    case "5": viewOverdueTasks(); break;
                    case "6": createNewTask(); break;
                    case "7": editTask(); break;
                    case "8": updateTaskStatus(); break;
                    case "9": assignTask(); break;
                    case "10": deleteTask(); break;
                    case "11": listAllUsers(); break;
                    case "12": createNewUser(); break;
                    case "13": switchUser(); return;
                    case "0": 
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("❌ Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Show task statistics
     */
    private void showTaskStatistics() throws SQLException {
        TaskService.TaskStatistics stats = taskService.getTaskStatistics();
        
        System.out.println("\n📊 TASK STATISTICS");
        System.out.printf("📝 To-Do: %d | 🔄 In Progress: %d | ✅ Completed: %d | ⚠️ Overdue: %d | 📋 Total: %d%n",
            stats.getTodoCount(),
            stats.getInProgressCount(),
            stats.getCompletedCount(),
            stats.getOverdueCount(),
            stats.getTotalCount());
    }
    
    /**
     * View all tasks
     */
    private void viewAllTasks() throws SQLException {
        List<Task> tasks = taskService.getAllTasks();
        displayTasks(tasks, "ALL TASKS");
    }
    
    /**
     * View tasks by status
     */
    private void viewTasksByStatus() throws SQLException {
        System.out.println("\nSelect status:");
        System.out.println("1. To-Do");
        System.out.println("2. In Progress");
        System.out.println("3. Completed");
        System.out.print("Choose status: ");
        
        String choice = scanner.nextLine().trim();
        TaskStatus status = null;
        
        switch (choice) {
            case "1": status = TaskStatus.TODO; break;
            case "2": status = TaskStatus.IN_PROGRESS; break;
            case "3": status = TaskStatus.COMPLETED; break;
            default:
                System.out.println("❌ Invalid status choice.");
                return;
        }
        
        List<Task> tasks = taskService.getTasksByStatus(status);
        displayTasks(tasks, "TASKS - " + status.getDisplayName().toUpperCase());
    }
    
    /**
     * View my assigned tasks
     */
    private void viewMyTasks() throws SQLException {
        List<Task> tasks = taskService.getTasksByAssignedUser(currentUser.getId());
        displayTasks(tasks, "MY ASSIGNED TASKS");
    }
    
    /**
     * View tasks by priority
     */
    private void viewTasksByPriority() throws SQLException {
        System.out.println("\nSelect priority:");
        System.out.println("1. Low");
        System.out.println("2. Medium");
        System.out.println("3. High");
        System.out.println("4. Urgent");
        System.out.print("Choose priority: ");
        
        String choice = scanner.nextLine().trim();
        Priority priority = null;
        
        switch (choice) {
            case "1": priority = Priority.LOW; break;
            case "2": priority = Priority.MEDIUM; break;
            case "3": priority = Priority.HIGH; break;
            case "4": priority = Priority.URGENT; break;
            default:
                System.out.println("❌ Invalid priority choice.");
                return;
        }
        
        List<Task> tasks = taskService.getTasksByPriority(priority);
        displayTasks(tasks, "TASKS - " + priority.getDisplayName().toUpperCase() + " PRIORITY");
    }
    
    /**
     * View overdue tasks
     */
    private void viewOverdueTasks() throws SQLException {
        List<Task> tasks = taskService.getOverdueTasks();
        displayTasks(tasks, "OVERDUE TASKS");
    }
    
    /**
     * Display tasks in a formatted table
     */
    private void displayTasks(List<Task> tasks, String title) {
        System.out.println("\n--- " + title + " ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        System.out.printf("%-5s %-30s %-10s %-12s %-15s %-20s %-15s%n", 
            "ID", "Title", "Priority", "Status", "Due Date", "Assigned To", "Created By");
        System.out.println("-".repeat(107));
        
        for (Task task : tasks) {
            String dueDate = task.getDueDate() != null ? task.getDueDate().toString() : "None";
            String assignedTo = task.getAssignedToUsername() != null ? task.getAssignedToUsername() : "Unassigned";
            String createdBy = task.getCreatedByUsername() != null ? task.getCreatedByUsername() : "Unknown";
            
            // Add emoji for status
            String statusDisplay = getStatusEmoji(task.getStatus()) + " " + task.getStatus().getDisplayName();
            
            // Add emoji for priority
            String priorityDisplay = getPriorityEmoji(task.getPriority()) + " " + task.getPriority().getDisplayName();
            
            System.out.printf("%-5d %-30s %-10s %-12s %-15s %-20s %-15s%n",
                task.getId(),
                truncateString(task.getTitle(), 30),
                priorityDisplay,
                statusDisplay,
                dueDate,
                truncateString(assignedTo, 20),
                truncateString(createdBy, 15));
        }
        
        // Option to view task details
        System.out.print("\nEnter task ID to view details (or press Enter to continue): ");
        String taskIdStr = scanner.nextLine().trim();
        
        if (!taskIdStr.isEmpty()) {
            try {
                int taskId = Integer.parseInt(taskIdStr);
                viewTaskDetails(taskId);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid task ID format.");
            } catch (SQLException e) {
                System.out.println("❌ Database error: " + e.getMessage());
            }
        }
    }
    
    /**
     * View detailed information about a specific task
     */
    private void viewTaskDetails(int taskId) throws SQLException {
        Optional<Task> taskOpt = taskService.findTaskById(taskId);
        
        if (taskOpt.isEmpty()) {
            System.out.println("❌ Task not found.");
            return;
        }
        
        Task task = taskOpt.get();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TASK DETAILS");
        System.out.println("=".repeat(60));
        System.out.println("ID: " + task.getId());
        System.out.println("Title: " + task.getTitle());
        System.out.println("Description: " + (task.getDescription() != null ? task.getDescription() : "No description"));
        System.out.println("Priority: " + getPriorityEmoji(task.getPriority()) + " " + task.getPriority().getDisplayName());
        System.out.println("Status: " + getStatusEmoji(task.getStatus()) + " " + task.getStatus().getDisplayName());
        System.out.println("Due Date: " + task.getDueDateStatus());
        System.out.println("Assigned To: " + (task.getAssignedToUsername() != null ? task.getAssignedToUsername() : "Unassigned"));
        System.out.println("Created By: " + (task.getCreatedByUsername() != null ? task.getCreatedByUsername() : "Unknown"));
        System.out.println("Created At: " + (task.getCreatedAt() != null ? task.getCreatedAt() : "Unknown"));
        System.out.println("Updated At: " + (task.getUpdatedAt() != null ? task.getUpdatedAt() : "Unknown"));
        
        if (task.isOverdue()) {
            System.out.println("⚠️ THIS TASK IS OVERDUE!");
        }
    }
    
    /**
     * Create new task
     */
    private void createNewTask() throws SQLException {
        System.out.println("\n--- CREATE NEW TASK ---");
        
        System.out.print("Enter task title: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("Enter task description (optional): ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) description = null;
        
        // Select priority
        System.out.println("Select priority:");
        System.out.println("1. Low");
        System.out.println("2. Medium (default)");
        System.out.println("3. High");
        System.out.println("4. Urgent");
        System.out.print("Choose priority (1-4): ");
        
        String priorityChoice = scanner.nextLine().trim();
        Priority priority = Priority.MEDIUM; // default
        
        switch (priorityChoice) {
            case "1": priority = Priority.LOW; break;
            case "2": 
            case "": priority = Priority.MEDIUM; break;
            case "3": priority = Priority.HIGH; break;
            case "4": priority = Priority.URGENT; break;
            default:
                System.out.println("Invalid choice, using Medium priority.");
        }
        
        // Due date
        System.out.print("Enter due date (YYYY-MM-DD, optional): ");
        String dueDateStr = scanner.nextLine().trim();
        LocalDate dueDate = null;
        
        if (!dueDateStr.isEmpty()) {
            try {
                dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Task will be created without due date.");
            }
        }
        
        // Assign to user
        System.out.print("Assign to user ID (optional, current user=" + currentUser.getId() + "): ");
        String assignedToStr = scanner.nextLine().trim();
        Integer assignedTo = null;
        
        if (!assignedToStr.isEmpty()) {
            try {
                assignedTo = Integer.parseInt(assignedToStr);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid user ID format. Task will be unassigned.");
            }
        }
        
        try {
            Task newTask = taskService.createTask(title, description, priority, dueDate, assignedTo, currentUser.getId());
            System.out.println("✅ Task created successfully! ID: " + newTask.getId());
        } catch (Exception e) {
            System.out.println("❌ Error creating task: " + e.getMessage());
        }
    }
    
    /**
     * Edit task
     */
    private void editTask() throws SQLException {
        System.out.print("Enter task ID to edit: ");
        String taskIdStr = scanner.nextLine().trim();
        
        try {
            int taskId = Integer.parseInt(taskIdStr);
            Optional<Task> taskOpt = taskService.findTaskById(taskId);
            
            if (taskOpt.isEmpty()) {
                System.out.println("❌ Task not found.");
                return;
            }
            
            Task task = taskOpt.get();
            
            System.out.println("\n--- EDITING TASK: " + task.getTitle() + " ---");
            
            System.out.print("Enter new title (current: " + task.getTitle() + "): ");
            String newTitle = scanner.nextLine().trim();
            if (!newTitle.isEmpty()) {
                task.setTitle(newTitle);
            }
            
            System.out.print("Enter new description (current: " + (task.getDescription() != null ? task.getDescription() : "None") + "): ");
            String newDescription = scanner.nextLine().trim();
            if (!newDescription.isEmpty()) {
                task.setDescription(newDescription);
            }
            
            // Update priority
            System.out.println("Select new priority (current: " + task.getPriority().getDisplayName() + "):");
            System.out.println("1. Low");
            System.out.println("2. Medium");
            System.out.println("3. High");
            System.out.println("4. Urgent");
            System.out.println("0. Keep current");
            System.out.print("Choose priority: ");
            
            String priorityChoice = scanner.nextLine().trim();
            switch (priorityChoice) {
                case "1": task.setPriority(Priority.LOW); break;
                case "2": task.setPriority(Priority.MEDIUM); break;
                case "3": task.setPriority(Priority.HIGH); break;
                case "4": task.setPriority(Priority.URGENT); break;
                // case "0" or default: keep current
            }
            
            // Update due date
            System.out.print("Enter new due date (YYYY-MM-DD, current: " + 
                (task.getDueDate() != null ? task.getDueDate() : "None") + ", 'none' to remove): ");
            String dueDateStr = scanner.nextLine().trim();
            
            if (!dueDateStr.isEmpty()) {
                if (dueDateStr.equalsIgnoreCase("none")) {
                    task.setDueDate(null);
                } else {
                    try {
                        LocalDate newDueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                        task.setDueDate(newDueDate);
                    } catch (DateTimeParseException e) {
                        System.out.println("❌ Invalid date format. Due date not changed.");
                    }
                }
            }
            
            boolean success = taskService.updateTask(task);
            if (success) {
                System.out.println("✅ Task updated successfully!");
            } else {
                System.out.println("❌ Failed to update task.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID format.");
        }
    }
    
    /**
     * Update task status
     */
    private void updateTaskStatus() throws SQLException {
        System.out.print("Enter task ID: ");
        String taskIdStr = scanner.nextLine().trim();
        
        try {
            int taskId = Integer.parseInt(taskIdStr);
            Optional<Task> taskOpt = taskService.findTaskById(taskId);
            
            if (taskOpt.isEmpty()) {
                System.out.println("❌ Task not found.");
                return;
            }
            
            Task task = taskOpt.get();
            
            System.out.println("\nCurrent status: " + task.getStatus().getDisplayName());
            System.out.println("Select new status:");
            System.out.println("1. To-Do");
            System.out.println("2. In Progress");
            System.out.println("3. Completed");
            System.out.println("4. Move to next status");
            System.out.println("5. Move to previous status");
            System.out.print("Choose option: ");
            
            String choice = scanner.nextLine().trim();
            boolean success = false;
            
            switch (choice) {
                case "1":
                    success = taskService.updateTaskStatus(taskId, TaskStatus.TODO);
                    break;
                case "2":
                    success = taskService.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);
                    break;
                case "3":
                    success = taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
                    break;
                case "4":
                    try {
                        success = taskService.moveTaskToNextStatus(taskId);
                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ " + e.getMessage());
                        return;
                    }
                    break;
                case "5":
                    try {
                        success = taskService.moveTaskToPreviousStatus(taskId);
                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ " + e.getMessage());
                        return;
                    }
                    break;
                default:
                    System.out.println("❌ Invalid choice.");
                    return;
            }
            
            if (success) {
                System.out.println("✅ Task status updated successfully!");
            } else {
                System.out.println("❌ Failed to update task status.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID format.");
        }
    }
    
    /**
     * Assign task to user
     */
    private void assignTask() throws SQLException {
        System.out.print("Enter task ID: ");
        String taskIdStr = scanner.nextLine().trim();
        
        try {
            int taskId = Integer.parseInt(taskIdStr);
            Optional<Task> taskOpt = taskService.findTaskById(taskId);
            
            if (taskOpt.isEmpty()) {
                System.out.println("❌ Task not found.");
                return;
            }
            
            Task task = taskOpt.get();
            System.out.println("Task: " + task.getTitle());
            System.out.println("Currently assigned to: " + 
                (task.getAssignedToUsername() != null ? task.getAssignedToUsername() : "Unassigned"));
            
            // Show available users
            List<User> users = userService.getAllUsers();
            System.out.println("\nAvailable users:");
            for (User user : users) {
                System.out.println(user.getId() + ". " + user.getUsername() + " (" + user.getFullName() + ")");
            }
            
            System.out.print("Enter user ID to assign (or 'none' to unassign): ");
            String userIdStr = scanner.nextLine().trim();
            
            Integer userId = null;
            if (!userIdStr.equalsIgnoreCase("none") && !userIdStr.isEmpty()) {
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid user ID format.");
                    return;
                }
            }
            
            boolean success = taskService.assignTask(taskId, userId);
            if (success) {
                System.out.println("✅ Task assignment updated successfully!");
            } else {
                System.out.println("❌ Failed to update task assignment.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID format.");
        }
    }
    
    /**
     * Delete task
     */
    private void deleteTask() throws SQLException {
        System.out.print("Enter task ID to delete: ");
        String taskIdStr = scanner.nextLine().trim();
        
        try {
            int taskId = Integer.parseInt(taskIdStr);
            Optional<Task> taskOpt = taskService.findTaskById(taskId);
            
            if (taskOpt.isEmpty()) {
                System.out.println("❌ Task not found.");
                return;
            }
            
            Task task = taskOpt.get();
            System.out.println("Task to delete: " + task.getTitle());
            System.out.print("Are you sure you want to delete this task? (y/N): ");
            
            String confirmation = scanner.nextLine().trim().toLowerCase();
            if (confirmation.equals("y") || confirmation.equals("yes")) {
                boolean success = taskService.deleteTask(taskId);
                if (success) {
                    System.out.println("✅ Task deleted successfully!");
                } else {
                    System.out.println("❌ Failed to delete task.");
                }
            } else {
                System.out.println("Task deletion cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID format.");
        }
    }
    
    /**
     * Switch user
     */
    private void switchUser() {
        currentUser = null;
        System.out.println("Logged out successfully.");
    }
    
    /**
     * Get emoji for task status
     */
    private String getStatusEmoji(TaskStatus status) {
        switch (status) {
            case TODO: return "📝";
            case IN_PROGRESS: return "🔄";
            case COMPLETED: return "✅";
            default: return "❓";
        }
    }
    
    /**
     * Get emoji for task priority
     */
    private String getPriorityEmoji(Priority priority) {
        switch (priority) {
            case LOW: return "🟢";
            case MEDIUM: return "🟡";
            case HIGH: return "🟠";
            case URGENT: return "🔴";
            default: return "⚪";
        }
    }
    
    /**
     * Truncate string to specified length
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
