package com.worksphere.gui;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.User;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

/**
 * Calendar view panel displaying tasks on their due dates in a monthly grid
 */
public class CalendarViewPanel extends JPanel {
    
    private TaskService taskService;
    private UserService userService;
    private User currentUser;
    
    private YearMonth currentMonth;
    private JLabel monthYearLabel;
    private JPanel calendarGrid;
    
    private Map<LocalDate, List<Task>> tasksByDate;
    
    // Color constants for priorities
    private static final Color URGENT_COLOR = new Color(220, 53, 69);      // Red
    private static final Color HIGH_COLOR = new Color(255, 140, 0);        // Orange
    private static final Color MEDIUM_COLOR = new Color(0, 123, 255);      // Blue
    private static final Color LOW_COLOR = new Color(40, 167, 69);         // Green
    private static final Color TODAY_COLOR = new Color(255, 235, 59);      // Yellow
    private static final Color WEEKEND_COLOR = new Color(245, 245, 245);   // Light gray
    
    public CalendarViewPanel(TaskService taskService, UserService userService, User currentUser) {
        this.taskService = taskService;
        this.userService = userService;
        this.currentUser = currentUser;
        this.currentMonth = YearMonth.now();
        this.tasksByDate = new HashMap<>();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        
        setupHeader();
        setupCalendar();
        
        // Load tasks after GUI is set up, using SwingUtilities.invokeLater to ensure proper initialization
        SwingUtilities.invokeLater(() -> loadTasks());
    }
    
    private void setupHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navPanel.setOpaque(false);
        
        // Previous month button
        JButton prevButton = createNavButton("◄ Previous");
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        
        // Month/Year label
        monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 24));
        monthYearLabel.setForeground(Color.WHITE);
        updateMonthYearLabel();
        
        // Next month button
        JButton nextButton = createNavButton("Next ►");
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });
        
        // Today button
        JButton todayButton = createNavButton("Today");
        todayButton.setBackground(new Color(255, 193, 7));
        todayButton.addActionListener(e -> {
            currentMonth = YearMonth.now();
            refreshCalendar();
        });
        
        navPanel.add(prevButton);
        navPanel.add(monthYearLabel);
        navPanel.add(nextButton);
        navPanel.add(Box.createHorizontalStrut(30));
        navPanel.add(todayButton);
        
        headerPanel.add(navPanel, BorderLayout.CENTER);
        
        // Legend panel
        JPanel legendPanel = createLegendPanel();
        headerPanel.add(legendPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 58, 64));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (text.equals("Today")) {
                    button.setBackground(new Color(255, 193, 7));
                } else {
                    button.setBackground(new Color(52, 58, 64));
                }
            }
        });
        
        return button;
    }
    
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        
        panel.add(createLegendItem("🔴 Urgent", URGENT_COLOR));
        panel.add(createLegendItem("🟠 High", HIGH_COLOR));
        panel.add(createLegendItem("🔵 Medium", MEDIUM_COLOR));
        panel.add(createLegendItem("🟢 Low", LOW_COLOR));
        panel.add(createLegendItem("⭐ Today", TODAY_COLOR));
        
        return panel;
    }
    
    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(Color.WHITE);
        
        item.add(colorBox);
        item.add(label);
        
        return item;
    }
    
    private void setupCalendar() {
        JPanel calendarPanel = new JPanel(new BorderLayout(0, 0));
        calendarPanel.setBackground(Color.WHITE);
        
        // Day headers (Sun, Mon, Tue, etc.)
        JPanel dayHeadersPanel = new JPanel(new GridLayout(1, 7, 1, 1));
        dayHeadersPanel.setBackground(Color.LIGHT_GRAY);
        dayHeadersPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
            dayLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            dayLabel.setBackground(new Color(220, 220, 220));
            dayLabel.setOpaque(true);
            dayLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            dayHeadersPanel.add(dayLabel);
        }
        
        calendarPanel.add(dayHeadersPanel, BorderLayout.NORTH);
        
        // Calendar grid (6 rows x 7 columns)
        calendarGrid = new JPanel(new GridLayout(6, 7, 1, 1));
        calendarGrid.setBackground(Color.LIGHT_GRAY);
        calendarGrid.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        calendarPanel.add(calendarGrid, BorderLayout.CENTER);
        
        add(calendarPanel, BorderLayout.CENTER);
    }
    
    private void updateMonthYearLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentMonth.format(formatter));
    }
    
    private void refreshCalendar() {
        updateMonthYearLabel();
        loadTasks();
    }
    
    private void loadTasks() {
        // Clear existing tasks
        tasksByDate.clear();
        
        // Safety check: ensure currentUser is not null
        if (currentUser == null) {
            // Overlay friendly message on top of calendar
            calendarGrid.removeAll();
            JLabel messageLabel = new JLabel("No user logged in. Please login to view your calendar.", SwingConstants.CENTER);
            messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
            messageLabel.setForeground(Color.RED);
            calendarGrid.setLayout(new BorderLayout());
            calendarGrid.add(messageLabel, BorderLayout.CENTER);
            calendarGrid.revalidate();
            calendarGrid.repaint();
            return;
        }
        
        // Get all tasks for the current user
        try {
            List<Task> allTasks;
            if (currentUser.isAdmin()) {
                allTasks = taskService.getAllTasks();
            } else {
                allTasks = taskService.getTasksForUser(currentUser.getId());
            }
            
            // Group tasks by due date
            for (Task task : allTasks) {
                if (task.getDueDate() != null) {
                    LocalDate dueDate = task.getDueDate();
                    tasksByDate.computeIfAbsent(dueDate, k -> new ArrayList<>()).add(task);
                }
            }
            
            // Render the calendar
            renderCalendar();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading tasks: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void renderCalendar() {
    // Restore calendar grid layout before rendering
    calendarGrid.removeAll();
    calendarGrid.setLayout(new GridLayout(6, 7, 1, 1));
        
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0=Sunday, 6=Saturday
        int daysInMonth = currentMonth.lengthOfMonth();
        
        LocalDate today = LocalDate.now();
        
        // Add empty cells before the first day
        for (int i = 0; i < dayOfWeek; i++) {
            calendarGrid.add(createEmptyDayCell());
        }
        
        // Add day cells
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            List<Task> tasksOnDate = tasksByDate.getOrDefault(date, new ArrayList<>());
            
            boolean isToday = date.equals(today);
            boolean isWeekend = date.getDayOfWeek().getValue() >= 6; // Saturday=6, Sunday=7
            
            JPanel dayCell = createDayCell(day, date, tasksOnDate, isToday, isWeekend);
            calendarGrid.add(dayCell);
        }
        
        // Fill remaining cells
        int totalCells = dayOfWeek + daysInMonth;
        int remainingCells = 42 - totalCells; // 6 rows * 7 columns = 42
        for (int i = 0; i < remainingCells; i++) {
            calendarGrid.add(createEmptyDayCell());
        }
        
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }
    
    private JPanel createEmptyDayCell() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return panel;
    }
    
    private JPanel createDayCell(int day, LocalDate date, List<Task> tasks, boolean isToday, boolean isWeekend) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        // Background color
        if (isToday) {
            panel.setBackground(TODAY_COLOR);
        } else if (isWeekend) {
            panel.setBackground(WEEKEND_COLOR);
        } else {
            panel.setBackground(Color.WHITE);
        }
        
        // Day number
        JLabel dayLabel = new JLabel(String.valueOf(day));
        dayLabel.setFont(new Font(Font.SANS_SERIF, isToday ? Font.BOLD : Font.PLAIN, 14));
        dayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(dayLabel);
        
        // Sort tasks by priority
        tasks.sort((t1, t2) -> t2.getPriority().compareTo(t1.getPriority()));
        
        // Add task indicators (max 4 visible)
        int maxVisible = 4;
        int taskCount = 0;
        for (Task task : tasks) {
            if (taskCount >= maxVisible) {
                JLabel moreLabel = new JLabel("+" + (tasks.size() - maxVisible) + " more");
                moreLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
                moreLabel.setForeground(Color.GRAY);
                moreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                moreLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                panel.add(moreLabel);
                break;
            }
            
            JPanel taskIndicator = createTaskIndicator(task);
            panel.add(taskIndicator);
            taskCount++;
        }
        
        // Make the cell clickable
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showTasksForDate(date, tasks);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(new LineBorder(new Color(70, 130, 180), 2));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
            }
        });
        
        return panel;
    }
    
    private JPanel createTaskIndicator(Task task) {
        JPanel indicator = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        indicator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        indicator.setAlignmentX(Component.LEFT_ALIGNMENT);
        indicator.setOpaque(false);
        
        // Color dot
        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(8, 8));
        dot.setBackground(getPriorityColor(task.getPriority()));
        dot.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        
        // Task title (truncated)
        String title = task.getTitle();
        if (title.length() > 20) {
            title = title.substring(0, 17) + "...";
        }
        
        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        label.setToolTipText(task.getTitle());
        
        indicator.add(dot);
        indicator.add(label);
        
        return indicator;
    }
    
    private Color getPriorityColor(Priority priority) {
        switch (priority) {
            case URGENT:
                return URGENT_COLOR;
            case HIGH:
                return HIGH_COLOR;
            case MEDIUM:
                return MEDIUM_COLOR;
            case LOW:
                return LOW_COLOR;
            default:
                return Color.GRAY;
        }
    }
    
    private void showTasksForDate(LocalDate date, List<Task> tasks) {
        if (tasks.isEmpty()) {
            // Show create task dialog for this date
            int result = JOptionPane.showConfirmDialog(this,
                "No tasks due on " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + 
                "\n\nWould you like to create a new task for this date?",
                "No Tasks",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Open task creation dialog with pre-filled due date
                TaskDialog dialog = new TaskDialog(
                    (Window) SwingUtilities.getWindowAncestor(this),
                    taskService,
                    userService,
                    null,
                    currentUser
                );
                dialog.setVisible(true);
                
                if (dialog.isTaskSaved()) {
                    refreshCalendar();
                }
            }
            return;
        }
        
        // Show dialog with list of tasks
        JDialog tasksDialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), 
            "Tasks for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), 
            Dialog.ModalityType.APPLICATION_MODAL);
        tasksDialog.setSize(500, 400);
        tasksDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel header = new JLabel("Tasks Due: " + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.add(header, BorderLayout.NORTH);
        
        // Task list
        DefaultListModel<Task> listModel = new DefaultListModel<>();
        for (Task task : tasks) {
            listModel.addElement(task);
        }
        
        JList<Task> taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskListCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Double-click to edit
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Task selectedTask = taskList.getSelectedValue();
                    if (selectedTask != null) {
                        openTaskDialog(selectedTask);
                        tasksDialog.dispose();
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> {
            Task selectedTask = taskList.getSelectedValue();
            if (selectedTask != null) {
                openTaskDialog(selectedTask);
                tasksDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(tasksDialog, "Please select a task to edit");
            }
        });
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> tasksDialog.dispose());
        
        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        tasksDialog.add(panel);
        tasksDialog.setVisible(true);
    }
    
    private void openTaskDialog(Task task) {
        TaskDialog dialog = new TaskDialog(
            (Window) SwingUtilities.getWindowAncestor(this),
            taskService,
            userService,
            task,
            currentUser
        );
        dialog.setVisible(true);
        
        if (dialog.isTaskSaved()) {
            refreshCalendar();
        }
    }
    
    // Custom cell renderer for task list in dialog
    private class TaskListCellRenderer extends JPanel implements ListCellRenderer<Task> {
        private JLabel titleLabel;
        private JLabel priorityLabel;
        private JLabel statusLabel;
        
        public TaskListCellRenderer() {
            setLayout(new BorderLayout(10, 5));
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            
            titleLabel = new JLabel();
            titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            
            JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            detailsPanel.setOpaque(false);
            
            priorityLabel = new JLabel();
            priorityLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            priorityLabel.setOpaque(true);
            priorityLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            
            statusLabel = new JLabel();
            statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            
            detailsPanel.add(priorityLabel);
            detailsPanel.add(statusLabel);
            
            add(titleLabel, BorderLayout.NORTH);
            add(detailsPanel, BorderLayout.CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task,
                int index, boolean isSelected, boolean cellHasFocus) {
            titleLabel.setText(task.getTitle());
            priorityLabel.setText(task.getPriority().toString());
            priorityLabel.setBackground(getPriorityColor(task.getPriority()));
            priorityLabel.setForeground(Color.WHITE);
            statusLabel.setText("Status: " + task.getStatus());
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            
            return this;
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Reload tasks with the new user
        if (user != null) {
            loadTasks();
        }
    }
    
    public void refresh() {
        refreshCalendar();
    }
}
