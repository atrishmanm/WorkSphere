package com.worksphere.service;

import com.worksphere.dao.TaskDAO;
import com.worksphere.dao.CategoryDAO;
import com.worksphere.model.Task;
import com.worksphere.model.Category;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.Priority;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service for advanced search and filtering functionality
 */
public class SearchService {
    private final TaskDAO taskDAO;
    private final CategoryDAO categoryDAO;
    
    public SearchService() {
        this.taskDAO = new TaskDAO();
        this.categoryDAO = new CategoryDAO();
    }
    
    /**
     * Search tasks by text across title, description, and tags
     */
    public List<Task> searchTasks(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return taskDAO.findAll();
        }
        
        String searchTerm = query.toLowerCase().trim();
        List<Task> allTasks = taskDAO.findAll();
        
        return allTasks.stream()
            .filter(task -> matchesSearchTerm(task, searchTerm))
            .collect(Collectors.toList());
    }
    
    /**
     * Advanced filter for tasks with multiple criteria
     */
    public List<Task> filterTasks(TaskFilterCriteria criteria) throws SQLException {
        List<Task> tasks = taskDAO.findAll();
        
        return tasks.stream()
            .filter(task -> matchesCriteria(task, criteria))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter by category
     */
    public List<Task> filterByCategory(int categoryId) throws SQLException {
        if (categoryId <= 0) {
            return taskDAO.findAll();
        }
        return taskDAO.findByCategory(categoryId);
    }
    
    /**
     * Filter by tag
     */
    public List<Task> filterByTag(String tag) throws SQLException {
        if (tag == null || tag.trim().isEmpty()) {
            return taskDAO.findAll();
        }
        return taskDAO.findByTag(tag.trim());
    }
    
    /**
     * Get all unique tags from all tasks
     */
    public List<String> getAllTags() throws SQLException {
        List<Task> allTasks = taskDAO.findAll();
        return allTasks.stream()
            .flatMap(task -> task.getTags().stream())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Get all categories
     */
    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }
    
    private boolean matchesSearchTerm(Task task, String searchTerm) {
        // Search in title
        if (task.getTitle() != null && task.getTitle().toLowerCase().contains(searchTerm)) {
            return true;
        }
        
        // Search in description
        if (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchTerm)) {
            return true;
        }
        
        // Search in tags
        if (task.getTags() != null) {
            for (String tag : task.getTags()) {
                if (tag.toLowerCase().contains(searchTerm)) {
                    return true;
                }
            }
        }
        
        // Search in category name
        if (task.getCategory() != null && 
            task.getCategory().getName() != null && 
            task.getCategory().getName().toLowerCase().contains(searchTerm)) {
            return true;
        }
        
        return false;
    }
    
    private boolean matchesCriteria(Task task, TaskFilterCriteria criteria) {
        // Status filter
        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            if (!criteria.getStatuses().contains(task.getStatus())) {
                return false;
            }
        }
        
        // Priority filter
        if (criteria.getPriorities() != null && !criteria.getPriorities().isEmpty()) {
            if (!criteria.getPriorities().contains(task.getPriority())) {
                return false;
            }
        }
        
        // Category filter
        if (criteria.getCategoryIds() != null && !criteria.getCategoryIds().isEmpty()) {
            if (!criteria.getCategoryIds().contains(task.getCategoryId())) {
                return false;
            }
        }
        
        // Assignee filter
        if (criteria.getAssigneeIds() != null && !criteria.getAssigneeIds().isEmpty()) {
            if (task.getAssignedTo() == null || !criteria.getAssigneeIds().contains(task.getAssignedTo())) {
                return false;
            }
        }
        
        // Date range filter
        if (criteria.getStartDate() != null && task.getDueDate() != null) {
            if (task.getDueDate().isBefore(criteria.getStartDate())) {
                return false;
            }
        }
        
        if (criteria.getEndDate() != null && task.getDueDate() != null) {
            if (task.getDueDate().isAfter(criteria.getEndDate())) {
                return false;
            }
        }
        
        // Overdue filter
        if (criteria.isOverdueOnly() && !task.isOverdue()) {
            return false;
        }
        
        // Text search
        if (criteria.getSearchText() != null && !criteria.getSearchText().trim().isEmpty()) {
            if (!matchesSearchTerm(task, criteria.getSearchText().toLowerCase().trim())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Filter criteria class for advanced filtering
     */
    public static class TaskFilterCriteria {
        private List<TaskStatus> statuses;
        private List<Priority> priorities;
        private List<Integer> categoryIds;
        private List<Integer> assigneeIds;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean overdueOnly;
        private String searchText;
        
        // Constructors
        public TaskFilterCriteria() {}
        
        // Getters and setters
        public List<TaskStatus> getStatuses() { return statuses; }
        public void setStatuses(List<TaskStatus> statuses) { this.statuses = statuses; }
        
        public List<Priority> getPriorities() { return priorities; }
        public void setPriorities(List<Priority> priorities) { this.priorities = priorities; }
        
        public List<Integer> getCategoryIds() { return categoryIds; }
        public void setCategoryIds(List<Integer> categoryIds) { this.categoryIds = categoryIds; }
        
        public List<Integer> getAssigneeIds() { return assigneeIds; }
        public void setAssigneeIds(List<Integer> assigneeIds) { this.assigneeIds = assigneeIds; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public boolean isOverdueOnly() { return overdueOnly; }
        public void setOverdueOnly(boolean overdueOnly) { this.overdueOnly = overdueOnly; }
        
        public String getSearchText() { return searchText; }
        public void setSearchText(String searchText) { this.searchText = searchText; }
        
        // Builder pattern methods
        public TaskFilterCriteria withStatuses(List<TaskStatus> statuses) {
            this.statuses = statuses;
            return this;
        }
        
        public TaskFilterCriteria withPriorities(List<Priority> priorities) {
            this.priorities = priorities;
            return this;
        }
        
        public TaskFilterCriteria withCategories(List<Integer> categoryIds) {
            this.categoryIds = categoryIds;
            return this;
        }
        
        public TaskFilterCriteria withAssignees(List<Integer> assigneeIds) {
            this.assigneeIds = assigneeIds;
            return this;
        }
        
        public TaskFilterCriteria withDateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            return this;
        }
        
        public TaskFilterCriteria withOverdueOnly(boolean overdueOnly) {
            this.overdueOnly = overdueOnly;
            return this;
        }
        
        public TaskFilterCriteria withSearchText(String searchText) {
            this.searchText = searchText;
            return this;
        }
    }
}