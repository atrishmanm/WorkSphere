package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.dao.CategoryDAO;
import com.worksphere.model.Task;
import com.worksphere.model.Category;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.Priority;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating comprehensive task analytics and productivity metrics
 */
public class AnalyticsService {
    private final TaskDAO taskDAO;
    private final CategoryDAO categoryDAO;
    
    public AnalyticsService() {
        this.taskDAO = new TaskDAO();
        this.categoryDAO = new CategoryDAO();
    }
    
    /**
     * Get overall productivity metrics
     */
    public ProductivityMetrics getProductivityMetrics(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        List<Task> filteredTasks = filterTasksByDateRange(allTasks, startDate, endDate);
        
        ProductivityMetrics metrics = new ProductivityMetrics();
        
        // Basic counts
        metrics.totalTasks = filteredTasks.size();
        metrics.completedTasks = (int) filteredTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
            .count();
        metrics.inProgressTasks = (int) filteredTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
            .count();
        metrics.todoTasks = (int) filteredTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.TODO)
            .count();
        
        // Completion rate
        metrics.completionRate = metrics.totalTasks > 0 ? 
            (double) metrics.completedTasks / metrics.totalTasks : 0.0;
        
        // Average completion time
        List<Task> completedTasksWithTime = filteredTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.COMPLETED && 
                           task.getCreatedAt() != null && 
                           task.getCompletedAt() != null)
            .collect(Collectors.toList());
        
        if (!completedTasksWithTime.isEmpty()) {
            double avgDays = completedTasksWithTime.stream()
                .mapToLong(task -> ChronoUnit.DAYS.between(task.getCreatedAt(), task.getCompletedAt()))
                .average()
                .orElse(0.0);
            metrics.averageCompletionDays = avgDays;
        }
        
        // Time tracking metrics
        metrics.totalTimeSpent = filteredTasks.stream()
            .mapToInt(Task::getActualMinutes)
            .sum();
        
        metrics.totalEstimatedTime = filteredTasks.stream()
            .mapToInt(Task::getEstimatedMinutes)
            .sum();
        
        // Time efficiency
        if (metrics.totalEstimatedTime > 0) {
            metrics.timeEfficiency = (double) metrics.totalEstimatedTime / metrics.totalTimeSpent;
        }
        
        return metrics;
    }
    
    /**
     * Get completion trend data for charts
     */
    public List<CompletionTrendData> getCompletionTrend(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        List<CompletionTrendData> trendData = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            final LocalDate dateToCheck = currentDate;
            
            long completedCount = allTasks.stream()
                .filter(task -> task.getCompletedAt() != null && 
                               task.getCompletedAt().toLocalDate().equals(dateToCheck))
                .count();
            
            long createdCount = allTasks.stream()
                .filter(task -> task.getCreatedAt() != null && 
                               task.getCreatedAt().toLocalDate().equals(dateToCheck))
                .count();
            
            trendData.add(new CompletionTrendData(currentDate, (int)createdCount, (int)completedCount));
            currentDate = currentDate.plusDays(1);
        }
        
        return trendData;
    }
    
    /**
     * Get priority distribution data
     */
    public Map<Priority, Integer> getPriorityDistribution() throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        Map<Priority, Integer> distribution = new HashMap<>();
        
        for (Priority priority : Priority.values()) {
            long count = allTasks.stream()
                .filter(task -> task.getPriority() == priority)
                .count();
            distribution.put(priority, (int)count);
        }
        
        return distribution;
    }
    
    /**
     * Get category performance metrics
     */
    public List<CategoryPerformance> getCategoryPerformance() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        List<CategoryPerformance> performance = new ArrayList<>();
        
        for (Category category : categories) {
            List<Task> categoryTasks = taskDAO.findByCategory(category.getId());
            
            CategoryPerformance perf = new CategoryPerformance();
            perf.category = category;
            perf.totalTasks = categoryTasks.size();
            perf.completedTasks = (int) categoryTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
            perf.completionRate = perf.totalTasks > 0 ? 
                (double) perf.completedTasks / perf.totalTasks : 0.0;
            perf.totalTimeSpent = categoryTasks.stream()
                .mapToInt(Task::getActualMinutes)
                .sum();
            perf.averageTimePerTask = perf.totalTasks > 0 ? 
                (double) perf.totalTimeSpent / perf.totalTasks : 0.0;
            
            performance.add(perf);
        }
        
        return performance;
    }
    
    /**
     * Get productive hours analysis
     */
    public Map<Integer, Integer> getProductiveHours() throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        Map<Integer, Integer> hourlyDistribution = new HashMap<>();
        
        // Initialize all hours
        for (int hour = 0; hour < 24; hour++) {
            hourlyDistribution.put(hour, 0);
        }
        
        // Count completions by hour
        allTasks.stream()
            .filter(task -> task.getCompletedAt() != null)
            .forEach(task -> {
                int hour = task.getCompletedAt().getHour();
                hourlyDistribution.put(hour, hourlyDistribution.get(hour) + 1);
            });
        
        return hourlyDistribution;
    }
    
    /**
     * Get overdue tasks analysis
     */
    public OverdueAnalysis getOverdueAnalysis() throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        OverdueAnalysis analysis = new OverdueAnalysis();
        
        LocalDate today = LocalDate.now();
        
        analysis.overdueTasks = (int) allTasks.stream()
            .filter(Task::isOverdue)
            .count();
        
        analysis.dueTodayTasks = (int) allTasks.stream()
            .filter(task -> task.getDueDate() != null && 
                           task.getDueDate().equals(today) && 
                           task.getStatus() != TaskStatus.COMPLETED)
            .count();
        
        analysis.dueThisWeekTasks = (int) allTasks.stream()
            .filter(task -> task.getDueDate() != null && 
                           task.getDueDate().isAfter(today) &&
                           task.getDueDate().isBefore(today.plusWeeks(1)) &&
                           task.getStatus() != TaskStatus.COMPLETED)
            .count();
        
        // Average overdue days
        List<Task> overdueTasks = allTasks.stream()
            .filter(Task::isOverdue)
            .collect(Collectors.toList());
        
        if (!overdueTasks.isEmpty()) {
            double avgOverdueDays = overdueTasks.stream()
                .mapToLong(task -> ChronoUnit.DAYS.between(task.getDueDate(), today))
                .average()
                .orElse(0.0);
            analysis.averageOverdueDays = avgOverdueDays;
        }
        
        return analysis;
    }
    
    /**
     * Get team performance metrics
     */
    public List<UserPerformance> getUserPerformance() throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        Map<Integer, UserPerformance> userPerformanceMap = new HashMap<>();
        
        for (Task task : allTasks) {
            Integer userId = task.getAssignedTo();
            if (userId == null) continue;
            
            UserPerformance perf = userPerformanceMap.computeIfAbsent(userId, id -> {
                UserPerformance p = new UserPerformance();
                p.userId = id;
                p.username = task.getAssignedToUsername();
                return p;
            });
            
            perf.assignedTasks++;
            if (task.getStatus() == TaskStatus.COMPLETED) {
                perf.completedTasks++;
            }
            perf.totalTimeSpent += task.getActualMinutes();
        }
        
        // Calculate completion rates
        for (UserPerformance perf : userPerformanceMap.values()) {
            perf.completionRate = perf.assignedTasks > 0 ? 
                (double) perf.completedTasks / perf.assignedTasks : 0.0;
        }
        
        return new ArrayList<>(userPerformanceMap.values());
    }
    
    private List<Task> filterTasksByDateRange(List<Task> tasks, LocalDate startDate, LocalDate endDate) {
        return tasks.stream()
            .filter(task -> {
                LocalDate taskDate = task.getCreatedAt() != null ? 
                    task.getCreatedAt().toLocalDate() : null;
                if (taskDate == null) return false;
                
                boolean afterStart = startDate == null || !taskDate.isBefore(startDate);
                boolean beforeEnd = endDate == null || !taskDate.isAfter(endDate);
                
                return afterStart && beforeEnd;
            })
            .collect(Collectors.toList());
    }
    
    // Data classes for analytics results
    
    public static class ProductivityMetrics {
        public int totalTasks;
        public int completedTasks;
        public int inProgressTasks;
        public int todoTasks;
        public double completionRate;
        public double averageCompletionDays;
        public int totalTimeSpent; // in minutes
        public int totalEstimatedTime; // in minutes
        public double timeEfficiency;
    }
    
    public static class CompletionTrendData {
        public LocalDate date;
        public int tasksCreated;
        public int tasksCompleted;
        
        public CompletionTrendData(LocalDate date, int tasksCreated, int tasksCompleted) {
            this.date = date;
            this.tasksCreated = tasksCreated;
            this.tasksCompleted = tasksCompleted;
        }
    }
    
    public static class CategoryPerformance {
        public Category category;
        public int totalTasks;
        public int completedTasks;
        public double completionRate;
        public int totalTimeSpent;
        public double averageTimePerTask;
    }
    
    public static class OverdueAnalysis {
        public int overdueTasks;
        public int dueTodayTasks;
        public int dueThisWeekTasks;
        public double averageOverdueDays;
    }
    
    public static class UserPerformance {
        public int userId;
        public String username;
        public int assignedTasks;
        public int completedTasks;
        public double completionRate;
        public int totalTimeSpent;
    }
}