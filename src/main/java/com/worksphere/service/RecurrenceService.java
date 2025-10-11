package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing recurring tasks and generating task instances
 */
public class RecurrenceService {
    
    private TaskDAO taskDAO;
    
    public RecurrenceService() {
        this.taskDAO = new TaskDAO();
    }
    
    /**
     * Create a recurring task with specified recurrence rule
     */
    public Task createRecurringTask(Task templateTask, RecurrenceRule rule) throws SQLException {
        // Set the recurrence rule on the task
        templateTask.setRecurrenceRule(rule.toString());
        
        // Save the template task
        Task savedTask = taskDAO.createTask(templateTask);
        
        // Generate initial instances
        generateTaskInstances(savedTask, rule, LocalDate.now(), LocalDate.now().plusMonths(3));
        
        return savedTask;
    }
    
    /**
     * Update recurrence rule for an existing recurring task
     */
    public void updateRecurrenceRule(int taskId, RecurrenceRule newRule) throws SQLException {
        Optional<Task> taskOpt = taskDAO.findById(taskId);
        if (taskOpt.isPresent() && taskOpt.get().getRecurrenceRule() != null) {
            Task task = taskOpt.get();
            // Update the template task
            task.setRecurrenceRule(newRule.toString());
            taskDAO.updateTask(task);
            
            // Remove future instances and regenerate
            removeFutureInstances(taskId);
            generateTaskInstances(task, newRule, LocalDate.now(), LocalDate.now().plusMonths(3));
        }
    }
    
    /**
     * Generate task instances for a recurring task within the specified date range
     */
    public List<Task> generateTaskInstances(Task templateTask, RecurrenceRule rule, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Task> generatedTasks = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        int instanceCount = 0;
        
        while (!currentDate.isAfter(endDate) && (rule.maxOccurrences == 0 || instanceCount < rule.maxOccurrences)) {
            LocalDate nextDate = getNextOccurrence(currentDate, rule);
            
            if (nextDate == null || nextDate.isAfter(endDate)) {
                break;
            }
            
            // Check if instance already exists
            if (!instanceExists(templateTask.getId(), nextDate)) {
                Task instance = createTaskInstance(templateTask, nextDate);
                Task savedInstance = taskDAO.createTask(instance);
                generatedTasks.add(savedInstance);
                instanceCount++;
            }
            
            currentDate = nextDate.plusDays(1);
        }
        
        return generatedTasks;
    }
    
    /**
     * Process recurring tasks - generate new instances and handle completed ones
     */
    public void processRecurringTasks() throws SQLException {
        List<Task> recurringTasks = getRecurringTasks();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusMonths(3);
        
        for (Task task : recurringTasks) {
            RecurrenceRule rule = RecurrenceRule.fromString(task.getRecurrenceRule());
            if (rule != null) {
                generateTaskInstances(task, rule, today, futureDate);
            }
        }
    }
    
    /**
     * Get all tasks that are marked as recurring (have recurrence rules)
     */
    public List<Task> getRecurringTasks() throws SQLException {
        return taskDAO.findAll().stream()
            .filter(task -> task.getRecurrenceRule() != null && !task.getRecurrenceRule().isEmpty())
            .filter(task -> task.getParentTaskId() == 0) // Only template tasks, not instances
            .collect(Collectors.toList());
    }
    
    /**
     * Get all instances of a recurring task
     */
    public List<Task> getTaskInstances(int parentTaskId) throws SQLException {
        return taskDAO.findAll().stream()
            .filter(task -> task.getParentTaskId() == parentTaskId)
            .sorted(Comparator.comparing(Task::getDueDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Delete a recurring task and all its instances
     */
    public void deleteRecurringTask(int taskId) throws SQLException {
        // Delete all instances first
        List<Task> instances = getTaskInstances(taskId);
        for (Task instance : instances) {
            taskDAO.deleteTask(instance.getId());
        }
        
        // Delete the template task
        taskDAO.deleteTask(taskId);
    }
    
    /**
     * Stop recurrence for a task (keep existing instances but don't generate new ones)
     */
    public void stopRecurrence(int taskId) throws SQLException {
        Optional<Task> taskOpt = taskDAO.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setRecurrenceRule(null);
            taskDAO.updateTask(task);
        }
    }
    
    private LocalDate getNextOccurrence(LocalDate fromDate, RecurrenceRule rule) {
        switch (rule.frequency) {
            case DAILY:
                return fromDate.plusDays(rule.interval);
                
            case WEEKLY:
                if (rule.daysOfWeek != null && !rule.daysOfWeek.isEmpty()) {
                    return getNextWeeklyOccurrence(fromDate, rule);
                } else {
                    return fromDate.plusWeeks(rule.interval);
                }
                
            case MONTHLY:
                if (rule.dayOfMonth > 0) {
                    return getNextMonthlyByDay(fromDate, rule);
                } else {
                    return fromDate.plusMonths(rule.interval);
                }
                
            case YEARLY:
                return fromDate.plusYears(rule.interval);
                
            default:
                return null;
        }
    }
    
    private LocalDate getNextWeeklyOccurrence(LocalDate fromDate, RecurrenceRule rule) {
        LocalDate nextDate = fromDate;
        
        // Find the next occurrence within the current week
        for (int i = 0; i < 7; i++) {
            nextDate = nextDate.plusDays(1);
            if (rule.daysOfWeek.contains(nextDate.getDayOfWeek())) {
                return nextDate;
            }
        }
        
        // If no occurrence in current week, move to next interval
        nextDate = fromDate.plusWeeks(rule.interval);
        DayOfWeek firstDay = rule.daysOfWeek.stream().min(Comparator.naturalOrder()).orElse(DayOfWeek.MONDAY);
        
        while (nextDate.getDayOfWeek() != firstDay) {
            nextDate = nextDate.plusDays(1);
        }
        
        return nextDate;
    }
    
    private LocalDate getNextMonthlyByDay(LocalDate fromDate, RecurrenceRule rule) {
        LocalDate nextMonth = fromDate.plusMonths(rule.interval);
        
        // Handle end of month edge cases
        int targetDay = Math.min(rule.dayOfMonth, nextMonth.lengthOfMonth());
        
        return nextMonth.withDayOfMonth(targetDay);
    }
    
    private boolean instanceExists(int parentTaskId, LocalDate dueDate) throws SQLException {
        return taskDAO.findAll().stream()
            .anyMatch(task -> task.getParentTaskId() == parentTaskId && 
                             task.getDueDate() != null && 
                             task.getDueDate().equals(dueDate));
    }
    
    private Task createTaskInstance(Task template, LocalDate dueDate) {
        Task instance = new Task();
        instance.setTitle(template.getTitle());
        instance.setDescription(template.getDescription());
        instance.setPriority(template.getPriority());
        instance.setStatus(TaskStatus.TODO);
        instance.setDueDate(dueDate);
        instance.setCreatedAt(LocalDateTime.now());
        instance.setAssignedTo(template.getAssignedTo());
        instance.setParentTaskId(template.getId());
        instance.setCategoryId(template.getCategoryId());
        instance.setEstimatedMinutes(template.getEstimatedMinutes());
        instance.setTags(new ArrayList<>(template.getTags()));
        
        return instance;
    }
    
    private void removeFutureInstances(int parentTaskId) throws SQLException {
        LocalDate today = LocalDate.now();
        List<Task> futureInstances = taskDAO.findAll().stream()
            .filter(task -> task.getParentTaskId() == parentTaskId)
            .filter(task -> task.getDueDate() != null && task.getDueDate().isAfter(today))
            .filter(task -> task.getStatus() == TaskStatus.TODO) // Don't remove completed/in-progress tasks
            .collect(Collectors.toList());
        
        for (Task instance : futureInstances) {
            taskDAO.deleteTask(instance.getId());
        }
    }
    
    // Recurrence rule class
    public static class RecurrenceRule {
        public RecurrenceFrequency frequency;
        public int interval = 1; // Every X units (1 = every day/week/month, 2 = every other day/week/month)
        public Set<DayOfWeek> daysOfWeek; // For weekly recurrence
        public int dayOfMonth; // For monthly recurrence (1-31)
        public int maxOccurrences; // 0 = unlimited
        public LocalDate endDate; // Optional end date
        
        public RecurrenceRule(RecurrenceFrequency frequency) {
            this.frequency = frequency;
        }
        
        // Builder pattern methods
        public RecurrenceRule every(int interval) {
            this.interval = interval;
            return this;
        }
        
        public RecurrenceRule onDays(DayOfWeek... days) {
            this.daysOfWeek = new HashSet<>(Arrays.asList(days));
            return this;
        }
        
        public RecurrenceRule onDayOfMonth(int day) {
            this.dayOfMonth = day;
            return this;
        }
        
        public RecurrenceRule limitTo(int occurrences) {
            this.maxOccurrences = occurrences;
            return this;
        }
        
        public RecurrenceRule until(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("FREQ=").append(frequency.name());
            if (interval > 1) {
                sb.append(";INTERVAL=").append(interval);
            }
            if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                sb.append(";BYDAY=");
                sb.append(daysOfWeek.stream()
                    .map(day -> day.name().substring(0, 2))
                    .collect(Collectors.joining(",")));
            }
            if (dayOfMonth > 0) {
                sb.append(";BYMONTHDAY=").append(dayOfMonth);
            }
            if (maxOccurrences > 0) {
                sb.append(";COUNT=").append(maxOccurrences);
            }
            if (endDate != null) {
                sb.append(";UNTIL=").append(endDate.toString());
            }
            return sb.toString();
        }
        
        public static RecurrenceRule fromString(String ruleString) {
            if (ruleString == null || ruleString.isEmpty()) {
                return null;
            }
            
            try {
                String[] parts = ruleString.split(";");
                RecurrenceRule rule = null;
                
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length != 2) continue;
                    
                    String key = keyValue[0];
                    String value = keyValue[1];
                    
                    switch (key) {
                        case "FREQ":
                            rule = new RecurrenceRule(RecurrenceFrequency.valueOf(value));
                            break;
                        case "INTERVAL":
                            if (rule != null) rule.interval = Integer.parseInt(value);
                            break;
                        case "BYDAY":
                            if (rule != null) {
                                rule.daysOfWeek = Arrays.stream(value.split(","))
                                    .map(day -> DayOfWeek.valueOf(day + day.substring(1).toLowerCase()))
                                    .collect(Collectors.toSet());
                            }
                            break;
                        case "BYMONTHDAY":
                            if (rule != null) rule.dayOfMonth = Integer.parseInt(value);
                            break;
                        case "COUNT":
                            if (rule != null) rule.maxOccurrences = Integer.parseInt(value);
                            break;
                        case "UNTIL":
                            if (rule != null) rule.endDate = LocalDate.parse(value);
                            break;
                    }
                }
                
                return rule;
            } catch (Exception e) {
                System.err.println("Error parsing recurrence rule: " + ruleString);
                return null;
            }
        }
        
        // Static factory methods for common patterns
        public static RecurrenceRule daily() {
            return new RecurrenceRule(RecurrenceFrequency.DAILY);
        }
        
        public static RecurrenceRule weekly() {
            return new RecurrenceRule(RecurrenceFrequency.WEEKLY);
        }
        
        public static RecurrenceRule monthly() {
            return new RecurrenceRule(RecurrenceFrequency.MONTHLY);
        }
        
        public static RecurrenceRule yearly() {
            return new RecurrenceRule(RecurrenceFrequency.YEARLY);
        }
        
        public static RecurrenceRule weekdays() {
            return new RecurrenceRule(RecurrenceFrequency.WEEKLY)
                .onDays(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                       DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        }
    }
    
    public enum RecurrenceFrequency {
        DAILY,
        WEEKLY, 
        MONTHLY,
        YEARLY
    }
}