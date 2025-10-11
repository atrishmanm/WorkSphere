package com.worksphere.gui;

import com.worksphere.model.Task;
import com.worksphere.service.PomodoroService;
import com.worksphere.service.PomodoroService.PomodoroEvent;
import com.worksphere.service.PomodoroService.PomodoroListener;
import com.worksphere.service.PomodoroService.PomodoroState;
import com.worksphere.service.TaskService;
import com.worksphere.service.SearchService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * GUI panel for Pomodoro Timer functionality
 */
public class PomodoroTimerPanel extends JPanel implements PomodoroListener {
    
    private PomodoroService pomodoroService;
    private TaskService taskService;
    private SearchService searchService;
    private Task currentTask;
    
    // UI Components
    private JLabel timerLabel;
    private JLabel stateLabel;
    private JLabel taskLabel;
    private JProgressBar progressBar;
    private JButton startButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton skipButton;
    private JLabel cycleLabel;
    
    // Task selection components
    private JTextField taskSearchField;
    private JComboBox<Task> taskComboBox;
    private JButton selectTaskButton;
    
    // Custom timer components
    private JSpinner workMinutesSpinner;
    private JSpinner breakMinutesSpinner;
    private JCheckBox customTimerCheckbox;
    
    public PomodoroTimerPanel() {
        this.pomodoroService = new PomodoroService();
        this.taskService = new TaskService();
        this.searchService = new SearchService();
        this.pomodoroService.addListener(this);
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        updateDisplay();
        loadTasks();
    }
    
    private void initializeComponents() {
        // Timer display
        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(Color.DARK_GRAY);
        
        // State and task labels
        stateLabel = new JLabel("Ready to start", SwingConstants.CENTER);
        stateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        taskLabel = new JLabel("No task selected", SwingConstants.CENTER);
        taskLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        taskLabel.setForeground(Color.GRAY);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        
        // Control buttons
        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        skipButton = new JButton("Skip");
        
        // Cycle counter
        cycleLabel = new JLabel("Cycles: 0", SwingConstants.CENTER);
        cycleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Task selection components
        taskSearchField = new JTextField(20);
        taskSearchField.setToolTipText("Search tasks...");
        
        taskComboBox = new JComboBox<>();
        taskComboBox.setRenderer(new TaskComboBoxRenderer());
        taskComboBox.setToolTipText("Select a task to focus on");
        
        selectTaskButton = new JButton("Select Task");
        selectTaskButton.setToolTipText("Set the selected task as current");
        
        // Custom timer components
        customTimerCheckbox = new JCheckBox("Custom Timer");
        customTimerCheckbox.setToolTipText("Enable custom work/break durations");
        
        workMinutesSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 120, 1));
        workMinutesSpinner.setToolTipText("Work session duration (minutes)");
        workMinutesSpinner.setEnabled(false);
        
        breakMinutesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        breakMinutesSpinner.setToolTipText("Break duration (minutes)");
        breakMinutesSpinner.setEnabled(false);
        
        // Initially disable some buttons
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        skipButton.setEnabled(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Pomodoro Timer"));
        
        // Top panel - task selection
        JPanel taskSelectionPanel = new JPanel(new BorderLayout(5, 5));
        taskSelectionPanel.setBorder(BorderFactory.createTitledBorder("Task Selection"));
        
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(taskSearchField);
        
        JPanel taskPanel = new JPanel(new FlowLayout());
        taskPanel.add(new JLabel("Task:"));
        taskPanel.add(taskComboBox);
        taskPanel.add(selectTaskButton);
        
        taskSelectionPanel.add(searchPanel, BorderLayout.NORTH);
        taskSelectionPanel.add(taskPanel, BorderLayout.CENTER);
        
        // Custom timer panel
        JPanel customTimerPanel = new JPanel(new FlowLayout());
        customTimerPanel.setBorder(BorderFactory.createTitledBorder("Timer Settings"));
        customTimerPanel.add(customTimerCheckbox);
        customTimerPanel.add(new JLabel("Work:"));
        customTimerPanel.add(workMinutesSpinner);
        customTimerPanel.add(new JLabel("min"));
        customTimerPanel.add(new JLabel("Break:"));
        customTimerPanel.add(breakMinutesSpinner);
        customTimerPanel.add(new JLabel("min"));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(taskSelectionPanel, BorderLayout.CENTER);
        topPanel.add(customTimerPanel, BorderLayout.SOUTH);
        
        // Timer display panel
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.add(timerLabel, BorderLayout.CENTER);
        timerPanel.add(stateLabel, BorderLayout.SOUTH);
        
        // Middle panel - task and progress
        JPanel middlePanel = new JPanel(new BorderLayout(5, 5));
        middlePanel.add(taskLabel, BorderLayout.NORTH);
        middlePanel.add(progressBar, BorderLayout.CENTER);
        middlePanel.add(cycleLabel, BorderLayout.SOUTH);
        
        // Bottom panel - control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(skipButton);
        
        // Add panels to main layout
        add(topPanel, BorderLayout.NORTH);
        add(timerPanel, BorderLayout.CENTER);
        add(middlePanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> {
            if (currentTask != null) {
                pomodoroService.startPomodoro(currentTask);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a task first", 
                    "No Task Selected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        pauseButton.addActionListener(e -> {
            if (pomodoroService.getCurrentState() == PomodoroState.PAUSED) {
                pomodoroService.resumePomodoro();
            } else {
                pomodoroService.pausePomodoro();
            }
        });
        
        stopButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to stop the current session?",
                "Stop Pomodoro",
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                pomodoroService.stopPomodoro();
            }
        });
        
        skipButton.addActionListener(e -> {
            pomodoroService.skipToNext();
        });
        
        // Task selection event handlers
        taskSearchField.addActionListener(e -> {
            searchTasks(taskSearchField.getText());
        });
        
        // Real-time search as user types
        taskSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchTasks(taskSearchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchTasks(taskSearchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchTasks(taskSearchField.getText()); }
        });
        
        selectTaskButton.addActionListener(e -> {
            Task selectedTask = (Task) taskComboBox.getSelectedItem();
            setCurrentTask(selectedTask);
            if (selectedTask != null) {
                JOptionPane.showMessageDialog(this, 
                    "Task selected: " + selectedTask.getTitle(), 
                    "Task Selected", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Custom timer event handlers
        customTimerCheckbox.addActionListener(e -> {
            boolean enabled = customTimerCheckbox.isSelected();
            workMinutesSpinner.setEnabled(enabled);
            breakMinutesSpinner.setEnabled(enabled);
            
            if (enabled) {
                // Update PomodoroService with custom durations if needed
                int workMinutes = (Integer) workMinutesSpinner.getValue();
                int breakMinutes = (Integer) breakMinutesSpinner.getValue();
                updateTimerDisplay(workMinutes);
            } else {
                // Reset to default
                updateTimerDisplay(25);
            }
        });
        
        workMinutesSpinner.addChangeListener(e -> {
            if (customTimerCheckbox.isSelected()) {
                int workMinutes = (Integer) workMinutesSpinner.getValue();
                updateTimerDisplay(workMinutes);
            }
        });
    }
    
    /**
     * Update timer display with given minutes
     */
    private void updateTimerDisplay(int minutes) {
        timerLabel.setText(String.format("%d:00", minutes));
    }
    
    @Override
    public void onPomodoroEvent(PomodoroEvent event, PomodoroService service) {
        SwingUtilities.invokeLater(() -> {
            switch (event) {
                case STARTED:
                    handleStarted();
                    break;
                case PAUSED:
                    handlePaused();
                    break;
                case RESUMED:
                    handleResumed();
                    break;
                case STOPPED:
                    handleStopped();
                    break;
                case TICK:
                    updateTimer();
                    break;
                case WORK_COMPLETED:
                    handleWorkCompleted();
                    break;
                case SHORT_BREAK_STARTED:
                    handleBreakStarted("Short Break");
                    break;
                case LONG_BREAK_STARTED:
                    handleBreakStarted("Long Break");
                    break;
                case SHORT_BREAK_COMPLETED:
                case LONG_BREAK_COMPLETED:
                    handleBreakCompleted();
                    break;
                case WORK_SESSION_STARTED:
                    handleWorkSessionStarted();
                    break;
            }
            updateDisplay();
        });
    }
    
    private void handleStarted() {
        stateLabel.setText("Working");
        timerLabel.setForeground(new Color(46, 125, 50)); // Green
        showNotification("Pomodoro Started", "Focus time! Work on: " + currentTask.getTitle());
    }
    
    private void handlePaused() {
        stateLabel.setText("Paused");
        timerLabel.setForeground(Color.ORANGE);
        pauseButton.setText("Resume");
    }
    
    private void handleResumed() {
        stateLabel.setText("Working");
        timerLabel.setForeground(new Color(46, 125, 50)); // Green
        pauseButton.setText("Pause");
    }
    
    private void handleStopped() {
        stateLabel.setText("Stopped");
        timerLabel.setForeground(Color.DARK_GRAY);
        timerLabel.setText("25:00");
        progressBar.setValue(0);
        progressBar.setString("Ready");
    }
    
    private void handleWorkCompleted() {
        showNotification("Work Session Complete!", 
            "Great job! Take a well-deserved break.");
        playNotificationSound();
    }
    
    private void handleBreakStarted(String breakType) {
        stateLabel.setText(breakType);
        timerLabel.setForeground(new Color(33, 150, 243)); // Blue
        showNotification(breakType + " Started", 
            "Time to relax and recharge!");
    }
    
    private void handleBreakCompleted() {
        showNotification("Break Complete", 
            "Ready for another productive session?");
        playNotificationSound();
    }
    
    private void handleWorkSessionStarted() {
        stateLabel.setText("Working");
        timerLabel.setForeground(new Color(46, 125, 50)); // Green
    }
    
    private void updateTimer() {
        timerLabel.setText(pomodoroService.getFormattedTimeRemaining());
        
        double progress = pomodoroService.getProgress() * 100;
        progressBar.setValue((int) progress);
        
        String stateText = pomodoroService.getCurrentState().toString().replace("_", " ");
        progressBar.setString(stateText + " - " + (int) progress + "%");
    }
    
    private void updateDisplay() {
        PomodoroState state = pomodoroService.getCurrentState();
        boolean isStopped = (state == PomodoroState.STOPPED);
        boolean isPaused = (state == PomodoroState.PAUSED);
        
        startButton.setEnabled(isStopped && currentTask != null);
        pauseButton.setEnabled(!isStopped);
        stopButton.setEnabled(!isStopped);
        skipButton.setEnabled(!isStopped);
        
        // Update pause button text
        pauseButton.setText(isPaused ? "Resume" : "Pause");
        
        // Update cycle count
        cycleLabel.setText("Cycles: " + pomodoroService.getCompletedCycles());
        
        // Update task label
        if (currentTask != null) {
            taskLabel.setText("Working on: " + currentTask.getTitle());
            taskLabel.setForeground(Color.BLACK);
        } else {
            taskLabel.setText("No task selected");
            taskLabel.setForeground(Color.GRAY);
        }
    }
    
    /**
     * Set the current task for the Pomodoro session
     */
    public void setCurrentTask(Task task) {
        this.currentTask = task;
        updateDisplay();
    }
    
    /**
     * Get the current task
     */
    public Task getCurrentTask() {
        return currentTask;
    }
    
    /**
     * Get the Pomodoro service for external access
     */
    public PomodoroService getPomodoroService() {
        return pomodoroService;
    }
    
    private void showNotification(String title, String message) {
        // Simple notification using system tray if available
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                
                // Create tray icon (you might want to use a custom icon)
                Image image = Toolkit.getDefaultToolkit().createImage("");
                TrayIcon trayIcon = new TrayIcon(image, "WorkSphere Pomodoro");
                trayIcon.setImageAutoSize(true);
                
                tray.add(trayIcon);
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                
                // Remove the icon after showing notification
                Timer timer = new Timer(3000, e -> tray.remove(trayIcon));
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception e) {
                // Fallback to dialog
                showDialogNotification(title, message);
            }
        } else {
            showDialogNotification(title, message);
        }
    }
    
    private void showDialogNotification(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void playNotificationSound() {
        // Simple beep sound
        Toolkit.getDefaultToolkit().beep();
    }
    
    /**
     * Load all tasks into the combo box
     */
    private void loadTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            taskComboBox.removeAllItems();
            taskComboBox.addItem(null); // "No task" option
            for (Task task : tasks) {
                taskComboBox.addItem(task);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading tasks: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Search and filter tasks based on search text
     */
    private void searchTasks(String searchText) {
        try {
            List<Task> filteredTasks;
            
            if (searchText == null || searchText.trim().isEmpty()) {
                filteredTasks = taskService.getAllTasks();
            } else {
                // Simple text-based filtering
                String query = searchText.trim().toLowerCase();
                filteredTasks = taskService.getAllTasks().stream()
                    .filter(task -> 
                        task.getTitle().toLowerCase().contains(query) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase().contains(query))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            taskComboBox.removeAllItems();
            taskComboBox.addItem(null); // "No task" option
            for (Task task : filteredTasks) {
                taskComboBox.addItem(task);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching tasks: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Custom renderer for task combo box
     */
    private class TaskComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value == null) {
                setText("No task selected");
                setForeground(Color.GRAY);
                setFont(getFont().deriveFont(Font.ITALIC));
            } else if (value instanceof Task) {
                Task task = (Task) value;
                setText("ID " + task.getId() + ": " + task.getTitle());
                setForeground(Color.BLACK);
                setFont(getFont().deriveFont(Font.PLAIN));
                
                // Add status indication
                switch (task.getStatus()) {
                    case COMPLETED:
                        setForeground(new Color(0, 128, 0));
                        break;
                    case IN_PROGRESS:
                        setForeground(new Color(255, 165, 0));
                        break;
                    case TODO:
                        setForeground(Color.BLACK);
                        break;
                }
            }
            
            return this;
        }
    }
}