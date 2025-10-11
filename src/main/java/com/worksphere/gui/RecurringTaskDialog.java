package com.worksphere.gui;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.User;
import com.worksphere.service.RecurrenceService;
import com.worksphere.service.RecurrenceService.RecurrenceRule;
import com.worksphere.service.RecurrenceService.RecurrenceFrequency;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dialog for creating recurring tasks
 */
public class RecurringTaskDialog extends JDialog {
    
    private TaskService taskService;
    private UserService userService;
    private RecurrenceService recurrenceService;
    private User currentUser;
    
    // Basic task fields
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Priority> priorityCombo;
    private JComboBox<User> assigneeCombo;
    private JTextField categoryField;
    private JTextField tagsField;
    private JSpinner estimatedMinutesSpinner;
    
    // Recurrence fields
    private JComboBox<RecurrenceFrequency> recurrenceTypeCombo;
    private JSpinner intervalSpinner;
    private JCheckBox[] weekdayCheckboxes;
    private JSpinner endAfterSpinner;
    private JRadioButton neverEndRadio;
    private JRadioButton endAfterRadio;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    private boolean taskSaved = false;
    
    public RecurringTaskDialog(Frame parent, TaskService taskService, UserService userService, User currentUser) {
        super(parent, "Create Recurring Task", true);
        this.taskService = taskService;
        this.userService = userService;
        this.recurrenceService = new RecurrenceService();
        this.currentUser = currentUser;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadUsers();
        
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Basic task fields
        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setSelectedItem(Priority.MEDIUM);
        
        assigneeCombo = new JComboBox<>();
        categoryField = new JTextField(15);
        tagsField = new JTextField(15);
        estimatedMinutesSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 999, 15));
        
        // Recurrence fields
        recurrenceTypeCombo = new JComboBox<>(RecurrenceFrequency.values());
        intervalSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        
        // Weekday checkboxes for weekly recurrence
        weekdayCheckboxes = new JCheckBox[7];
        weekdayCheckboxes[0] = new JCheckBox("Monday");
        weekdayCheckboxes[1] = new JCheckBox("Tuesday");
        weekdayCheckboxes[2] = new JCheckBox("Wednesday");
        weekdayCheckboxes[3] = new JCheckBox("Thursday");
        weekdayCheckboxes[4] = new JCheckBox("Friday");
        weekdayCheckboxes[5] = new JCheckBox("Saturday");
        weekdayCheckboxes[6] = new JCheckBox("Sunday");
        
        // End condition
        neverEndRadio = new JRadioButton("Never end", true);
        endAfterRadio = new JRadioButton("End after");
        endAfterSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));
        
        ButtonGroup endGroup = new ButtonGroup();
        endGroup.add(neverEndRadio);
        endGroup.add(endAfterRadio);
        
        // Buttons
        saveButton = new JButton("Save Recurring Task");
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Basic task information panel
        JPanel basicPanel = createBasicTaskPanel();
        
        // Recurrence pattern panel
        JPanel recurrencePanel = createRecurrencePanel();
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Combine panels
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(basicPanel, BorderLayout.NORTH);
        contentPanel.add(recurrencePanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createBasicTaskPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Task Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(titleField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(descriptionArea), gbc);
        
        // Priority and Assignee
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(priorityCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Assignee:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(assigneeCombo, gbc);
        
        // Category and Tags
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Tags:"), gbc);
        gbc.gridx = 1;
        panel.add(tagsField, gbc);
        
        // Estimated time
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Estimated Minutes:"), gbc);
        gbc.gridx = 1;
        panel.add(estimatedMinutesSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createRecurrencePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Recurrence Pattern"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Recurrence type
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Repeat:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(recurrenceTypeCombo, gbc);
        
        // Interval
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Every:"), gbc);
        gbc.gridx = 1;
        panel.add(intervalSpinner, gbc);
        gbc.gridx = 2;
        JLabel intervalLabel = new JLabel("time(s)");
        panel.add(intervalLabel, gbc);
        
        // Weekdays (for weekly recurrence)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel weekdayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        weekdayPanel.setBorder(new TitledBorder("Days of Week (for weekly recurrence)"));
        for (JCheckBox checkbox : weekdayCheckboxes) {
            weekdayPanel.add(checkbox);
        }
        panel.add(weekdayPanel, gbc);
        
        // End condition
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("End:"), gbc);
        gbc.gridx = 1;
        panel.add(neverEndRadio, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        JPanel endAfterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        endAfterPanel.add(endAfterRadio);
        endAfterPanel.add(endAfterSpinner);
        endAfterPanel.add(new JLabel("occurrences"));
        panel.add(endAfterPanel, gbc);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRecurringTask();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Update interval label based on recurrence type
        recurrenceTypeCombo.addActionListener(e -> updateIntervalLabel());
    }
    
    private void updateIntervalLabel() {
        // This would update the interval label based on selected recurrence type
        // For now, keeping it simple
    }
    
    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            assigneeCombo.removeAllItems();
            for (User user : users) {
                assigneeCombo.addItem(user);
            }
            if (currentUser != null) {
                assigneeCombo.setSelectedItem(currentUser);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load users: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveRecurringTask() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Create base task
            Task task = new Task();
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionArea.getText().trim());
            task.setPriority((Priority) priorityCombo.getSelectedItem());
            task.setStatus(TaskStatus.TODO);
            task.setCreatedAt(LocalDateTime.now());
            task.setEstimatedMinutes((Integer) estimatedMinutesSpinner.getValue());
            
            User assignee = (User) assigneeCombo.getSelectedItem();
            if (assignee != null) {
                task.setAssignedTo(assignee.getId());
            }
            
            // Set category and tags
            String category = categoryField.getText().trim();
            if (!category.isEmpty()) {
                // You might want to create/find category ID here
                task.setCategoryId(1); // Default for now
            }
            
            String tags = tagsField.getText().trim();
            if (!tags.isEmpty()) {
                List<String> tagList = Arrays.asList(tags.split(","));
                tagList = tagList.stream().map(String::trim).collect(java.util.stream.Collectors.toList());
                task.setTags(tagList);
            }
            
            // Create recurrence rule
            RecurrenceFrequency frequency = (RecurrenceFrequency) recurrenceTypeCombo.getSelectedItem();
            int interval = (Integer) intervalSpinner.getValue();
            
            RecurrenceRule rule = new RecurrenceRule(frequency).every(interval);
            
            // Set weekdays for weekly recurrence
            if (frequency == RecurrenceFrequency.WEEKLY) {
                List<DayOfWeek> weekdays = new ArrayList<>();
                for (int i = 0; i < weekdayCheckboxes.length; i++) {
                    if (weekdayCheckboxes[i].isSelected()) {
                        weekdays.add(DayOfWeek.of(i + 1)); // Monday = 1
                    }
                }
                if (!weekdays.isEmpty()) {
                    rule.onDays(weekdays.toArray(new DayOfWeek[0]));
                }
            }
            
            // Set end condition
            if (endAfterRadio.isSelected()) {
                rule.limitTo((Integer) endAfterSpinner.getValue());
            }
            
            // Create recurring task
            recurrenceService.createRecurringTask(task, rule);
            
            taskSaved = true;
            dispose();
            
            JOptionPane.showMessageDialog(getParent(),
                "Recurring task created successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to create recurring task: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return false;
        }
        
        if (assigneeCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select an assignee!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check if weekly recurrence has at least one day selected
        RecurrenceFrequency frequency = (RecurrenceFrequency) recurrenceTypeCombo.getSelectedItem();
        if (frequency == RecurrenceFrequency.WEEKLY) {
            boolean hasSelectedDay = false;
            for (JCheckBox checkbox : weekdayCheckboxes) {
                if (checkbox.isSelected()) {
                    hasSelectedDay = true;
                    break;
                }
            }
            if (!hasSelectedDay) {
                JOptionPane.showMessageDialog(this, 
                    "Please select at least one day of the week for weekly recurrence!", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isTaskSaved() {
        return taskSaved;
    }
}