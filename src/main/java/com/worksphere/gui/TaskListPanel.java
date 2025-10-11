package com.worksphere.gui;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.User;
import com.worksphere.model.Category;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;
import com.worksphere.service.SearchService;
import com.worksphere.dao.CategoryDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for displaying and managing tasks
 */
public class TaskListPanel extends JPanel {
    
    private TaskService taskService;
    private UserService userService;
    private SearchService searchService;
    private CategoryDAO categoryDAO;
    private User currentUser;
    private WorkSphereGUI mainFrame;
    
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JComboBox<TaskStatus> statusFilter;
    private JComboBox<Priority> priorityFilter;
    private JComboBox<String> userFilter;
    private JComboBox<Category> categoryFilter;
    private JTextField tagFilter;
    private JTextField searchField;
    
    private static final String[] COLUMN_NAMES = {
        "ID", "Title", "Priority", "Status", "Category", "Tags", "Due Date", "Time Est.", "Time Used", "Assigned To", "Actions"
    };
    
    public TaskListPanel(TaskService taskService, UserService userService, User currentUser, WorkSphereGUI mainFrame) {
        this.taskService = taskService;
        this.userService = userService;
        this.searchService = new SearchService();
        this.categoryDAO = new CategoryDAO();
        this.currentUser = currentUser;
        this.mainFrame = mainFrame;
        
        initializePanel();
        setupLayout();
        loadTasks();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupLayout() {
        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Create table
        createTaskTable();
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)), 
            "🔍 Search & Filter", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(73, 80, 87)
        ));
        filterPanel.setBackground(Color.WHITE);
        
        // First row of filters
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.setBackground(Color.WHITE);
        
        // Search field with styling
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row1.add(searchLabel);
        
        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.addActionListener(e -> applyFilters());
        row1.add(searchField);
        
        row1.add(Box.createHorizontalStrut(20));
        
        // Status filter with styling
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row1.add(statusLabel);
        
        statusFilter = new JComboBox<>();
        statusFilter.addItem(null); // All statuses
        for (TaskStatus status : TaskStatus.values()) {
            statusFilter.addItem(status);
        }
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilter.setPreferredSize(new Dimension(150, 30));
        statusFilter.addActionListener(e -> applyFilters());
        row1.add(statusFilter);
        
        // Second row of filters
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.setBackground(Color.WHITE);
        
        JLabel priorityLabel = new JLabel("Priority:");
        priorityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row2.add(priorityLabel);
        
        priorityFilter = new JComboBox<>();
        priorityFilter.addItem(null); // All priorities
        for (Priority priority : Priority.values()) {
            priorityFilter.addItem(priority);
        }
        priorityFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priorityFilter.setPreferredSize(new Dimension(120, 30));
        priorityFilter.addActionListener(e -> applyFilters());
        row2.add(priorityFilter);
        
        row2.add(Box.createHorizontalStrut(20));
        
        JLabel userLabel = new JLabel("Assigned To:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row2.add(userLabel);
        
        userFilter = new JComboBox<>();
        userFilter.addItem("All Users");
        populateUserFilter();
        userFilter.addActionListener(e -> applyFilters());
        row2.add(userFilter);
        
        row2.add(Box.createHorizontalStrut(20));
        
        // Category filter
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row2.add(categoryLabel);
        
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem(null); // All categories
        populateCategoryFilter();
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryFilter.setPreferredSize(new Dimension(120, 30));
        categoryFilter.addActionListener(e -> applyFilters());
        row2.add(categoryFilter);
        
        // Third row for additional filters
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row3.setBackground(Color.WHITE);
        
        // Tag filter
        JLabel tagLabel = new JLabel("Tags:");
        tagLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row3.add(tagLabel);
        
        tagFilter = new JTextField(15);
        tagFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagFilter.setPreferredSize(new Dimension(150, 30));
        tagFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        tagFilter.setToolTipText("Enter tag names separated by commas");
        tagFilter.addActionListener(e -> applyFilters());
        row3.add(tagFilter);
        
        row3.add(Box.createHorizontalStrut(20));
        
        JButton clearFiltersBtn = new JButton("Clear Filters");
        clearFiltersBtn.addActionListener(e -> clearFilters());
        row3.add(clearFiltersBtn);
        
        filterPanel.add(row1);
        filterPanel.add(row2);
        filterPanel.add(row3);
        
        return filterPanel;
    }
    
    private void populateCategoryFilter() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category category : categories) {
                categoryFilter.addItem(category);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateUserFilter() {
        try {
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                userFilter.addItem(user.getUsername());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading users: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createTaskTable() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10; // Only Actions column is editable
            }
        };
        
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(45); // Reduced since no text wrapping
        taskTable.setShowGrid(true);
        taskTable.setGridColor(new Color(230, 230, 230));
        taskTable.setBackground(Color.WHITE);
        taskTable.setSelectionBackground(new Color(52, 144, 220, 180));
        taskTable.setSelectionForeground(Color.BLACK);
        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Increased font size
        
        // Enable table sorting
        taskTable.setAutoCreateRowSorter(true);
        
        // Fix ID column to use numeric sorting
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) taskTable.getRowSorter();
        sorter.setComparator(0, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                try {
                    Integer id1 = Integer.parseInt(o1.toString());
                    Integer id2 = Integer.parseInt(o2.toString());
                    return id1.compareTo(id2);
                } catch (NumberFormatException e) {
                    return o1.toString().compareTo(o2.toString());
                }
            }
        });
        
        // Set header styling
        taskTable.getTableHeader().setBackground(new Color(248, 249, 250));
        taskTable.getTableHeader().setForeground(new Color(73, 80, 87));
        taskTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        taskTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(222, 226, 230)));
        taskTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // Set column widths
        TableColumn idColumn = taskTable.getColumnModel().getColumn(0);
        idColumn.setPreferredWidth(50);
        idColumn.setMaxWidth(50);
        
        TableColumn titleColumn = taskTable.getColumnModel().getColumn(1);
        titleColumn.setPreferredWidth(200);
        
        TableColumn priorityColumn = taskTable.getColumnModel().getColumn(2);
        priorityColumn.setPreferredWidth(80);
        priorityColumn.setMaxWidth(90);
        priorityColumn.setCellRenderer(new PriorityRenderer());
        
        TableColumn statusColumn = taskTable.getColumnModel().getColumn(3);
        statusColumn.setPreferredWidth(100);
        statusColumn.setCellRenderer(new StatusRenderer());
        
        TableColumn categoryColumn = taskTable.getColumnModel().getColumn(4);
        categoryColumn.setPreferredWidth(100);
        
        TableColumn tagsColumn = taskTable.getColumnModel().getColumn(5);
        tagsColumn.setPreferredWidth(120);
        
        TableColumn dueDateColumn = taskTable.getColumnModel().getColumn(6);
        dueDateColumn.setPreferredWidth(100);
        dueDateColumn.setCellRenderer(new DateRenderer());
        
        TableColumn timeEstColumn = taskTable.getColumnModel().getColumn(7);
        timeEstColumn.setPreferredWidth(80);
        timeEstColumn.setMaxWidth(90);
        
        TableColumn timeUsedColumn = taskTable.getColumnModel().getColumn(8);
        timeUsedColumn.setPreferredWidth(80);
        timeUsedColumn.setMaxWidth(90);
        
        TableColumn assignedColumn = taskTable.getColumnModel().getColumn(9);
        assignedColumn.setPreferredWidth(120);
        
        // Set Actions column widths and renderers
        TableColumn actionsColumn = taskTable.getColumnModel().getColumn(10);
        actionsColumn.setPreferredWidth(120);
        actionsColumn.setMinWidth(120);
        actionsColumn.setMaxWidth(140);
        actionsColumn.setCellRenderer(new ActionButtonRenderer());
        actionsColumn.setCellEditor(new ActionButtonEditor());
        
        // Double-click to edit
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = taskTable.getSelectedRow();
                    if (row >= 0) {
                        editTask(row);
                    }
                }
            }
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Create stylish buttons with text-based labels and improved layout
        JButton newTaskBtn = createStyledButton("+ New Task", new Color(40, 167, 69), Color.WHITE);
        newTaskBtn.setToolTipText("Create a new task");
        newTaskBtn.addActionListener(e -> createNewTask());
        
        JButton editTaskBtn = createStyledButton("Edit", new Color(0, 123, 255), Color.WHITE);
        editTaskBtn.setToolTipText("Edit selected task");
        editTaskBtn.addActionListener(e -> editSelectedTask());
        
        JButton deleteTaskBtn = createStyledButton("Delete", new Color(220, 53, 69), Color.WHITE);
        deleteTaskBtn.setToolTipText("Delete selected task");
        deleteTaskBtn.addActionListener(e -> deleteSelectedTask());
        
        JButton refreshBtn = createStyledButton("Refresh", new Color(108, 117, 125), Color.WHITE);
        refreshBtn.setToolTipText("Refresh task list");
        refreshBtn.addActionListener(e -> refresh());
        
        JButton exportBtn = createStyledButton("Export", new Color(111, 66, 193), Color.WHITE);
        exportBtn.setToolTipText("Export task statistics");
        exportBtn.addActionListener(e -> exportTasks());
        
        // Add separator
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 30));
        
        buttonPanel.add(newTaskBtn);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(editTaskBtn);
        buttonPanel.add(deleteTaskBtn);
        buttonPanel.add(separator);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        
        return buttonPanel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(100, 32));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = backgroundColor;
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private void editSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a task to edit.",
                "No Task Selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int taskId = (Integer) tableModel.getValueAt(selectedRow, 0);
            Task task = taskService.getTaskById(taskId);
            
            if (task != null) {
                TaskDialog taskDialog = new TaskDialog(
                    mainFrame,
                    taskService,
                    userService,
                    task
                );
                taskDialog.setVisible(true);
                
                if (taskDialog.isTaskSaved()) {
                    refresh();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error editing task: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a task to delete.",
                "No Task Selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int taskId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String taskTitle = (String) tableModel.getValueAt(selectedRow, 1);
            
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete task:\n'" + taskTitle + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (option == JOptionPane.YES_OPTION) {
                taskService.deleteTask(taskId);
                refresh();
                JOptionPane.showMessageDialog(this,
                    "Task deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting task: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTasks() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Task> tasks;
                if (currentUser != null && currentUser.isAdmin()) {
                    // Admin can see all tasks
                    tasks = taskService.getAllTasks();
                } else if (currentUser != null) {
                    // Regular users can only see tasks assigned to them or created by them
                    tasks = taskService.getTasksForUser(currentUser.getId());
                } else {
                    // No user logged in
                    tasks = new ArrayList<>();
                }
                updateTableData(tasks != null ? tasks : new ArrayList<>());
            } catch (Exception e) {
                System.err.println("Error loading tasks: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error loading tasks. Please try refreshing.\nError: " + e.getMessage(),
                    "Loading Error",
                    JOptionPane.WARNING_MESSAGE);
                // Load empty table as fallback
                updateTableData(new ArrayList<>());
            }
        });
    }
    
    private void updateTableData(List<Task> tasks) {
        try {
            tableModel.setRowCount(0);
            
            if (tasks == null) {
                return;
            }
            
            for (Task task : tasks) {
                if (task == null) continue; // Skip null tasks
                
                // Get category name
                String categoryName = "None";
                if (task.getCategoryId() > 0) {
                    try {
                        Category category = categoryDAO.getCategoryById(task.getCategoryId());
                        if (category != null) {
                            categoryName = category.getName();
                        }
                    } catch (Exception e) {
                        categoryName = "Unknown";
                    }
                }
                
                // Format tags
                String tagsStr = "";
                if (task.getTags() != null && !task.getTags().isEmpty()) {
                    tagsStr = String.join(", ", task.getTags());
                }
                
                // Format time estimates and usage
                String timeEst = task.getEstimatedMinutes() > 0 ? task.getEstimatedMinutes() + "m" : "-";
                String timeUsed = task.getActualMinutes() > 0 ? task.getActualMinutes() + "m" : "-";
                
                // Get assigned user name
                String assignedToName = "Unassigned";
                if (task.getAssignedTo() != null && task.getAssignedTo() > 0) {
                    try {
                        User assignedUser = userService.getUserById(task.getAssignedTo());
                        if (assignedUser != null) {
                            assignedToName = assignedUser.getUsername();
                        }
                    } catch (Exception e) {
                        assignedToName = "Unknown";
                    }
                }
                
                Object[] row = {
                    task.getId(),
                    task.getTitle() != null ? task.getTitle() : "N/A",
                    task.getPriority() != null ? task.getPriority() : "Unknown",
                    task.getStatus() != null ? task.getStatus() : "Unknown",
                    categoryName,
                    tagsStr,
                    task.getDueDate() != null ? 
                        safeFormatDate(task.getDueDate()) : "",
                    timeEst,
                    timeUsed,
                    assignedToName,
                    "Actions" // Placeholder for action buttons
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("Error updating table data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String safeFormatDate(java.time.LocalDate date) {
        try {
            if (date == null) return "";
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    private String safeFormatDateTime(java.time.LocalDateTime dateTime) {
        try {
            if (dateTime == null) return "N/A";
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private void applyFilters() {
        try {
            TaskStatus selectedStatus = (TaskStatus) statusFilter.getSelectedItem();
            Priority selectedPriority = (Priority) priorityFilter.getSelectedItem();
            String selectedUser = (String) userFilter.getSelectedItem();
            Category selectedCategory = (Category) categoryFilter.getSelectedItem();
            String searchText = searchField.getText().trim();
            String tagText = tagFilter.getText().trim();
            
            // Use SearchService for advanced filtering
            SearchService.TaskFilterCriteria criteria = new SearchService.TaskFilterCriteria();
            
            if (selectedStatus != null) {
                criteria.setStatuses(List.of(selectedStatus));
            }
            if (selectedPriority != null) {
                criteria.setPriorities(List.of(selectedPriority));
            }
            if (selectedCategory != null) {
                criteria.setCategoryIds(List.of(selectedCategory.getId()));
            }
            if (!searchText.isEmpty()) {
                criteria.setSearchText(searchText);
            }
            
            List<Task> filteredTasks;
            if (!searchText.isEmpty()) {
                // Use text search if search text is provided
                filteredTasks = searchService.searchTasks(searchText);
            } else {
                // Get all tasks and filter
                filteredTasks = taskService.getAllTasks();
            }
            
            // Apply criteria filters
            filteredTasks = searchService.filterTasks(criteria);
            
            // Apply user filter manually (since it's by username, not ID)
            if (selectedUser != null && !selectedUser.equals("All Users")) {
                filteredTasks = filteredTasks.stream()
                    .filter(task -> task.getAssignedToUsername() != null && 
                                   task.getAssignedToUsername().equals(selectedUser))
                    .collect(Collectors.toList());
            }
            
            // Apply tag filter manually 
            if (!tagText.isEmpty()) {
                String[] tags = tagText.split(",");
                List<String> tagList = Arrays.stream(tags)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
                
                filteredTasks = filteredTasks.stream()
                    .filter(task -> task.getTags() != null && 
                                   task.getTags().stream()
                                       .anyMatch(tag -> tagList.contains(tag.toLowerCase())))
                    .collect(Collectors.toList());
            }
                
            updateTableData(filteredTasks);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error applying filters: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFilters() {
        statusFilter.setSelectedItem(null);
        priorityFilter.setSelectedItem(null);
        userFilter.setSelectedItem("All Users");
        categoryFilter.setSelectedItem(null);
        tagFilter.setText("");
        searchField.setText("");
        loadTasks();
    }
    
    private void createNewTask() {
        TaskDialog taskDialog = new TaskDialog(
            SwingUtilities.getWindowAncestor(this), 
            taskService, 
            userService, 
            null
        );
        taskDialog.setVisible(true);
        
        if (taskDialog.isTaskSaved()) {
            refresh();
        }
    }
    
    private void editTask(int row) {
        try {
            int taskId = (Integer) tableModel.getValueAt(row, 0);
            Task task = taskService.getTaskById(taskId);
            
            if (task != null) {
                TaskDialog taskDialog = new TaskDialog(
                    SwingUtilities.getWindowAncestor(this), 
                    taskService, 
                    userService, 
                    task
                );
                taskDialog.setVisible(true);
                
                if (taskDialog.isTaskSaved()) {
                    refresh();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error opening task: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteTask(int row) {
        try {
            int taskId = (Integer) tableModel.getValueAt(row, 0);
            String taskTitle = (String) tableModel.getValueAt(row, 1);
            
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete task:\n'" + taskTitle + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (option == JOptionPane.YES_OPTION) {
                taskService.deleteTask(taskId);
                refresh();
                JOptionPane.showMessageDialog(this,
                    "Task deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting task: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showTaskInfo(int row) {
        try {
            int taskId = (Integer) tableModel.getValueAt(row, 0);
            Task task = taskService.getTaskById(taskId);
            
            if (task == null) {
                JOptionPane.showMessageDialog(this, "Task not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create detailed info dialog
            JDialog infoDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Task Information - " + task.getTitle(), true);
            infoDialog.setSize(500, 600);
            infoDialog.setLocationRelativeTo(this);
            
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // Create info text
            StringBuilder info = new StringBuilder();
            info.append("<html><body style='font-family: Arial; font-size: 12px;'>");
            info.append("<h2 style='color: #2c5282;'>").append(task.getTitle()).append("</h2>");
            
            info.append("<p><b>Description:</b><br>")
                .append(task.getDescription() != null ? task.getDescription() : "No description").append("</p>");
            
            info.append("<p><b>Priority:</b> ").append(task.getPriority()).append("<br>");
            info.append("<b>Status:</b> ").append(task.getStatus()).append("</p>");
            
            // Category info
            if (task.getCategoryId() > 0) {
                try {
                    Category category = categoryDAO.getCategoryById(task.getCategoryId());
                    if (category != null) {
                        info.append("<p><b>Category:</b> <span style='background-color: ")
                            .append(category.getColor()).append("; padding: 2px 6px; border-radius: 3px;'>")
                            .append(category.getName()).append("</span></p>");
                    }
                } catch (Exception e) {
                    info.append("<p><b>Category:</b> Unknown</p>");
                }
            } else {
                info.append("<p><b>Category:</b> None</p>");
            }
            
            // Tags
            if (task.getTags() != null && !task.getTags().isEmpty()) {
                info.append("<p><b>Tags:</b> ");
                for (String tag : task.getTags()) {
                    info.append("<span style='background-color: #e2e8f0; padding: 2px 6px; margin: 2px; border-radius: 3px;'>")
                        .append(tag).append("</span> ");
                }
                info.append("</p>");
            } else {
                info.append("<p><b>Tags:</b> None</p>");
            }
            
            // Time tracking
            info.append("<h3 style='color: #2d3748;'>Time Tracking</h3>");
            info.append("<p><b>Estimated Time:</b> ").append(task.getEstimatedMinutes() > 0 ? task.getEstimatedMinutes() + " minutes" : "Not set").append("<br>");
            info.append("<b>Actual Time Spent:</b> ").append(task.getActualMinutes() > 0 ? task.getActualMinutes() + " minutes" : "Not tracked").append("</p>");
            
            if (task.getEstimatedMinutes() > 0 && task.getActualMinutes() > 0) {
                double efficiency = (double) task.getEstimatedMinutes() / task.getActualMinutes() * 100;
                String efficiencyColor = efficiency >= 100 ? "#38a169" : efficiency >= 80 ? "#d69e2e" : "#e53e3e";
                info.append("<p><b>Efficiency:</b> <span style='color: ").append(efficiencyColor).append(";'>")
                    .append(String.format("%.1f%%", efficiency)).append("</span></p>");
            }
            
            // Dates
            info.append("<h3 style='color: #2d3748;'>Dates</h3>");
            info.append("<p><b>Due Date:</b> ").append(task.getDueDate() != null ? task.getDueDate() : "Not set").append("<br>");
            info.append("<b>Created:</b> ").append(task.getCreatedAt() != null ? task.getCreatedAt() : "Unknown").append("<br>");
            if (task.getCompletedAt() != null) {
                info.append("<b>Completed:</b> ").append(task.getCompletedAt()).append("<br>");
            }
            info.append("</p>");
            
            // Recurrence
            if (task.getRecurrenceRule() != null && !task.getRecurrenceRule().isEmpty()) {
                info.append("<h3 style='color: #2d3748;'>Recurrence</h3>");
                info.append("<p><b>Recurrence Rule:</b> ").append(task.getRecurrenceRule()).append("</p>");
                if (task.getParentTaskId() != null) {
                    info.append("<p><b>Parent Task ID:</b> ").append(task.getParentTaskId()).append("</p>");
                }
            }
            
            // Assigned user
            if (task.getAssignedTo() != null) {
                try {
                    User assignedUser = userService.getUserById(task.getAssignedTo());
                    if (assignedUser != null) {
                        info.append("<p><b>Assigned To:</b> ").append(assignedUser.getFullName())
                            .append(" (").append(assignedUser.getUsername()).append(")</p>");
                    }
                } catch (Exception e) {
                    info.append("<p><b>Assigned To:</b> Unknown User (ID: ").append(task.getAssignedTo()).append(")</p>");
                }
            } else {
                info.append("<p><b>Assigned To:</b> Unassigned</p>");
            }
            
            info.append("</body></html>");
            
            JLabel infoLabel = new JLabel(info.toString());
            JScrollPane scrollPane = new JScrollPane(infoLabel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton startPomodoroBtn = new JButton("Start Pomodoro");
            startPomodoroBtn.setBackground(new Color(138, 43, 226));
            startPomodoroBtn.setForeground(Color.WHITE);
            startPomodoroBtn.addActionListener(e -> {
                if (mainFrame != null) {
                    mainFrame.startPomodoroForTask(task);
                    infoDialog.dispose();
                }
            });
            
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> infoDialog.dispose());
            
            buttonPanel.add(startPomodoroBtn);
            buttonPanel.add(closeBtn);
            
            infoPanel.add(scrollPane, BorderLayout.CENTER);
            infoPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            infoDialog.add(infoPanel);
            infoDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error showing task info: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportTasks() {
        // Simple export to show task count and statistics
        try {
            List<Task> allTasks = taskService.getAllTasks();
            long todoCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
            long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
            long completedCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            
            String stats = String.format(
                "Task Statistics\n\n" +
                "Total Tasks: %d\n" +
                "📝 To-Do: %d\n" +
                "In Progress: %d\n" +
                "Completed: %d\n\n" +
                "Completion Rate: %.1f%%",
                allTasks.size(),
                todoCount,
                inProgressCount,
                completedCount,
                allTasks.size() > 0 ? (completedCount * 100.0 / allTasks.size()) : 0.0
            );
            
            JOptionPane.showMessageDialog(this, stats, "Task Export", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error exporting tasks: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void refresh() {
        if (searchField.getText().trim().isEmpty() && 
            statusFilter.getSelectedItem() == null &&
            priorityFilter.getSelectedItem() == null &&
            userFilter.getSelectedItem().equals("All Users")) {
            loadTasks();
        } else {
            applyFilters();
        }
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        // Refresh tasks when user changes to apply role-based filtering
        loadTasks();
    }
    
    // Custom renderers
    private class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Priority) {
                Priority priority = (Priority) value;
                switch (priority) {
                    case LOW:
                        setText("LOW");
                        setForeground(isSelected ? Color.BLACK : new Color(0, 150, 0));
                        break;
                    case MEDIUM:
                        setText("MEDIUM");
                        setForeground(isSelected ? Color.BLACK : new Color(200, 150, 0));
                        break;
                    case HIGH:
                        setText("HIGH");
                        setForeground(isSelected ? Color.BLACK : new Color(255, 100, 0));
                        break;
                    case URGENT:
                        setText("URGENT");
                        setForeground(isSelected ? Color.BLACK : new Color(200, 0, 0));
                        break;
                }
                setFont(new Font("Arial", Font.BOLD, 11));
            }
            
            return this;
        }
    }
    
    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof TaskStatus) {
                TaskStatus status = (TaskStatus) value;
                switch (status) {
                    case TODO:
                        setText("TO-DO");
                        setForeground(isSelected ? Color.BLACK : new Color(108, 117, 125));
                        break;
                    case IN_PROGRESS:
                        setText("IN PROGRESS");
                        setForeground(isSelected ? Color.BLACK : new Color(0, 123, 255));
                        break;
                    case COMPLETED:
                        setText("COMPLETED");
                        setForeground(isSelected ? Color.BLACK : new Color(40, 167, 69));
                        break;
                }
                setFont(new Font("Arial", Font.BOLD, 11));
            }
            
            return this;
        }
    }
    
    private class DateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String dateStr = (String) value;
                if (!dateStr.isEmpty()) {
                    // Check if due date is overdue (only for due date column)
                    if (column == 6) { // Due date column
                        try {
                            LocalDate dueDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                            if (dueDate.isBefore(LocalDate.now())) {
                                setForeground(isSelected ? Color.BLACK : Color.RED);
                                setText("! " + dateStr);
                            } else {
                                setForeground(isSelected ? Color.BLACK : Color.DARK_GRAY);
                                setText(dateStr);
                            }
                        } catch (Exception e) {
                            setForeground(isSelected ? Color.BLACK : Color.DARK_GRAY);
                            setText(dateStr);
                        }
                    } else {
                        setForeground(isSelected ? Color.BLACK : Color.DARK_GRAY);
                        setText(dateStr);
                    }
                } else {
                    setForeground(isSelected ? Color.BLACK : Color.GRAY);
                    setText("-");
                }
            }
            
            return this;
        }
    }
    
    // Action button renderer and editor
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editBtn;
        private JButton deleteBtn;
        private JButton infoBtn;
        
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
            setOpaque(true);
            
            editBtn = new JButton("Edit");
            editBtn.setPreferredSize(new Dimension(45, 22));
            editBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            editBtn.setToolTipText("Edit Task");
            editBtn.setBackground(new Color(52, 144, 220));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            editBtn.setFocusPainted(false);
            
            deleteBtn = new JButton("Del");
            deleteBtn.setPreferredSize(new Dimension(35, 22));
            deleteBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            deleteBtn.setToolTipText("Delete Task");
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            deleteBtn.setFocusPainted(false);
            
            infoBtn = new JButton("Info");
            infoBtn.setPreferredSize(new Dimension(40, 22));
            infoBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            infoBtn.setToolTipText("More Information");
            infoBtn.setBackground(new Color(40, 167, 69));
            infoBtn.setForeground(Color.WHITE);
            infoBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            infoBtn.setFocusPainted(false);
            
            add(editBtn);
            add(deleteBtn);
            add(infoBtn);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }
    
    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton editBtn;
        private JButton deleteBtn;
        private JButton infoBtn;
        private int currentRow;
        
        public ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
            panel.setOpaque(true);
            
            editBtn = new JButton("Edit");
            editBtn.setPreferredSize(new Dimension(45, 22));
            editBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            editBtn.setBackground(new Color(52, 144, 220));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            editBtn.setFocusPainted(false);
            editBtn.addActionListener(e -> {
                editTask(currentRow);
                fireEditingStopped();
            });
            
            deleteBtn = new JButton("Del");
            deleteBtn.setPreferredSize(new Dimension(35, 22));
            deleteBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            deleteBtn.setFocusPainted(false);
            deleteBtn.addActionListener(e -> {
                deleteTask(currentRow);
                fireEditingStopped();
            });
            
            infoBtn = new JButton("Info");
            infoBtn.setPreferredSize(new Dimension(40, 22));
            infoBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            infoBtn.setBackground(new Color(40, 167, 69));
            infoBtn.setForeground(Color.WHITE);
            infoBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            infoBtn.setFocusPainted(false);
            infoBtn.addActionListener(e -> {
                showTaskInfo(currentRow);
                fireEditingStopped();
            });
            
            panel.add(editBtn);
            panel.add(deleteBtn);
            panel.add(infoBtn);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }
    
    /**
     * Custom renderer for description column with hyphenation (no text wrapping)
     */
    private class DescriptionRenderer extends DefaultTableCellRenderer {
        
        public DescriptionRenderer() {
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String text = value != null ? value.toString() : "";
            String displayText = addHyphenation(text, 40); // Hyphenate at ~40 characters
            setText(displayText);
            
            // Set tooltip to show full text
            setToolTipText("<html><div style='width: 300px;'>" + text + "</div></html>");
            
            setVerticalAlignment(SwingConstants.CENTER);
            
            return this;
        }
        
        /**
         * Add smart hyphenation to long text
         */
        private String addHyphenation(String text, int maxLength) {
            if (text.length() <= maxLength) {
                return text;
            }
            
            // Find a good breaking point near maxLength
            int breakPoint = findBreakPoint(text, maxLength);
            
            if (breakPoint == -1) {
                // No good break point found, just truncate with ellipsis
                return text.substring(0, maxLength - 3) + "...";
            }
            
            return text.substring(0, breakPoint) + "...";
        }
        
        /**
         * Find a good breaking point (space, comma, period) near the max length
         */
        private int findBreakPoint(String text, int maxLength) {
            // Look for word boundaries within a range
            int searchStart = Math.max(0, maxLength - 10);
            int searchEnd = Math.min(text.length(), maxLength + 5);
            
            // Look for ideal break characters (space, comma, period)
            for (int i = searchEnd - 1; i >= searchStart; i--) {
                char c = text.charAt(i);
                if (c == ' ' || c == ',' || c == '.' || c == ';') {
                    return i;
                }
            }
            
            // If no ideal break point, look for any space
            for (int i = maxLength - 1; i >= searchStart; i--) {
                if (text.charAt(i) == ' ') {
                    return i;
                }
            }
            
            return -1; // No good break point found
        }
    }
}
