package com.worksphere.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.worksphere.dao.TaskDAO;
import com.worksphere.dao.UserDAO;
import com.worksphere.model.Task;
import com.worksphere.model.User;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.sql.SQLException;

/**
 * Main GUI Application for WorkSphere
 */
public class WorkSphereGUI extends JFrame {
    
    private TaskService taskService;
    private UserService userService;
    private User currentUser;
    
    // Main panels
    private JTabbedPane mainTabbedPane;
    private TaskListPanel taskListPanel;
    private DashboardPanel dashboardPanel;
    private UserManagementPanel userManagementPanel;
    private KanbanBoardPanel kanbanBoardPanel;
    private PomodoroTimerPanel pomodoroTimerPanel;
    private AnalyticsDashboardPanel analyticsDashboardPanel;
    
    // Menu and toolbar
    private JMenuBar menuBar;
    private JToolBar toolBar;
    
    public WorkSphereGUI() {
        try {
            // Initialize services
            this.taskService = new TaskService();
            this.userService = new UserService();
            
            // Initialize GUI
            initializeGUI();
            showLoginDialog();
            
        } catch (Exception e) {
            e.printStackTrace(); // Add detailed stack trace
            System.err.println("Exception details: " + e.getClass().getName() + ": " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Failed to start application: " + e.getMessage() + "\n\nCheck console for details.",
                "Startup Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void initializeGUI() {
        setTitle("WorkSphere - Task Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 600); // Further reduced height for compact display
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        
        // Set window background
        getContentPane().setBackground(Color.WHITE);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Use standard font sizes - let Windows handle DPI scaling
            Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
            Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
            Font tableFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", buttonFont);
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("TextArea.font", defaultFont);
            UIManager.put("List.font", defaultFont);
            UIManager.put("Table.font", tableFont);
            UIManager.put("TableHeader.font", boldFont);
            UIManager.put("Menu.font", defaultFont);
            UIManager.put("MenuItem.font", defaultFont);
            UIManager.put("TabbedPane.font", titleFont);
            UIManager.put("ToolTip.font", defaultFont);
            UIManager.put("ComboBox.font", defaultFont);
            UIManager.put("CheckBox.font", defaultFont);
            UIManager.put("RadioButton.font", defaultFont);
            UIManager.put("TitledBorder.font", boldFont);
            
            // Use standard component sizes - let Windows handle DPI scaling
            UIManager.put("Table.rowHeight", 22);
            UIManager.put("Button.arc", 6);
            
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create menu bar
        createMenuBar();
        
        // Create toolbar
        createToolBar();
        
        // Create main content area
        createMainContent();
        
        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        
        // Set minimum size
        setMinimumSize(new Dimension(800, 500));
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem newTaskItem = new JMenuItem("New Task");
        newTaskItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newTaskItem.addActionListener(e -> showNewTaskDialog());
        
        JMenuItem newRecurringTaskItem = new JMenuItem("New Recurring Task");
        newRecurringTaskItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift N"));
        newRecurringTaskItem.addActionListener(e -> showNewRecurringTaskDialog());
        
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshAllPanels());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(newTaskItem);
        fileMenu.add(newRecurringTaskItem);
        fileMenu.addSeparator();
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        
        JMenuItem dashboardItem = new JMenuItem("Dashboard");
        dashboardItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(0));
        
        JMenuItem tasksItem = new JMenuItem("Tasks");
        tasksItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(1));
        
        JMenuItem kanbanItem = new JMenuItem("Kanban Board");
        kanbanItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(2));
        
        JMenuItem pomodoroItem = new JMenuItem("Pomodoro Timer");
        pomodoroItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(3));
        
        JMenuItem analyticsItem = new JMenuItem("Analytics");
        analyticsItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(4));
        
        // Users menu item will be added conditionally in updateMenuBasedOnUserRole()
        
        viewMenu.add(dashboardItem);
        viewMenu.add(tasksItem);
        viewMenu.add(kanbanItem);
        viewMenu.add(pomodoroItem);
        viewMenu.add(analyticsItem);
        // Don't add usersItem here - it will be added conditionally
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // New Task button
        JButton newTaskBtn = new JButton("+ New Task");
        newTaskBtn.setBackground(new Color(40, 167, 69));
        newTaskBtn.setForeground(Color.WHITE);
        newTaskBtn.setFocusPainted(false);
        newTaskBtn.addActionListener(e -> showNewTaskDialog());
        
        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(108, 117, 125));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshAllPanels());
        
        // Export button
        JButton exportBtn = new JButton("Export");
        exportBtn.setBackground(new Color(0, 123, 255));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> showExportDialog());
        
        // User info
        JLabel userLabel = new JLabel("Not logged in");
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> logout());
        
        toolBar.add(newTaskBtn);
        toolBar.add(refreshBtn);
        toolBar.add(exportBtn);
        toolBar.addSeparator();
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(userLabel);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(logoutBtn);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private void createMainContent() {
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setBackground(Color.WHITE);
        mainTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainTabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Create panels
        dashboardPanel = new DashboardPanel(taskService, userService, currentUser);
        taskListPanel = new TaskListPanel(taskService, userService, currentUser, this);
        userManagementPanel = new UserManagementPanel(userService, this);
        kanbanBoardPanel = new KanbanBoardPanel(taskService, userService, currentUser);
        pomodoroTimerPanel = new PomodoroTimerPanel();
        analyticsDashboardPanel = new AnalyticsDashboardPanel(currentUser);
        
        // Add basic tabs - Users tab will be added in updateTabsBasedOnUserRole()
        mainTabbedPane.addTab("Dashboard", dashboardPanel);
        mainTabbedPane.addTab("Tasks", taskListPanel);
        mainTabbedPane.addTab("Kanban Board", kanbanBoardPanel);
        mainTabbedPane.addTab("Pomodoro Timer", pomodoroTimerPanel);
        mainTabbedPane.addTab("Analytics", analyticsDashboardPanel);
        
        // Set initial tab tooltips
        mainTabbedPane.setToolTipTextAt(0, "View task statistics and overview");
        mainTabbedPane.setToolTipTextAt(1, "Manage and organize your tasks");
        mainTabbedPane.setToolTipTextAt(2, "Drag and drop task management board");
        mainTabbedPane.setToolTipTextAt(3, "Focus timer for productive work sessions");
        mainTabbedPane.setToolTipTextAt(4, "Detailed analytics and productivity insights");
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void updateTabsBasedOnUserRole() {
        // Remove Users tab if it exists
        for (int i = mainTabbedPane.getTabCount() - 1; i >= 0; i--) {
            if ("Users".equals(mainTabbedPane.getTitleAt(i))) {
                mainTabbedPane.removeTabAt(i);
                break;
            }
        }
        
        // Add Users tab only if current user is admin
        if (currentUser != null && currentUser.isAdmin()) {
            mainTabbedPane.addTab("Users", userManagementPanel);
            mainTabbedPane.setToolTipTextAt(mainTabbedPane.getTabCount() - 1, "Manage users and permissions");
        }
        
        // Update menu items as well
        updateMenuBasedOnUserRole();
    }
    
    private void updateMenuBasedOnUserRole() {
        JMenuBar menuBar = getJMenuBar();
        if (menuBar != null) {
            // Find the View menu
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                if (menu != null && "View".equals(menu.getText())) {
                    // Remove existing Users menu item
                    for (int j = menu.getItemCount() - 1; j >= 0; j--) {
                        JMenuItem item = menu.getItem(j);
                        if (item != null && "Users".equals(item.getText())) {
                            menu.remove(j);
                            break;
                        }
                    }
                    
                    // Add Users menu item only for admins
                    if (currentUser != null && currentUser.isAdmin()) {
                        JMenuItem usersItem = new JMenuItem("Users");
                        usersItem.addActionListener(e -> {
                            // Find Users tab index
                            for (int k = 0; k < mainTabbedPane.getTabCount(); k++) {
                                if ("Users".equals(mainTabbedPane.getTitleAt(k))) {
                                    mainTabbedPane.setSelectedIndex(k);
                                    break;
                                }
                            }
                        });
                        menu.add(usersItem);
                    }
                    break;
                }
            }
        }
    }
    
    private void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(this, userService);
        loginDialog.setVisible(true);
        
        if (loginDialog.isLoginSuccessful()) {
            this.currentUser = loginDialog.getLoggedInUser();
            updateUserInfo();
            refreshAllPanels();
            setVisible(true);
        } else {
            System.exit(0);
        }
    }
    
    private void updateUserInfo() {
        if (currentUser != null) {
            // Update toolbar user label
            Component[] components = toolBar.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    String text = label.getText();
                    // Check if this is the user label (contains "User:" or "Not logged in")
                    if (text.contains("logged in") || text.startsWith("User:")) {
                        label.setText("User: " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")");
                        break;
                    }
                }
            }
            
            // Update panels with current user
            if (dashboardPanel != null) {
                dashboardPanel.setCurrentUser(currentUser);
            }
            if (taskListPanel != null) {
                taskListPanel.setCurrentUser(currentUser);
            }
            if (kanbanBoardPanel != null) {
                kanbanBoardPanel.setCurrentUser(currentUser);
            }
            
            // Update tabs and menu based on user role
            updateTabsBasedOnUserRole();
        }
    }
    
    private void refreshAllPanels() {
        if (dashboardPanel != null) {
            dashboardPanel.refresh();
        }
        if (taskListPanel != null) {
            taskListPanel.refresh();
        }
        if (kanbanBoardPanel != null) {
            kanbanBoardPanel.refresh();
        }
        if (userManagementPanel != null) {
            userManagementPanel.refresh();
        }
        if (analyticsDashboardPanel != null && currentUser != null) {
            analyticsDashboardPanel.refreshData();
        }
    }
    
    private void showNewTaskDialog() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, 
                "Please login first!", 
                "Authentication Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaskDialog taskDialog = new TaskDialog(this, taskService, userService, null);
        taskDialog.setVisible(true);
        
        if (taskDialog.isTaskSaved()) {
            refreshAllPanels();
        }
    }
    
    private void showNewRecurringTaskDialog() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, 
                "Please login first!", 
                "Authentication Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        RecurringTaskDialog dialog = new RecurringTaskDialog(this, taskService, userService, currentUser);
        dialog.setVisible(true);
        
        if (dialog.isTaskSaved()) {
            refreshAllPanels();
        }
    }
    
    private void showExportDialog() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, 
                "Please login first!", 
                "Authentication Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String[] options = {"CSV Export", "Excel Export", "PDF Report", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "Choose export format:",
            "Export Data",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        try {
            switch (choice) {
                case 0: // CSV Export
                    exportToCSV();
                    break;
                case 1: // Excel Export
                    exportToExcel();
                    break;
                case 2: // PDF Report
                    exportToPDF();
                    break;
                default:
                    // Cancel - do nothing
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setSelectedFile(new java.io.File("tasks_export.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                com.worksphere.service.ExportService exportService = new com.worksphere.service.ExportService();
                exportService.exportTasksToCSV(taskService.getAllTasks(), file.getAbsolutePath());
                
                JOptionPane.showMessageDialog(this,
                    "Tasks exported successfully to " + file.getName(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                throw new RuntimeException("CSV export failed", e);
            }
        }
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new java.io.File("tasks_export.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                com.worksphere.service.ExportService exportService = new com.worksphere.service.ExportService();
                exportService.exportTasksToExcel(taskService.getAllTasks(), file.getAbsolutePath());
                
                JOptionPane.showMessageDialog(this,
                    "Tasks exported successfully to " + file.getName(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                throw new RuntimeException("Excel export failed", e);
            }
        }
    }
    
    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new java.io.File("tasks_report.pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                com.worksphere.service.PDFReportService pdfService = new com.worksphere.service.PDFReportService();
                
                // Generate PDF with specific filename
                java.io.File generatedFile = pdfService.generateTaskReport(taskService.getAllTasks(), "Task Report", file.getParent());
                
                // Rename to user's chosen filename
                if (!generatedFile.renameTo(file)) {
                    // If rename fails, copy the content
                    java.nio.file.Files.copy(generatedFile.toPath(), file.toPath(), 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    generatedFile.delete();
                }
                
                JOptionPane.showMessageDialog(this,
                    "PDF report generated successfully: " + file.getName(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "PDF export failed: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void logout() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            this.currentUser = null;
            dispose();
            showLoginDialog();
        }
    }
    
    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    private void showAboutDialog() {
        String message = "WorkSphere\n\n" +
                        "Version: 1.0.0\n" +
                        "A comprehensive task management system\n" +
                        "Built with Java Swing and SQLite\n\n" +
                        "Features:\n" +
                        "• Task creation and management\n" +
                        "• User authentication and role-based access\n" +
                        "• Priority and status tracking\n" +
                        "• Due date management\n" +
                        "• Statistics and reporting\n" +
                        "• User management for administrators";
                        
        JOptionPane.showMessageDialog(this, message, "About WorkSphere", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private Image createAppIcon() {
        // Create a simple icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple task board icon
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRoundRect(2, 2, 28, 28, 6, 6);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(6, 6, 6, 8, 2, 2);
        g2d.fillRoundRect(14, 6, 6, 8, 2, 2);
        g2d.fillRoundRect(22, 6, 6, 8, 2, 2);
        
        g2d.dispose();
        return icon;
    }
    
    private Icon createIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                g2d.drawString(emoji, x, y + 12);
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 16; }
            
            @Override
            public int getIconHeight() { return 16; }
        };
    }
    
    // Getters for panels (used by dialogs)
    public TaskService getTaskService() { return taskService; }
    public UserService getUserService() { return userService; }
    public User getCurrentUser() { return currentUser; }
    
    /**
     * Set the current task for the Pomodoro timer and switch to timer tab
     */
    public void startPomodoroForTask(Task task) {
        if (pomodoroTimerPanel != null) {
            pomodoroTimerPanel.setCurrentTask(task);
            // Switch to Pomodoro Timer tab
            mainTabbedPane.setSelectedIndex(3);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WorkSphereGUI();
        });
    }
}
