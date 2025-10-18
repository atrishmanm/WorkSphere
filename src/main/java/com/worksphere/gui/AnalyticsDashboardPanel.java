package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.AnalyticsService;
import com.worksphere.service.AnalyticsService.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.worksphere.util.NotificationUtil;

/**
 * Analytics Dashboard Panel for displaying productivity metrics and charts
 */
public class AnalyticsDashboardPanel extends JPanel {
    
    private AnalyticsService analyticsService;
    private User currentUser;
    
    // Date range controls
    private JComboBox<String> dateRangeCombo;
    private JButton refreshButton;
    private JButton exportButton;
    
    // KPI Metrics
    private JLabel totalTasksLabel;
    private JLabel completedTasksLabel;
    private JLabel completionRateLabel;
    private JLabel avgCompletionLabel;
    private JLabel timeEfficiencyLabel;
    private JLabel overdueCountLabel;
    
    // Insights
    private JTextArea insightsArea;
    
    // Chart panels
    private ChartPanel completionTrendChart;
    private ChartPanel priorityDistributionChart;
    private ChartPanel teamPerformanceChart;
    private ChartPanel timeAccuracyChart;
    
    public AnalyticsDashboardPanel(User currentUser) {
        this.analyticsService = new AnalyticsService();
        this.currentUser = currentUser;
        
        initializeComponents();
        setupLayout();
        loadAnalytics();
    }
    
    private void initializeComponents() {
        // Date range selector
        dateRangeCombo = new JComboBox<>(new String[]{
            "Last 7 Days", "Last 30 Days", "Last 90 Days", "This Year", "All Time"
        });
        dateRangeCombo.setSelectedItem("Last 30 Days");
        dateRangeCombo.addActionListener(e -> loadAnalytics());
        
    refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAnalytics());
        
    exportButton = new JButton("Export");
        exportButton.setEnabled(false);
        
        // KPI Labels with large fonts
        Font kpiFont = new Font("Segoe UI", Font.BOLD, 28);
        
        totalTasksLabel = createStyledLabel(kpiFont, new Color(33, 150, 243));
        completedTasksLabel = createStyledLabel(kpiFont, new Color(76, 175, 80));
        completionRateLabel = createStyledLabel(kpiFont, new Color(255, 152, 0));
        avgCompletionLabel = createStyledLabel(kpiFont, new Color(156, 39, 176));
        timeEfficiencyLabel = createStyledLabel(kpiFont, new Color(3, 169, 244));
        overdueCountLabel = createStyledLabel(kpiFont, new Color(244, 67, 54));
        
        // Insights area
        insightsArea = new JTextArea(4, 30);
        insightsArea.setEditable(false);
        insightsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        insightsArea.setLineWrap(true);
        insightsArea.setWrapStyleWord(true);
        insightsArea.setBackground(new Color(255, 248, 225));
        insightsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private JLabel createStyledLabel(Font font, Color color) {
        JLabel label = new JLabel("0");
        label.setFont(font);
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));
        
        // TOP: Header with title and controls (fixed at top)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(240, 242, 245));
        
    JLabel titleLabel = new JLabel("Analytics & Performance Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.setBackground(new Color(240, 242, 245));
        controlsPanel.add(new JLabel("Period:"));
        controlsPanel.add(dateRangeCombo);
        controlsPanel.add(refreshButton);
        controlsPanel.add(exportButton);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(controlsPanel, BorderLayout.EAST);
        
        // CENTER: Scrollable content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(240, 242, 245));
        
        // Add all components to content panel
        contentPanel.add(createKPIPanel());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createInsightsPanel());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createChartsPanel());
        contentPanel.add(Box.createVerticalStrut(20)); // Bottom padding
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createKPIPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBackground(new Color(240, 242, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
    panel.add(createKPICard("Total Tasks", totalTasksLabel, "All tasks in period"));
    panel.add(createKPICard("Completed", completedTasksLabel, "Successfully finished"));
    panel.add(createKPICard("Completion Rate", completionRateLabel, "Success percentage"));
    panel.add(createKPICard("Avg Completion", avgCompletionLabel, "Days to complete"));
    panel.add(createKPICard("Time Efficiency", timeEfficiencyLabel, "Estimate accuracy"));
    panel.add(createKPICard("Overdue Tasks", overdueCountLabel, "Past due date"));
        
        return panel;
    }
    
    private JPanel createKPICard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(73, 80, 87));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createInsightsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
            "Key Insights & Recommendations",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(73, 80, 87)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JScrollPane scrollPane = new JScrollPane(insightsArea);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(0, 120));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)), 
            "Key Metrics", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(73, 80, 87)
        ));
        panel.setBackground(Color.WHITE);
        
        // Metric cards
        panel.add(createMetricCard("Total Tasks", totalTasksLabel, "Total"));
        panel.add(createMetricCard("Completed", completedTasksLabel, "Done"));
        panel.add(createMetricCard("Completion Rate", completionRateLabel, "Rate"));
        panel.add(createMetricCard("Avg Completion", avgCompletionLabel, "Days"));
        panel.add(createMetricCard("Efficiency", timeEfficiencyLabel, "Score"));
        
        return panel;
    }
    
    private JPanel createMetricCard(String title, JLabel valueLabel, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(icon + " " + title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBackground(new Color(240, 242, 245));
        panel.setPreferredSize(new Dimension(1200, 650));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 650));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Create charts
        completionTrendChart = createCompletionTrendChart();
        priorityDistributionChart = createPriorityDistributionChart();
        teamPerformanceChart = createTeamPerformanceChart();
        timeAccuracyChart = createTimeAccuracyChart();
        
        // Add wrapped charts
    panel.add(wrapChart(completionTrendChart, "Daily Completions"));
    panel.add(wrapChart(priorityDistributionChart, "Priority Distribution"));
    panel.add(wrapChart(teamPerformanceChart, "Team Performance"));
    panel.add(wrapChart(timeAccuracyChart, "Time Estimation Accuracy"));
        
        return panel;
    }
    
    private JPanel wrapChart(ChartPanel chartPanel, String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(73, 80, 87)
        ));
        wrapper.add(chartPanel, BorderLayout.CENTER);
        return wrapper;
    }
    
    private ChartPanel createCompletionTrendChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createLineChart(
            "", // Empty title - using wrapper border title
            "Date",
            "Tasks",
            dataset
        );
        
        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinePaint(new Color(240, 240, 240));
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        // Enhanced line renderer with gradient effect
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(33, 150, 243));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setSeriesShapesFilled(0, true);
        
        // Enable tooltips with custom format
        renderer.setDefaultToolTipGenerator(
            new org.jfree.chart.labels.StandardCategoryToolTipGenerator(
                "{1}: {2} tasks", java.text.NumberFormat.getInstance()
            )
        );
        
        plot.setRenderer(renderer);
        
        // Style axes
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMouseWheelEnabled(false); // Disable zoom on scroll
        chartPanel.setPopupMenu(null); // Remove right-click menu for cleaner look
        chartPanel.setDisplayToolTips(true); // Enable tooltips
        return chartPanel;
    }
    
    private ChartPanel createPriorityDistributionChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        JFreeChart chart = ChartFactory.createPieChart(
            "", dataset, true, true, false
        );
        
        // Customize pie chart
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);
        
        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        
        // Beautiful color scheme
        plot.setSectionPaint("URGENT", new Color(244, 67, 54));
        plot.setSectionPaint("HIGH", new Color(63, 81, 181));
        plot.setSectionPaint("MEDIUM", new Color(255, 193, 7));
        plot.setSectionPaint("LOW", new Color(76, 175, 80));
        
        // Add explode effect for visual appeal
        plot.setExplodePercent("URGENT", 0.05);
        
        // Enhanced labels
        plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setSimpleLabels(false);
        
        // Custom tooltip generator
        plot.setToolTipGenerator(
            new org.jfree.chart.labels.StandardPieToolTipGenerator(
                "{0}: {1} tasks ({2})", java.text.NumberFormat.getInstance(), 
                java.text.NumberFormat.getPercentInstance()
            )
        );
        
        // Legend styling
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMouseWheelEnabled(false); // Disable zoom on scroll
        chartPanel.setPopupMenu(null);
        chartPanel.setDisplayToolTips(true); // Enable tooltips
        return chartPanel;
    }
    
    private ChartPanel createTeamPerformanceChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            "", "User", "Tasks", dataset
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinePaint(new Color(240, 240, 240));
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        
        // Enhanced bar renderer with gradient
        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, new Color(76, 175, 80));
        renderer.setSeriesPaint(1, new Color(255, 152, 0));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.15);
        
        // Add spacing between bars
        renderer.setItemMargin(0.2);
        
        // Custom tooltip generator
        renderer.setDefaultToolTipGenerator(
            new org.jfree.chart.labels.StandardCategoryToolTipGenerator(
                "{0}: {2} tasks", java.text.NumberFormat.getInstance()
            )
        );
        
        plot.setRenderer(renderer);
        
        // Style axes
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        
        // Legend styling
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMouseWheelEnabled(false); // Disable zoom on scroll
        chartPanel.setPopupMenu(null);
        chartPanel.setDisplayToolTips(true); // Enable tooltips
        return chartPanel;
    }
    
    private ChartPanel createTimeAccuracyChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            "", "Metric", "Hours", dataset
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinePaint(new Color(240, 240, 240));
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        
        // Enhanced bar renderer
        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, new Color(33, 150, 243));
        renderer.setSeriesPaint(1, new Color(244, 67, 54));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.3);
        
        // Add spacing between bars
        renderer.setItemMargin(0.2);
        
        // Custom tooltip generator
        renderer.setDefaultToolTipGenerator(
            new org.jfree.chart.labels.StandardCategoryToolTipGenerator(
                "{0}: {2} hours", java.text.NumberFormat.getInstance()
            )
        );
        
        plot.setRenderer(renderer);
        
        // Style axes
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        
        // Legend styling
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMouseWheelEnabled(false); // Disable zoom on scroll
        chartPanel.setPopupMenu(null);
        chartPanel.setDisplayToolTips(true); // Enable tooltips
        return chartPanel;
    }
    
    private ChartPanel createCategoryPerformanceChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Category Performance",
            "Category",
            "Completion Rate %",
            dataset
        );
        
        // Customize bar chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.getRenderer().setSeriesPaint(0, new Color(156, 39, 176));
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Category Completion Rates"));
        return chartPanel;
    }
    
    private void loadAnalytics() {
        try {
            System.out.println("Loading analytics data...");
            LocalDate[] dateRange = getSelectedDateRange();
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];
            
            System.out.println("Date range: " + startDate + " to " + endDate);
            
            // Load productivity metrics
            ProductivityMetrics metrics = analyticsService.getProductivityMetrics(startDate, endDate);
            OverdueAnalysis overdueAnalysis = analyticsService.getOverdueAnalysis();
            
            updateKPIs(metrics, overdueAnalysis);
            updateInsights(metrics, overdueAnalysis);
            updateCompletionTrendChart(startDate, endDate);
            updatePriorityDistributionChart();
            updateTeamPerformanceChart();
            updateTimeAccuracyChart(metrics);
            
            System.out.println("Analytics loaded successfully!");
            
        } catch (Exception e) {
            System.err.println("Error loading analytics: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading analytics: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateKPIs(ProductivityMetrics metrics, OverdueAnalysis overdueAnalysis) {
        totalTasksLabel.setText(String.valueOf(metrics.totalTasks));
        completedTasksLabel.setText(String.valueOf(metrics.completedTasks));
        completionRateLabel.setText(String.format("%.1f%%", metrics.completionRate * 100));
        avgCompletionLabel.setText(String.format("%.1f days", metrics.averageCompletionDays));
        
        // Time efficiency (> 100% means faster than estimate)
        String efficiencyText = metrics.timeEfficiency > 0 ? 
            String.format("%.0f%%", metrics.timeEfficiency * 100) : "N/A";
        timeEfficiencyLabel.setText(efficiencyText);
        
        overdueCountLabel.setText(String.valueOf(overdueAnalysis.overdueTasks));
    }
    
    private void updateInsights(ProductivityMetrics metrics, OverdueAnalysis overdueAnalysis) {
        StringBuilder insights = new StringBuilder();
        try {
            LocalDate[] dateRange = getSelectedDateRange();
            DeadlinePrediction prediction = analyticsService.getDeadlinePrediction(dateRange[0], dateRange[1]);
            if (prediction != null && prediction.atRisk) {
                insights.append(" At risk of missing deadline(s)! Expected completion: ")
                       .append(prediction.expectedCompletion)
                       .append(". Latest due: ")
                       .append(prediction.latestDueDate)
                       .append(". Estimated days late: ")
                       .append(prediction.daysLate)
                       .append(".\n\n");
                // Show desktop notification for deadline risk
                NotificationUtil.showNotification(this, "Deadline Risk Alert", "At risk of missing deadline(s)!\nLatest due: " + prediction.latestDueDate + "\nExpected completion: " + prediction.expectedCompletion);
            }
        } catch (Exception e) {
            // Ignore prediction errors, don't block insights
        }

        // Completion rate insight
        if (metrics.completionRate >= 0.8) {
            insights.append("Excellent completion rate of ")
                   .append(String.format("%.0f%%", metrics.completionRate * 100))
                   .append("! Team is performing well.\n\n");
        } else if (metrics.completionRate >= 0.5) {
            insights.append("Moderate completion rate of ")
                   .append(String.format("%.0f%%", metrics.completionRate * 100))
                   .append(". Consider reviewing task assignments.\n\n");
        } else if (metrics.totalTasks > 0) {
            insights.append("Low completion rate of ")
                   .append(String.format("%.0f%%", metrics.completionRate * 100))
                   .append(". Immediate attention needed!\n\n");
        }

        // Overdue tasks insight
        if (overdueAnalysis.overdueTasks > 0) {
            insights.append(overdueAnalysis.overdueTasks)
                   .append(" overdue tasks need attention (avg ")
                   .append(String.format("%.1f", overdueAnalysis.averageOverdueDays))
                   .append(" days overdue).\n\n");
            // Show desktop notification for overdue tasks
            NotificationUtil.showNotification(this, "Overdue Tasks Alert", overdueAnalysis.overdueTasks + " tasks are overdue! Avg " + String.format("%.1f", overdueAnalysis.averageOverdueDays) + " days overdue.");
        } else {
            insights.append("No overdue tasks! Great time management.\n\n");
        }

        // Time efficiency insight
        if (metrics.timeEfficiency > 1.2) {
            insights.append("Tasks are being completed 20% faster than estimated! Consider adjusting estimates.\n\n");
        } else if (metrics.timeEfficiency < 0.8 && metrics.totalEstimatedTime > 0) {
            insights.append("Tasks taking 20% longer than estimated. Review planning process.\n\n");
        }

        // Productivity summary
        if (metrics.totalTasks > 0) {
            insights.append(metrics.completedTasks)
                   .append(" of ")
                   .append(metrics.totalTasks)
                   .append(" tasks completed, with ")
                   .append(String.format("%.0f hours", metrics.totalTimeSpent / 60.0))
                   .append(" invested.");
        }

        if (insights.length() == 0) {
            insights.append("No tasks in selected period. Try selecting a different time range.");
        }

        insightsArea.setText(insights.toString());
    }
    
    private void updateMetricsDisplay(ProductivityMetrics metrics) {
        totalTasksLabel.setText(String.valueOf(metrics.totalTasks));
        completedTasksLabel.setText(String.valueOf(metrics.completedTasks));
        completionRateLabel.setText(String.format("%.1f%%", metrics.completionRate * 100));
        avgCompletionLabel.setText(String.format("%.1f days", metrics.averageCompletionDays));
        
        String efficiencyText = metrics.timeEfficiency > 0 ? 
            String.format("%.0f%%", metrics.timeEfficiency * 100) : "N/A";
        timeEfficiencyLabel.setText(efficiencyText);
    }
    
    private void updateCompletionTrendChart(LocalDate startDate, LocalDate endDate) {
        try {
            System.out.println("  📈 Getting completion trend data...");
            List<CompletionTrendData> trendData = analyticsService.getCompletionTrend(startDate, endDate);
            System.out.println("  ✅ Got " + trendData.size() + " data points");
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
            
            for (CompletionTrendData data : trendData) {
                String dateStr = data.date.format(formatter);
                dataset.addValue(data.tasksCompleted, "Completed", dateStr);
            }
            
            System.out.println("  ✅ Dataset created with " + dataset.getColumnCount() + " columns");
            
            // Update chart
            JFreeChart chart = completionTrendChart.getChart();
            chart.getCategoryPlot().setDataset(dataset);
            System.out.println("  ✅ Completion trend chart updated");
            
        } catch (Exception e) {
            System.err.println("  ❌ Error updating completion trend chart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updatePriorityDistributionChart() {
        try {
            Map<com.worksphere.model.Priority, Integer> distribution = 
                analyticsService.getPriorityDistribution();
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            
            distribution.forEach((priority, count) -> {
                if (count > 0) {
                    dataset.setValue(priority.toString(), count);
                }
            });
            
            JFreeChart chart = priorityDistributionChart.getChart();
            @SuppressWarnings("unchecked")
            PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
            plot.setDataset(dataset);
        } catch (Exception e) {
            System.err.println("Error updating priority distribution chart: " + e.getMessage());
        }
    }
    
    private void updateTeamPerformanceChart() {
        try {
            List<UserPerformance> userPerformance = analyticsService.getUserPerformance();
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            for (UserPerformance perf : userPerformance) {
                String username = perf.username != null ? perf.username : "User " + perf.userId;
                dataset.addValue(perf.completedTasks, "Completed", username);
                dataset.addValue(perf.assignedTasks - perf.completedTasks, "In Progress", username);
            }
            
            JFreeChart chart = teamPerformanceChart.getChart();
            chart.getCategoryPlot().setDataset(dataset);
        } catch (Exception e) {
            System.err.println("Error updating team performance: " + e.getMessage());
        }
    }
    
    private void updateTimeAccuracyChart(ProductivityMetrics metrics) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            double estimatedHours = metrics.totalEstimatedTime / 60.0;
            double actualHours = metrics.totalTimeSpent / 60.0;
            
            if (estimatedHours > 0 || actualHours > 0) {
                dataset.addValue(estimatedHours, "Estimated", "Time");
                dataset.addValue(actualHours, "Actual", "Time");
            }
            
            JFreeChart chart = timeAccuracyChart.getChart();
            chart.getCategoryPlot().setDataset(dataset);
        } catch (Exception e) {
            System.err.println("Error updating time accuracy: " + e.getMessage());
        }
    }
    
    private LocalDate[] getSelectedDateRange() {
        String selected = (String) dateRangeCombo.getSelectedItem();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (selected) {
            case "Last 7 Days":
                startDate = endDate.minusDays(7);
                break;
            case "Last 30 Days":
                startDate = endDate.minusDays(30);
                break;
            case "Last 90 Days":
                startDate = endDate.minusDays(90);
                break;
            case "This Year":
                startDate = LocalDate.of(endDate.getYear(), 1, 1);
                break;
            case "All Time":
            default:
                startDate = LocalDate.of(2020, 1, 1); // Far enough back
                break;
        }
        
        return new LocalDate[]{startDate, endDate};
    }
    
    private String formatMinutes(int minutes) {
        if (minutes == 0) return "0h";
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return hours + "h " + (mins > 0 ? mins + "m" : "");
        } else {
            return mins + "m";
        }
    }
    
    /**
     * Refresh the analytics data
     */
    public void refreshData() {
        loadAnalytics();
    }
}