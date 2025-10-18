package com.worksphere.gui;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.User;
import com.worksphere.model.Category;
import com.worksphere.model.Subtask;
import com.worksphere.model.TaskHistory;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;
import com.worksphere.dao.CategoryDAO;
import com.worksphere.dao.SubtaskDAO;
import com.worksphere.dao.TaskHistoryDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Dialog for creating and editing tasks
 */
public class TaskDialog extends JDialog {
    
    private TaskService taskService;
    private UserService userService;
    private CategoryDAO categoryDAO;
    private SubtaskDAO subtaskDAO;
    private TaskHistoryDAO taskHistoryDAO;
    private Task task; // null for new task, existing task for editing
    private User currentUser;  // Current logged-in user for history logging
    private boolean taskSaved = false;
    
    // Form components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Priority> priorityCombo;
    private JComboBox<TaskStatus> statusCombo;
    private JComboBox<String> assignedToCombo;
    private JComboBox<Category> categoryCombo;
    private JTextField tagsField;
    private JSpinner estimatedMinutesSpinner;
    private DatePickerField datePickerField;
    
    // Subtask components
    private DefaultListModel<Subtask> subtaskListModel;
    private JList<Subtask> subtaskList;
    private JTextField newSubtaskField;
    private JLabel subtaskProgressLabel;
    private List<Subtask> subtasks;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    public TaskDialog(Window parent, TaskService taskService, UserService userService, Task task, User currentUser) {
        super(parent, task == null ? "Create New Task" : "Edit Task", ModalityType.APPLICATION_MODAL);
        
        this.taskService = taskService;
        this.userService = userService;
        this.categoryDAO = new CategoryDAO();
        this.subtaskDAO = new SubtaskDAO();
        this.taskHistoryDAO = new TaskHistoryDAO();
        this.task = task;
        this.currentUser = currentUser;
        this.subtasks = new ArrayList<>();
        
        initializeDialog();
        setupLayout();
        setupEventHandlers();
        populateFields();
        loadSubtasks();
    }
    
    private void initializeDialog() {
        setSize(700, 650);  // Reduced height from 750 to 650
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(600, 500));  // Set minimum size for better UX
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        String headerText = task == null ? "📝 Create New Task" : "✏️ Edit Task";
        JLabel titleLabel = new JLabel(headerText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Title field
        formPanel.add(createFieldSection("Title (required):", createTitleField()));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Description field
        formPanel.add(createFieldSection("Description:", createDescriptionField()));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Priority and Status in same row
        JPanel priorityStatusPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        priorityStatusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        priorityStatusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        priorityStatusPanel.add(createFieldSection("Priority:", createPriorityField()));
        priorityStatusPanel.add(createFieldSection("Status:", createStatusField()));
        
        formPanel.add(priorityStatusPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Assigned to and Due date in same row
        JPanel assignedDueDatePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        assignedDueDatePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        assignedDueDatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        assignedDueDatePanel.add(createFieldSection("Assigned To:", createAssignedToField()));
        assignedDueDatePanel.add(createFieldSection("Due Date:", createDueDateField()));
        
        formPanel.add(assignedDueDatePanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Category and Estimated Time in same row
        JPanel categoryTimePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        categoryTimePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        categoryTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        categoryTimePanel.add(createFieldSection("Category:", createCategoryField()));
        categoryTimePanel.add(createFieldSection("Estimated Time (min):", createEstimatedTimeField()));
        
        formPanel.add(categoryTimePanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Tags field (full width)
        formPanel.add(createFieldSection("Tags (comma-separated):", createTagsField()));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Subtasks section (only for existing tasks)
        if (task != null) {
            formPanel.add(createSubtasksPanel());
            formPanel.add(Box.createVerticalStrut(15));
        }
        
        // Use tabbed interface for existing tasks, simple layout for new tasks
        if (task != null) {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("📝 Details", new JScrollPane(formPanel));
            tabbedPane.addTab("📜 History", createHistoryPanel());
            add(tabbedPane, BorderLayout.CENTER);
        } else {
            add(new JScrollPane(formPanel), BorderLayout.CENTER);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        saveButton = new JButton(task == null ? "Create Task" : "Save Changes");
        saveButton.setPreferredSize(new Dimension(140, 40));
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        saveButton.setBackground(new Color(60, 160, 60));
        saveButton.setForeground(Color.WHITE);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFieldSection(String labelText, JComponent field) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        section.add(label);
        section.add(Box.createVerticalStrut(5));
        section.add(field);
        
        return section;
    }
    
    private JTextField createTitleField() {
        titleField = new JTextField();
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return titleField;
    }
    
    private JScrollPane createDescriptionField() {
        descriptionArea = new JTextArea(6, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        return scrollPane;
    }
    
    private JComboBox<Priority> createPriorityField() {
        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return priorityCombo;
    }
    
    private JComboBox<TaskStatus> createStatusField() {
        statusCombo = new JComboBox<>(TaskStatus.values());
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return statusCombo;
    }
    
    private JComboBox<String> createAssignedToField() {
        assignedToCombo = new JComboBox<>();
        assignedToCombo.addItem("Unassigned");
        
        try {
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                assignedToCombo.addItem(user.getUsername());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading users: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        assignedToCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return assignedToCombo;
    }
    
    private JComboBox<Category> createCategoryField() {
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem(null); // No category option
        
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category category : categories) {
                categoryCombo.addItem(category);
            }
        } catch (Exception e) {
            // Handle error silently
        }
        
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("No Category");
                } else if (value instanceof Category) {
                    Category cat = (Category) value;
                    setText(cat.getName());
                    setOpaque(true);
                    if (!isSelected) {
                        setBackground(Color.decode(cat.getColor()));
                        setForeground(Color.BLACK);
                    }
                }
                return this;
            }
        });
        
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return categoryCombo;
    }
    
    private JTextField createTagsField() {
        tagsField = new JTextField();
        tagsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        tagsField.setToolTipText("Enter tags separated by commas (e.g., urgent, meeting, development)");
        return tagsField;
    }
    
    private JSpinner createEstimatedTimeField() {
        estimatedMinutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 15));
        estimatedMinutesSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return estimatedMinutesSpinner;
    }
    
    private JPanel createDueDateField() {
        datePickerField = new DatePickerField();
        datePickerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(datePickerField, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
    }
    
    private JPanel createSubtasksPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            "✓ Subtasks",
            0,
            0,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            new Color(100, 149, 237)
        ));
        
        // Progress label
        subtaskProgressLabel = new JLabel("0 of 0 completed");
        subtaskProgressLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        subtaskProgressLabel.setForeground(new Color(60, 160, 60));
        subtaskProgressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subtaskProgressLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Subtask list
        subtaskListModel = new DefaultListModel<>();
        subtaskList = new JList<>(subtaskListModel);
        subtaskList.setCellRenderer(new SubtaskListCellRenderer());
        subtaskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subtaskList.setVisibleRowCount(4);
        
        // Add mouse listener for checkbox clicks
        subtaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = subtaskList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle cellBounds = subtaskList.getCellBounds(index, index);
                    if (cellBounds != null && e.getX() < 25) { // Click on checkbox area
                        Subtask subtask = subtaskListModel.get(index);
                        toggleSubtask(subtask, index);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(subtaskList);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));
        
        // Add subtask panel
        JPanel addPanel = new JPanel(new BorderLayout(5, 0));
        addPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        addPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        newSubtaskField = new JTextField();
        newSubtaskField.setToolTipText("Enter subtask title and press Add");
        
        JButton addButton = new JButton("+ Add");
        addButton.setBackground(new Color(60, 160, 60));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addSubtask());
        
        // Allow Enter key to add subtask
        newSubtaskField.addActionListener(e -> addSubtask());
        
        JButton removeButton = new JButton("− Remove");
        removeButton.setBackground(new Color(220, 53, 69));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(e -> removeSelectedSubtask());
        
        addPanel.add(newSubtaskField, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.EAST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(removeButton);
        
        panel.add(addPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private void toggleSubtask(Subtask subtask, int index) {
        subtask.toggleCompleted();
        subtaskListModel.set(index, subtask); // Trigger repaint
        updateSubtaskProgress();
    }
    
    private void addSubtask() {
        String title = newSubtaskField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a subtask title",
                "Validation",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Subtask subtask = new Subtask();
        subtask.setTaskId(task.getId());
        subtask.setTitle(title);
        subtask.setCompleted(false);
        subtask.setOrderIndex(subtasks.size());
        
        subtasks.add(subtask);
        subtaskListModel.addElement(subtask);
        newSubtaskField.setText("");
        updateSubtaskProgress();
    }
    
    private void removeSelectedSubtask() {
        int selectedIndex = subtaskList.getSelectedIndex();
        if (selectedIndex >= 0) {
            Subtask subtask = subtaskListModel.get(selectedIndex);
            subtasks.remove(subtask);
            subtaskListModel.remove(selectedIndex);
            updateSubtaskProgress();
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select a subtask to remove",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateSubtaskProgress() {
        int total = subtaskListModel.getSize();
        int completed = 0;
        for (int i = 0; i < total; i++) {
            if (subtaskListModel.get(i).isCompleted()) {
                completed++;
            }
        }
        subtaskProgressLabel.setText(completed + " of " + total + " completed");
        
        // Update color based on progress
        if (total == 0) {
            subtaskProgressLabel.setForeground(Color.GRAY);
        } else if (completed == total) {
            subtaskProgressLabel.setForeground(new Color(60, 160, 60)); // Green
        } else {
            subtaskProgressLabel.setForeground(new Color(255, 140, 0)); // Orange
        }
    }
    
    private void loadSubtasks() {
        if (task != null && task.getId() > 0) {
            try {
                subtasks = subtaskDAO.getSubtasksByTaskId(task.getId());
                for (Subtask subtask : subtasks) {
                    subtaskListModel.addElement(subtask);
                }
                updateSubtaskProgress();
            } catch (Exception e) {
                System.err.println("Error loading subtasks: " + e.getMessage());
            }
        }
    }
    
    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("📜 Task History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        historyPanel.add(titleLabel, BorderLayout.NORTH);
        
        // History list panel
        JPanel historyListPanel = new JPanel();
        historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));
        historyListPanel.setBackground(Color.WHITE);
        
        try {
            if (task != null && task.getId() > 0) {
                List<TaskHistory> histories = taskHistoryDAO.getHistoryByTaskId(task.getId());
                
                if (histories.isEmpty()) {
                    JLabel noHistoryLabel = new JLabel("No history entries yet");
                    noHistoryLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                    noHistoryLabel.setForeground(Color.GRAY);
                    noHistoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    noHistoryLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                    historyListPanel.add(noHistoryLabel);
                } else {
                    for (TaskHistory history : histories) {
                        historyListPanel.add(createHistoryEntryPanel(history));
                        historyListPanel.add(Box.createVerticalStrut(10));
                    }
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading history: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            historyListPanel.add(errorLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(historyListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
        return historyPanel;
    }
    
    private JPanel createHistoryEntryPanel(TaskHistory history) {
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        entryPanel.setBackground(new Color(250, 250, 250));
        entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // User and timestamp row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel userLabel = new JLabel("👤 " + history.getUsername());
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));
        userLabel.setForeground(new Color(70, 130, 180));
        
        JLabel timeLabel = new JLabel(formatRelativeTime(history.getTimestamp()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(Color.GRAY);
        
        headerRow.add(userLabel, BorderLayout.WEST);
        headerRow.add(timeLabel, BorderLayout.EAST);
        
        entryPanel.add(headerRow);
        entryPanel.add(Box.createVerticalStrut(5));
        
        // Description
        JLabel descLabel = new JLabel(history.getFormattedDescription());
        descLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        entryPanel.add(descLabel);
        
        return entryPanel;
    }
    
    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown time";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();
        
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (seconds < 2592000) {
            long weeks = seconds / 604800;
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        } else {
            long months = seconds / 2592000;
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        }
    }
    
    private void saveSubtasks() {
        if (task == null || task.getId() <= 0) {
            return;
        }
        
        try {
            // Get existing subtasks from database
            List<Subtask> existingSubtasks = subtaskDAO.getSubtasksByTaskId(task.getId());
            
            // Delete removed subtasks
            for (Subtask existing : existingSubtasks) {
                boolean found = false;
                for (Subtask current : subtasks) {
                    if (current.getId() == existing.getId()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    subtaskDAO.deleteSubtask(existing.getId());
                }
            }
            
            // Create or update current subtasks
            for (int i = 0; i < subtasks.size(); i++) {
                Subtask subtask = subtasks.get(i);
                subtask.setOrderIndex(i); // Update order
                
                if (subtask.getId() > 0) {
                    // Update existing
                    subtaskDAO.updateSubtask(subtask);
                } else {
                    // Create new
                    subtask.setTaskId(task.getId());
                    int id = subtaskDAO.createSubtask(subtask);
                    subtask.setId(id);
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving subtasks: " + e.getMessage());
        }
    }
    
    // Custom cell renderer for subtasks with checkboxes
    private class SubtaskListCellRenderer extends JPanel implements ListCellRenderer<Subtask> {
        private JCheckBox checkbox;
        private JLabel label;
        
        public SubtaskListCellRenderer() {
            setLayout(new BorderLayout(5, 0));
            checkbox = new JCheckBox();
            label = new JLabel();
            add(checkbox, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Subtask> list, Subtask subtask,
                int index, boolean isSelected, boolean cellHasFocus) {
            checkbox.setSelected(subtask.isCompleted());
            
            String text = subtask.getTitle();
            if (subtask.isCompleted()) {
                label.setText("<html><s>" + text + "</s></html>");
                label.setForeground(Color.GRAY);
            } else {
                label.setText(text);
                label.setForeground(Color.BLACK);
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
            }
            
            return this;
        }
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTask();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Set focus to title field
        SwingUtilities.invokeLater(() -> titleField.requestFocus());
    }
    
    private void populateFields() {
        if (task != null) {
            // Editing existing task
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
            priorityCombo.setSelectedItem(task.getPriority());
            statusCombo.setSelectedItem(task.getStatus());
            
            if (task.getAssignedToUsername() != null) {
                assignedToCombo.setSelectedItem(task.getAssignedToUsername());
            } else {
                assignedToCombo.setSelectedItem("Unassigned");
            }
            
            if (task.getDueDate() != null) {
                datePickerField.setSelectedDate(task.getDueDate());
            } else {
                datePickerField.setDateEnabled(false);
            }
            
            // Set category
            if (task.getCategoryId() > 0) {
                try {
                    Category category = categoryDAO.getCategoryById(task.getCategoryId());
                    if (category != null) {
                        categoryCombo.setSelectedItem(category);
                    }
                } catch (Exception e) {
                    // Ignore error, leave no category selected
                }
            }
            
            // Set tags
            if (task.getTags() != null && !task.getTags().isEmpty()) {
                tagsField.setText(String.join(", ", task.getTags()));
            }
            
            // Set estimated time
            estimatedMinutesSpinner.setValue(task.getEstimatedMinutes());
            
        } else {
            // Creating new task - set defaults
            priorityCombo.setSelectedItem(Priority.MEDIUM);
            statusCombo.setSelectedItem(TaskStatus.TODO);
            assignedToCombo.setSelectedItem("Unassigned");
            categoryCombo.setSelectedItem(null);
            tagsField.setText("");
            estimatedMinutesSpinner.setValue(0);
        }
    }
    
    private void saveTask() {
        // Validation
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Title is required",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }
        
        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            description = null;
        }
        
        Priority priority = (Priority) priorityCombo.getSelectedItem();
        TaskStatus status = (TaskStatus) statusCombo.getSelectedItem();
        
        String assignedTo = (String) assignedToCombo.getSelectedItem();
        if ("Unassigned".equals(assignedTo)) {
            assignedTo = null;
        }
        
        LocalDate dueDate = datePickerField.getSelectedDate();
        
        // Get category
        Category selectedCategory = (Category) categoryCombo.getSelectedItem();
        int categoryId = selectedCategory != null ? selectedCategory.getId() : 0;
        
        // Get tags
        String tagsText = tagsField.getText().trim();
        List<String> tags = new ArrayList<>();
        if (!tagsText.isEmpty()) {
            String[] tagArray = tagsText.split(",");
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    tags.add(trimmedTag);
                }
            }
        }
        
        // Get estimated time
        int estimatedMinutes = (Integer) estimatedMinutesSpinner.getValue();
        
        try {
            if (task == null) {
                // Creating new task - use the existing createTask method then update additional fields
                task = taskService.createTask(title, description, priority, assignedTo, dueDate);
                
                // Now update the additional fields
                task.setCategoryId(categoryId);
                task.setTags(tags);
                task.setEstimatedMinutes(estimatedMinutes);
                
                // Update the task with the new fields
                taskService.updateTask(task);
                
                // Log task creation
                if (currentUser != null) {
                    try {
                        taskHistoryDAO.logTaskCreation(task.getId(), currentUser.getId());
                    } catch (Exception e) {
                        System.err.println("Error logging task creation: " + e.getMessage());
                    }
                }
                
                JOptionPane.showMessageDialog(this,
                    "Task created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Store old values for history logging
                Task oldTask = taskService.getTaskById(task.getId());
                
                // Updating existing task
                task.setTitle(title);
                task.setDescription(description);
                task.setPriority(priority);
                task.setStatus(status);
                
                // Convert username to user ID for assignedTo
                if (assignedTo != null) {
                    try {
                        User assignedUser = userService.getUserByUsername(assignedTo);
                        if (assignedUser != null) {
                            task.setAssignedTo(assignedUser.getId());
                            task.setAssignedToUsername(assignedTo);
                        } else {
                            task.setAssignedTo(null);
                            task.setAssignedToUsername(null);
                        }
                    } catch (Exception e) {
                        System.err.println("Error finding user: " + e.getMessage());
                        task.setAssignedTo(null);
                        task.setAssignedToUsername(null);
                    }
                } else {
                    task.setAssignedTo(null);
                    task.setAssignedToUsername(null);
                }
                
                task.setDueDate(dueDate);
                task.setCategoryId(categoryId);
                task.setTags(tags);
                task.setEstimatedMinutes(estimatedMinutes);
                
                taskService.updateTask(task);
                
                // Log changes to history
                if (currentUser != null && oldTask != null) {
                    try {
                        // Log title change
                        if (!oldTask.getTitle().equals(task.getTitle())) {
                            taskHistoryDAO.logFieldChange(task.getId(), currentUser.getId(), 
                                "title", oldTask.getTitle(), task.getTitle());
                        }
                        
                        // Log description change
                        String oldDesc = oldTask.getDescription() != null ? oldTask.getDescription() : "";
                        String newDesc = task.getDescription() != null ? task.getDescription() : "";
                        if (!oldDesc.equals(newDesc)) {
                            taskHistoryDAO.logFieldChange(task.getId(), currentUser.getId(), 
                                "description", oldDesc.isEmpty() ? "empty" : "updated", 
                                newDesc.isEmpty() ? "empty" : "updated");
                        }
                        
                        // Log priority change
                        if (oldTask.getPriority() != task.getPriority()) {
                            taskHistoryDAO.logFieldChange(task.getId(), currentUser.getId(), 
                                "priority", oldTask.getPriority().toString(), task.getPriority().toString());
                        }
                        
                        // Log status change
                        if (oldTask.getStatus() != task.getStatus()) {
                            taskHistoryDAO.logStatusChange(task.getId(), currentUser.getId(), 
                                oldTask.getStatus().toString(), task.getStatus().toString());
                        }
                        
                        // Log assignment change
                        String oldAssigned = oldTask.getAssignedToUsername() != null ? oldTask.getAssignedToUsername() : "Unassigned";
                        String newAssigned = task.getAssignedToUsername() != null ? task.getAssignedToUsername() : "Unassigned";
                        if (!oldAssigned.equals(newAssigned)) {
                            taskHistoryDAO.logFieldChange(task.getId(), currentUser.getId(), 
                                "assigned to", oldAssigned, newAssigned);
                        }
                        
                        // Log due date change
                        String oldDate = oldTask.getDueDate() != null ? oldTask.getDueDate().toString() : "No date";
                        String newDate = task.getDueDate() != null ? task.getDueDate().toString() : "No date";
                        if (!oldDate.equals(newDate)) {
                            taskHistoryDAO.logFieldChange(task.getId(), currentUser.getId(), 
                                "due date", oldDate, newDate);
                        }
                    } catch (Exception e) {
                        System.err.println("Error logging task history: " + e.getMessage());
                    }
                }
                
                // Save subtasks if task exists
                saveSubtasks();
                
                JOptionPane.showMessageDialog(this,
                    "Task updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            taskSaved = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving task: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isTaskSaved() {
        return taskSaved;
    }

    /**
     * Returns the saved Task after dialog closes (if saved), otherwise null.
     */
    public Task getSavedTask() {
        return taskSaved ? task : null;
    }
}
