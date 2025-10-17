package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.dao.UserDAO;
import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing and exporting tasks via CSV files
 */
public class CSVImportExportService {
    private final TaskDAO taskDAO;
    private final UserDAO userDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public CSVImportExportService() {
        this.taskDAO = new TaskDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Generate a sample CSV template file that users can modify
     * @param filePath Path where to save the template
     * @return true if generation was successful
     */
    public boolean generateSampleTemplate(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("title,description,priority,status,due_date,assigned_to_username,estimated_hours");
            
            // Write sample rows
            writer.println("\"Setup Development Environment\",\"Install IDE and configure tools\",\"HIGH\",\"TODO\",\"2025-10-25\",\"john_doe\",\"3\"");
            writer.println("\"Design Database Schema\",\"Create ER diagram and define tables\",\"URGENT\",\"IN_PROGRESS\",\"2025-10-22\",\"jane_smith\",\"5\"");
            writer.println("\"Write Unit Tests\",\"Implement comprehensive test suite\",\"MEDIUM\",\"TODO\",\"2025-10-30\",\"mike_wilson\",\"8\"");
            writer.println("\"Code Review Guidelines\",\"Document best practices and standards\",\"LOW\",\"COMPLETED\",\"2025-10-15\",\"john_doe\",\"2\"");
            writer.println("\"API Documentation\",\"Create REST API documentation with examples\",\"MEDIUM\",\"IN_PROGRESS\",\"2025-10-28\",\"jane_smith\",\"4\"");
            
            System.out.println("✅ Sample CSV template generated at: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error generating CSV template: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Import tasks from a CSV file
     * @param filePath Path to the CSV file
     * @param createdBy User ID who is importing the tasks
     * @return ImportResult containing success count, errors, and messages
     */
    public ImportResult importTasksFromCSV(String filePath, int createdBy) {
        ImportResult result = new ImportResult();
        int lineNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Task task = parseCSVLine(line, createdBy);
                    if (task != null) {
                        Task createdTask = taskDAO.createTask(task);
                        if (createdTask != null && createdTask.getId() > 0) {
                            result.successCount++;
                            result.importedTasks.add(createdTask);
                        } else {
                            result.errorCount++;
                            result.errors.add("Line " + lineNumber + ": Failed to create task in database");
                        }
                    }
                } catch (Exception e) {
                    result.errorCount++;
                    result.errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            result.totalLines = lineNumber - 1; // Exclude header
            
        } catch (IOException e) {
            result.errors.add("File reading error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Parse a single CSV line into a Task object
     * @param line CSV line to parse
     * @param createdBy User ID who is creating the task
     * @return Task object or null if parsing fails
     */
    private Task parseCSVLine(String line, int createdBy) throws Exception {
        List<String> fields = parseCSVFields(line);
        
        if (fields.size() < 7) {
            throw new Exception("Insufficient fields. Expected 7 fields: title, description, priority, status, due_date, assigned_to_username, estimated_hours");
        }
        
        // Extract fields
        String title = fields.get(0).trim();
        String description = fields.get(1).trim();
        String priorityStr = fields.get(2).trim().toUpperCase();
        String statusStr = fields.get(3).trim().toUpperCase();
        String dueDateStr = fields.get(4).trim();
        String assignedToUsername = fields.get(5).trim();
        String estimatedHoursStr = fields.get(6).trim();
        
        // Validate title
        if (title.isEmpty()) {
            throw new Exception("Title cannot be empty");
        }
        
        // Parse priority
        Priority priority;
        try {
            priority = Priority.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid priority: " + priorityStr + ". Must be LOW, MEDIUM, HIGH, or URGENT");
        }
        
        // Parse status
        TaskStatus status;
        try {
            status = TaskStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid status: " + statusStr + ". Must be TODO, IN_PROGRESS, or COMPLETED");
        }
        
        // Parse due date
        LocalDate dueDate = null;
        if (!dueDateStr.isEmpty()) {
            try {
                dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new Exception("Invalid date format: " + dueDateStr + ". Expected format: yyyy-MM-dd");
            }
        }
        
        // Find assigned user
        Integer assignedTo = null;
        if (!assignedToUsername.isEmpty()) {
            try {
                var userOpt = userDAO.findByUsername(assignedToUsername);
                if (userOpt.isPresent()) {
                    assignedTo = userOpt.get().getId();
                } else {
                    throw new Exception("User not found: " + assignedToUsername);
                }
            } catch (Exception e) {
                throw new Exception("Error finding user: " + assignedToUsername + " - " + e.getMessage());
            }
        }
        
        // Parse estimated hours
        int estimatedMinutes = 0;
        if (!estimatedHoursStr.isEmpty()) {
            try {
                double hours = Double.parseDouble(estimatedHoursStr);
                estimatedMinutes = (int) (hours * 60);
            } catch (NumberFormatException e) {
                throw new Exception("Invalid estimated hours: " + estimatedHoursStr);
            }
        }
        
        // Create task
        Task task = new Task(title, description, priority, status, dueDate, assignedTo, createdBy);
        task.setEstimatedMinutes(estimatedMinutes);
        
        return task;
    }
    
    /**
     * Parse CSV fields handling quoted strings properly
     * @param line CSV line
     * @return List of fields
     */
    private List<String> parseCSVFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString());
        
        return fields;
    }
    
    /**
     * Export tasks to a CSV file
     * @param tasks List of tasks to export
     * @param filePath Path where to save the CSV file
     * @return true if export was successful
     */
    public boolean exportTasksToCSV(List<Task> tasks, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("id,title,description,priority,status,due_date,assigned_to,created_by,estimated_hours,actual_hours,completed_at,created_at");
            
            // Write task data
            for (Task task : tasks) {
                writer.print(task.getId());
                writer.print(",\"" + escapeCsv(task.getTitle()) + "\"");
                writer.print(",\"" + escapeCsv(task.getDescription() != null ? task.getDescription() : "") + "\"");
                writer.print("," + task.getPriority());
                writer.print("," + task.getStatus());
                writer.print("," + (task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : ""));
                writer.print(",\"" + (task.getAssignedToUsername() != null ? task.getAssignedToUsername() : "") + "\"");
                writer.print(",\"" + (task.getCreatedByUsername() != null ? task.getCreatedByUsername() : "") + "\"");
                writer.print("," + String.format("%.2f", task.getEstimatedMinutes() / 60.0));
                writer.print("," + String.format("%.2f", task.getActualMinutes() / 60.0));
                writer.print("," + (task.getCompletedAt() != null ? task.getCompletedAt() : ""));
                writer.println("," + (task.getCreatedAt() != null ? task.getCreatedAt() : ""));
            }
            
            System.out.println("✅ Successfully exported " + tasks.size() + " tasks to: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error exporting tasks to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Escape CSV special characters
     * @param value String to escape
     * @return Escaped string
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Result class for import operations
     */
    public static class ImportResult {
        public int totalLines = 0;
        public int successCount = 0;
        public int errorCount = 0;
        public List<String> errors = new ArrayList<>();
        public List<Task> importedTasks = new ArrayList<>();
        
        public boolean hasErrors() {
            return errorCount > 0;
        }
        
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Import completed:\n");
            sb.append("  Total rows processed: ").append(totalLines).append("\n");
            sb.append("  Successfully imported: ").append(successCount).append("\n");
            sb.append("  Errors: ").append(errorCount).append("\n");
            
            if (hasErrors()) {
                sb.append("\nErrors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}
