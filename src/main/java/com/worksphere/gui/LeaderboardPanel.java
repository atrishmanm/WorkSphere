package com.worksphere.gui;

import com.worksphere.service.LeaderboardService;
import com.worksphere.service.LeaderboardService.UserRanking;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel displaying user leaderboard rankings with trophy icons and statistics
 * Enhanced UI with modern design and better visual hierarchy
 */
public class LeaderboardPanel extends JPanel {
    private LeaderboardService leaderboardService;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private JPanel tabButtonPanel;
    private JLabel titleLabel;
    private String currentPeriod = "all-time";
    
    public LeaderboardPanel(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
        initializeUI();
        loadAllTimeRankings(); // Default view
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with title and period selector
        JPanel headerPanel = createEnhancedHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content area with table
        JPanel contentPanel = createLeaderboardContent();
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer with legend
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createEnhancedHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(245, 247, 250));
        
        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(245, 247, 250));
        
        titleLabel = new JLabel("Leaderboard - Top Performers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(32, 45, 64));
        
        JLabel subtitleLabel = new JLabel("Rankings based on completed tasks and performance metrics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(128, 128, 128));
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        
        // Period selector buttons (modern tab style)
        tabButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tabButtonPanel.setBackground(new Color(245, 247, 250));
        
        JButton allTimeBtn = createTabButton("All Time", "all-time", true);
        JButton monthBtn = createTabButton("This Month", "monthly", false);
        JButton weekBtn = createTabButton("This Week", "weekly", false);
        
        tabButtonPanel.add(allTimeBtn);
        tabButtonPanel.add(monthBtn);
        tabButtonPanel.add(weekBtn);
        
        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(tabButtonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JButton createTabButton(String text, String period, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (selected) {
            button.setBackground(new Color(59, 130, 246));
            button.setForeground(Color.WHITE);
            currentPeriod = period;
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(100, 100, 100));
        }
        
        button.addActionListener(e -> {
            // Update all buttons
            Component[] components = tabButtonPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(new Color(100, 100, 100));
                }
            }
            
            // Highlight selected button
            button.setBackground(new Color(59, 130, 246));
            button.setForeground(Color.WHITE);
            currentPeriod = period;
            
            // Load rankings for selected period
            switch (period) {
                case "all-time": loadAllTimeRankings(); break;
                case "monthly": loadMonthlyRankings(); break;
                case "weekly": loadWeeklyRankings(); break;
            }
        });
        
        return button;
    }
    
    private JPanel createLeaderboardContent() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Table columns
        String[] columns = {"Rank", "User", "Points", "Tasks", "Completion Rate", "Avg Time (hrs)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setRowHeight(60);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leaderboardTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        leaderboardTable.getTableHeader().setBackground(new Color(248, 249, 250));
        leaderboardTable.getTableHeader().setForeground(new Color(50, 50, 50));
        leaderboardTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        leaderboardTable.setSelectionBackground(new Color(230, 240, 255));
        leaderboardTable.setGridColor(new Color(240, 240, 240));
        leaderboardTable.setShowVerticalLines(true);
        leaderboardTable.setShowHorizontalLines(true);
        leaderboardTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(250); // User
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Points
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Tasks
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Rate
        leaderboardTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Avg Time
        
        // Custom renderer for rank column (with trophies and badges)
        leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(new EnhancedRankCellRenderer());
        
        // Custom renderer for user column
        leaderboardTable.getColumnModel().getColumn(1).setCellRenderer(new UserCellRenderer());
        
        // Center align for numeric columns with custom styling
        leaderboardTable.getColumnModel().getColumn(2).setCellRenderer(new PointsCellRenderer());
        leaderboardTable.getColumnModel().getColumn(3).setCellRenderer(new NumericCellRenderer());
        leaderboardTable.getColumnModel().getColumn(4).setCellRenderer(new PercentageCellRenderer());
        leaderboardTable.getColumnModel().getColumn(5).setCellRenderer(new NumericCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        
        JLabel legendLabel = new JLabel("<html><b>Points System:</b> " +
            "Completed Task (+10) | Urgent Task (+20 bonus) | On-Time (+5 bonus) | Overdue (-5 penalty)</html>");
        legendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legendLabel.setForeground(new Color(100, 100, 100));
        
        panel.add(legendLabel);
        
        return panel;
    }
    
    private void loadAllTimeRankings() {
        List<UserRanking> rankings = leaderboardService.getTopUsers(10, "all-time");
        updateTable(rankings);
    }
    
    private void loadMonthlyRankings() {
        List<UserRanking> rankings = leaderboardService.getTopUsers(10, "monthly");
        updateTable(rankings);
    }
    
    private void loadWeeklyRankings() {
        List<UserRanking> rankings = leaderboardService.getTopUsers(10, "weekly");
        updateTable(rankings);
    }
    
    private void updateTable(List<UserRanking> rankings) {
        tableModel.setRowCount(0); // Clear existing rows
        
        System.out.println("📊 Updating leaderboard table with " + rankings.size() + " rankings");
        
        if (rankings.isEmpty()) {
            // Show empty state
            System.out.println("📊 Rankings list is empty, showing 'No data' message");
            tableModel.addRow(new Object[]{"--", "No data available", "--", "--", "--", "--"});
            return;
        }
        
        for (UserRanking ranking : rankings) {
            Object[] row = new Object[6];
            row[0] = ranking.getRank();
            row[1] = String.format("%s (%s)", ranking.getFullName(), ranking.getUsername());
            row[2] = ranking.getPoints();
            row[3] = ranking.getTasksCompleted();
            row[4] = String.format("%.1f%%", ranking.getCompletionRate());
            row[5] = ranking.getAvgCompletionTimeHours();
            
            System.out.println("📊 Adding row: " + ranking.getUsername() + " - " + ranking.getPoints() + " points, " + ranking.getTasksCompleted() + " tasks");
            tableModel.addRow(row);
        }
        
        System.out.println("📊 Table now has " + tableModel.getRowCount() + " rows");
    }
    
    /**
     * Refresh the currently displayed rankings
     */
    public void refreshRankings() {
        switch (currentPeriod) {
            case "all-time": loadAllTimeRankings(); break;
            case "monthly": loadMonthlyRankings(); break;
            case "weekly": loadWeeklyRankings(); break;
        }
    }
    
    /**
     * Enhanced cell renderer for the rank column with trophy icons and gradient backgrounds
     */
    private class EnhancedRankCellRenderer extends JPanel implements TableCellRenderer {
        private JLabel rankLabel;
        private int rank;
        
        public EnhancedRankCellRenderer() {
            setLayout(new BorderLayout());
            rankLabel = new JLabel();
            rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rankLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            add(rankLabel, BorderLayout.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            if (value instanceof Integer) {
                rank = (Integer) value;
                
                switch (rank) {
                    case 1:
                        rankLabel.setText("#1");
                        setBackground(new Color(255, 223, 0, 80));
                        rankLabel.setForeground(new Color(180, 130, 0));
                        setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(255, 215, 0)));
                        break;
                    case 2:
                        rankLabel.setText("#2");
                        setBackground(new Color(192, 192, 192, 60));
                        rankLabel.setForeground(new Color(100, 100, 100));
                        setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(192, 192, 192)));
                        break;
                    case 3:
                        rankLabel.setText("#3");
                        setBackground(new Color(205, 127, 50, 60));
                        rankLabel.setForeground(new Color(139, 69, 19));
                        setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(205, 127, 50)));
                        break;
                    default:
                        rankLabel.setText("#" + rank);
                        setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                        rankLabel.setForeground(new Color(80, 80, 80));
                        setBorder(null);
                }
            }
            
            return this;
        }
    }
    
    /**
     * Custom cell renderer for user names with icons
     */
    private class UserCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                setText(value.toString());
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
            
            setHorizontalAlignment(SwingConstants.LEFT);
            return c;
        }
    }
    
    /**
     * Custom cell renderer for points with star icons
     */
    private class PointsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Integer) {
                int points = (Integer) value;
                setText(String.valueOf(points));
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(new Color(59, 130, 246));
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
    
    /**
     * Custom cell renderer for numeric values
     */
    private class NumericCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
    
    /**
     * Custom cell renderer for percentage values with progress bar style
     */
    private class PercentageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String percentStr = value.toString();
                setText(percentStr);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                // Color code based on completion rate
                if (percentStr.contains("100.0")) {
                    setForeground(new Color(34, 197, 94)); // Green
                } else if (percentStr.contains("0.0")) {
                    setForeground(new Color(239, 68, 68)); // Red
                } else {
                    setForeground(new Color(251, 146, 60)); // Orange
                }
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
}
