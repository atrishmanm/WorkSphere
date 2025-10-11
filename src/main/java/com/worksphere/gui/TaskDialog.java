package com.worksphere.gui;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.User;
import com.worksphere.model.Category;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;
import com.worksphere.dao.CategoryDAO;

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
    private Task task; // null for new task, existing task for editing
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
    
    private JButton saveButton;
    private JButton cancelButton;
    
    public TaskDialog(Window parent, TaskService taskService, UserService userService, Task task) {
        super(parent, task == null ? "Create New Task" : "Edit Task", ModalityType.APPLICATION_MODAL);
        
        this.taskService = taskService;
        this.userService = userService;
        this.categoryDAO = new CategoryDAO();
        this.task = task;
        
        initializeDialog();
        setupLayout();
        setupEventHandlers();
        populateFields();
    }
    
    private void initializeDialog() {
        setSize(650, 550);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
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
        
        add(formPanel, BorderLayout.CENTER);
        
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
                
                JOptionPane.showMessageDialog(this,
                    "Task created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Updating existing task
                task.setTitle(title);
                task.setDescription(description);
                task.setPriority(priority);
                task.setStatus(status);
                task.setAssignedToUsername(assignedTo);
                task.setDueDate(dueDate);
                task.setCategoryId(categoryId);
                task.setTags(tags);
                task.setEstimatedMinutes(estimatedMinutes);
                
                taskService.updateTask(task);
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
    
    public Task getTask() {
        return task;
    }
}
