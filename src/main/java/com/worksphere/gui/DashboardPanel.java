package com.worksphere.gui;

import com.worksphere.model.Priority;
import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.User;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Modern Dashboard panel with card-based design
 */
public class DashboardPanel extends JPanel {
    
    private TaskService taskService;
    private UserService userService;
    private User currentUser;
    
    // References to stat cards for updates
    private JLabel totalTasksValue;
    private JLabel completedValue;
    private JLabel thisWeekValue;
    private JLabel overdueValue;
    
    // References to progress bars
    private JProgressBar todoProgress;
    private JProgressBar inProgressProgress;
    private JProgressBar completedProgress;
    private JProgressBar lowPriorityProgress;
    private JProgressBar mediumPriorityProgress;
    private JProgressBar highPriorityProgress;
    private JProgressBar urgentPriorityProgress;
    
    private JPanel recentTasksContainer;
    
    public DashboardPanel(TaskService taskService, UserService userService, User currentUser) {
        this.taskService = taskService;
        this.userService = userService;
        this.currentUser = currentUser;
        
        initializePanel();
        refresh();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with cards - wrapped in scroll pane for better navigation
        JPanel mainPanel = createMainPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(52, 58, 64));
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        refreshButton.addActionListener(e -> refresh());
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createMainPanel() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(248, 249, 250));
        
        // Stats cards row
        JPanel statsRow = createStatsCardsPanel();
        main.add(statsRow);
        main.add(Box.createVerticalStrut(20));
        
        // Charts row
        JPanel chartsRow = createChartsPanel();
        main.add(chartsRow);
        main.add(Box.createVerticalStrut(20));
        
        // Recent tasks section
        JPanel recentTasksPanel = createRecentTasksPanel();
        main.add(recentTasksPanel);
        
        return main;
    }
    
    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        panel.add(createStatCard("📋", "Total Tasks", totalTasksValue = new JLabel("0"), new Color(52, 144, 220)));
        panel.add(createStatCard("✅", "Completed", completedValue = new JLabel("0"), new Color(40, 167, 69)));
        panel.add(createStatCard("⏰", "This Week", thisWeekValue = new JLabel("0"), new Color(255, 193, 7)));
        panel.add(createStatCard("⚠️", "Overdue", overdueValue = new JLabel("0"), new Color(220, 53, 69)));
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Icon and title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Value
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400)); // Increased height
        
        panel.add(createChartCard("Task Status Distribution", createTaskStatusChart()));
        panel.add(createChartCard("Priority Distribution", createPriorityChart()));
        
        return panel;
    }
    
    private JPanel createChartCard(String title, JPanel chartContent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Header
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Increased font size
        headerLabel.setForeground(new Color(52, 58, 64));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25)); // Increased padding
        
        card.add(headerLabel, BorderLayout.NORTH);
        card.add(chartContent, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTaskStatusChart() {
        JPanel chart = new JPanel();
        chart.setBackground(Color.WHITE);
        chart.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        chart.setLayout(new BoxLayout(chart, BoxLayout.Y_AXIS));
        
        chart.add(createProgressBar("To Do", todoProgress = new JProgressBar(0, 100), new Color(220, 53, 69)));
        chart.add(Box.createVerticalStrut(10));
        chart.add(createProgressBar("In Progress", inProgressProgress = new JProgressBar(0, 100), new Color(255, 193, 7)));
        chart.add(Box.createVerticalStrut(10));
        chart.add(createProgressBar("Completed", completedProgress = new JProgressBar(0, 100), new Color(40, 167, 69)));
        
        return chart;
    }
    
    private JPanel createPriorityChart() {
        JPanel chart = new JPanel();
        chart.setBackground(Color.WHITE);
        chart.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        chart.setLayout(new BoxLayout(chart, BoxLayout.Y_AXIS));
        
        chart.add(createProgressBar("Low", lowPriorityProgress = new JProgressBar(0, 100), new Color(40, 167, 69)));
        chart.add(Box.createVerticalStrut(10));
        chart.add(createProgressBar("Medium", mediumPriorityProgress = new JProgressBar(0, 100), new Color(255, 193, 7)));
        chart.add(Box.createVerticalStrut(10));
        chart.add(createProgressBar("High", highPriorityProgress = new JProgressBar(0, 100), new Color(255, 133, 27)));
        chart.add(Box.createVerticalStrut(10));
        chart.add(createProgressBar("Urgent", urgentPriorityProgress = new JProgressBar(0, 100), new Color(220, 53, 69)));
        
        return chart;
    }
    
    private JPanel createProgressBar(String label, JProgressBar progressBar, Color color) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Increased font size
        nameLabel.setPreferredSize(new Dimension(120, 25)); // Increased size
        
        progressBar.setForeground(color);
        progressBar.setBackground(new Color(233, 236, 239));
        progressBar.setPreferredSize(new Dimension(250, 25)); // Increased height
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true); // Enable string painting to show percentage
        progressBar.setString("0%"); // Set initial text
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Set progress bar font
        
        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Increased font size
        valueLabel.setForeground(new Color(108, 117, 125));
        valueLabel.setPreferredSize(new Dimension(60, 25)); // Increased size
        
        container.add(nameLabel, BorderLayout.WEST);
        container.add(progressBar, BorderLayout.CENTER);
        container.add(valueLabel, BorderLayout.EAST);
        
        return container;
    }
    
    private JPanel createRecentTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        // Remove max height restriction to allow full display
        
        // Header
        JLabel headerLabel = new JLabel("Recent Tasks");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(new Color(52, 58, 64));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Task list container - no scroll pane, display all tasks
        recentTasksContainer = new JPanel();
        recentTasksContainer.setLayout(new BoxLayout(recentTasksContainer, BoxLayout.Y_AXIS));
        recentTasksContainer.setBackground(Color.WHITE);
        recentTasksContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(recentTasksContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void refresh() {
        try {
            if (currentUser == null) {
                // If no user is logged in yet, don't try to load data
                return;
            }
            
            List<Task> tasks = currentUser.isAdmin() ? 
                taskService.getAllTasks() : 
                taskService.getTasksForUser(currentUser.getId());
                
            updateStatCards(tasks);
            updateCharts(tasks);
            updateRecentTasks(tasks);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error refreshing dashboard: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatCards(List<Task> tasks) {
        totalTasksValue.setText(String.valueOf(tasks.size()));
        
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        completedValue.setText(String.valueOf(completed));
        
        long thisWeek = tasks.stream()
            .filter(t -> t.getDueDate() != null && 
                        t.getDueDate().isAfter(LocalDate.now().minusDays(7)))
            .count();
        thisWeekValue.setText(String.valueOf(thisWeek));
        
        long overdue = tasks.stream()
            .filter(t -> t.getDueDate() != null && 
                        t.getDueDate().isBefore(LocalDate.now()) && 
                        t.getStatus() != TaskStatus.COMPLETED)
            .count();
        overdueValue.setText(String.valueOf(overdue));
    }
    
    private void updateCharts(List<Task> tasks) {
        int total = tasks.size();
        if (total == 0) {
            todoProgress.setValue(0);
            todoProgress.setString("0% (0)");
            inProgressProgress.setValue(0);
            inProgressProgress.setString("0% (0)");
            completedProgress.setValue(0);
            completedProgress.setString("0% (0)");
            lowPriorityProgress.setValue(0);
            lowPriorityProgress.setString("0% (0)");
            mediumPriorityProgress.setValue(0);
            mediumPriorityProgress.setString("0% (0)");
            highPriorityProgress.setValue(0);
            highPriorityProgress.setString("0% (0)");
            urgentPriorityProgress.setValue(0);
            urgentPriorityProgress.setString("0% (0)");
            return;
        }
        
        // Update status chart
        long todoCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        
        int todoPercent = (int) (todoCount * 100 / total);
        int inProgressPercent = (int) (inProgressCount * 100 / total);
        int completedPercent = (int) (completedCount * 100 / total);
        
        todoProgress.setValue(todoPercent);
        todoProgress.setString(todoPercent + "% (" + todoCount + ")");
        inProgressProgress.setValue(inProgressPercent);
        inProgressProgress.setString(inProgressPercent + "% (" + inProgressCount + ")");
        completedProgress.setValue(completedPercent);
        completedProgress.setString(completedPercent + "% (" + completedCount + ")");
        
        // Update priority chart
        long lowCount = tasks.stream().filter(t -> t.getPriority() == Priority.LOW).count();
        long mediumCount = tasks.stream().filter(t -> t.getPriority() == Priority.MEDIUM).count();
        long highCount = tasks.stream().filter(t -> t.getPriority() == Priority.HIGH).count();
        long urgentCount = tasks.stream().filter(t -> t.getPriority() == Priority.URGENT).count();
        
        int lowPercent = (int) (lowCount * 100 / total);
        int mediumPercent = (int) (mediumCount * 100 / total);
        int highPercent = (int) (highCount * 100 / total);
        int urgentPercent = (int) (urgentCount * 100 / total);
        
        lowPriorityProgress.setValue(lowPercent);
        lowPriorityProgress.setString(lowPercent + "% (" + lowCount + ")");
        mediumPriorityProgress.setValue(mediumPercent);
        mediumPriorityProgress.setString(mediumPercent + "% (" + mediumCount + ")");
        highPriorityProgress.setValue(highPercent);
        highPriorityProgress.setString(highPercent + "% (" + highCount + ")");
        urgentPriorityProgress.setValue(urgentPercent);
        urgentPriorityProgress.setString(urgentPercent + "% (" + urgentCount + ")");
    }
    
    private void updateRecentTasks(List<Task> tasks) {
        recentTasksContainer.removeAll();
        
        tasks.stream()
            .limit(5)
            .forEach(task -> {
                JPanel taskItem = createTaskItem(task);
                recentTasksContainer.add(taskItem);
                recentTasksContainer.add(Box.createVerticalStrut(8));
            });
            
        recentTasksContainer.revalidate();
        recentTasksContainer.repaint();
    }
    
    private JPanel createTaskItem(Task task) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(new Color(248, 249, 250));
        item.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        String status = task.getStatus().getDisplayName();
        String priority = task.getPriority().getDisplayName();
        JLabel detailsLabel = new JLabel(status + " • " + priority);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsLabel.setForeground(new Color(108, 117, 125));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(detailsLabel);
        
        item.add(leftPanel, BorderLayout.WEST);
        
        if (task.getDueDate() != null) {
            JLabel dueDateLabel = new JLabel(task.getDueDate().toString());
            dueDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dueDateLabel.setForeground(new Color(108, 117, 125));
            item.add(dueDateLabel, BorderLayout.EAST);
        }
        
        return item;
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        refresh(); // Refresh the dashboard with the new user
    }
}