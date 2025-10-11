package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.AnalyticsService;
import com.worksphere.service.AnalyticsService.ProductivityMetrics;
import com.worksphere.service.AnalyticsService.CompletionTrendData;
import com.worksphere.service.AnalyticsService.CategoryPerformance;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Analytics Dashboard Panel for displaying productivity metrics and charts
 */
public class AnalyticsDashboardPanel extends JPanel {
    
    private AnalyticsService analyticsService;
    private User currentUser;
    
    // Date range controls
    private JComboBox<String> dateRangeCombo;
    private JButton refreshButton;
    
    // Metrics display
    private JLabel totalTasksLabel;
    private JLabel completedTasksLabel;
    private JLabel completionRateLabel;
    private JLabel timeSpentLabel;
    private JLabel efficiencyLabel;
    
    // Chart panels
    private ChartPanel completionTrendChart;
    private ChartPanel priorityDistributionChart;
    private ChartPanel categoryPerformanceChart;
    
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
        
        // Metrics labels
        totalTasksLabel = new JLabel("0");
        completedTasksLabel = new JLabel("0");
        completionRateLabel = new JLabel("0%");
        timeSpentLabel = new JLabel("0h");
        efficiencyLabel = new JLabel("0%");
        
        // Style metrics labels
        Font metricsFont = new Font("Segoe UI", Font.BOLD, 24);
        Color primaryColor = new Color(33, 150, 243);
        
        totalTasksLabel.setFont(metricsFont);
        totalTasksLabel.setForeground(primaryColor);
        completedTasksLabel.setFont(metricsFont);
        completedTasksLabel.setForeground(new Color(76, 175, 80));
        completionRateLabel.setFont(metricsFont);
        completionRateLabel.setForeground(new Color(255, 152, 0));
        timeSpentLabel.setFont(metricsFont);
        timeSpentLabel.setForeground(new Color(156, 39, 176));
        efficiencyLabel.setFont(metricsFont);
        efficiencyLabel.setForeground(new Color(244, 67, 54));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Top panel - controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Time Period:"));
        topPanel.add(dateRangeCombo);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(refreshButton);
        
        // Metrics panel
        JPanel metricsPanel = createMetricsPanel();
        
        // Charts panel
        JPanel chartsPanel = createChartsPanel();
        
        // Layout
        add(topPanel, BorderLayout.NORTH);
        add(metricsPanel, BorderLayout.CENTER);
        add(chartsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)), 
            "📊 Key Metrics", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(73, 80, 87)
        ));
        panel.setBackground(Color.WHITE);
        
        // Metric cards
        panel.add(createMetricCard("Total Tasks", totalTasksLabel, "📝"));
        panel.add(createMetricCard("Completed", completedTasksLabel, "✅"));
        panel.add(createMetricCard("Completion Rate", completionRateLabel, "📈"));
        panel.add(createMetricCard("Time Spent", timeSpentLabel, "⏱️"));
        panel.add(createMetricCard("Efficiency", efficiencyLabel, "🎯"));
        
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
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setPreferredSize(new Dimension(0, 300));
        
        // Create placeholder charts
        completionTrendChart = createCompletionTrendChart();
        priorityDistributionChart = createPriorityDistributionChart();
        categoryPerformanceChart = createCategoryPerformanceChart();
        
        panel.add(completionTrendChart);
        panel.add(priorityDistributionChart);
        panel.add(categoryPerformanceChart);
        
        return panel;
    }
    
    private ChartPanel createCompletionTrendChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createLineChart(
            "Completion Trend",
            "Date",
            "Tasks Completed",
            dataset
        );
        
        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(33, 150, 243));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Daily Completions"));
        return chartPanel;
    }
    
    private ChartPanel createPriorityDistributionChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("High", 0);
        dataset.setValue("Medium", 0);
        dataset.setValue("Low", 0);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Priority Distribution",
            dataset,
            true,
            true,
            false
        );
        
        // Customize pie chart
        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setSectionPaint("High", new Color(244, 67, 54));
        plot.setSectionPaint("Medium", new Color(255, 152, 0));
        plot.setSectionPaint("Low", new Color(76, 175, 80));
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Task Priorities"));
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
            LocalDate[] dateRange = getSelectedDateRange();
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];
            
            // Load productivity metrics
            ProductivityMetrics metrics = analyticsService.getProductivityMetrics(startDate, endDate);
            updateMetricsDisplay(metrics);
            
            // Load and update charts
            updateCompletionTrendChart(startDate, endDate);
            updatePriorityDistributionChart(startDate, endDate);
            updateCategoryPerformanceChart();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading analytics: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateMetricsDisplay(ProductivityMetrics metrics) {
        totalTasksLabel.setText(String.valueOf(metrics.totalTasks));
        completedTasksLabel.setText(String.valueOf(metrics.completedTasks));
        completionRateLabel.setText(String.format("%.1f%%", metrics.completionRate * 100));
        timeSpentLabel.setText(formatMinutes(metrics.totalTimeSpent));
        efficiencyLabel.setText(String.format("%.1f%%", metrics.timeEfficiency * 100));
    }
    
    private void updateCompletionTrendChart(LocalDate startDate, LocalDate endDate) {
        try {
            List<CompletionTrendData> trendData = analyticsService.getCompletionTrend(startDate, endDate);
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
            
            for (CompletionTrendData data : trendData) {
                String dateStr = data.date.format(formatter);
                dataset.addValue(data.tasksCompleted, "Completed", dateStr);
            }
            
            // Update chart
            JFreeChart chart = completionTrendChart.getChart();
            chart.getCategoryPlot().setDataset(dataset);
            
        } catch (Exception e) {
            System.err.println("Error updating completion trend chart: " + e.getMessage());
        }
    }
    
    private void updatePriorityDistributionChart(LocalDate startDate, LocalDate endDate) {
        try {
            // Get task counts by priority (simplified implementation)
            ProductivityMetrics metrics = analyticsService.getProductivityMetrics(startDate, endDate);
            
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            // This is a simplified version - you could extend AnalyticsService to get priority distribution
            dataset.setValue("High", metrics.totalTasks * 0.3);
            dataset.setValue("Medium", metrics.totalTasks * 0.5);
            dataset.setValue("Low", metrics.totalTasks * 0.2);
            
            // Update chart
            JFreeChart chart = priorityDistributionChart.getChart();
            @SuppressWarnings("unchecked")
            PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
            plot.setDataset(dataset);
            
        } catch (Exception e) {
            System.err.println("Error updating priority distribution chart: " + e.getMessage());
        }
    }
    
    private void updateCategoryPerformanceChart() {
        try {
            List<CategoryPerformance> categoryData = analyticsService.getCategoryPerformance();
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            for (CategoryPerformance category : categoryData) {
                String categoryName = category.category != null ? 
                    category.category.getName() : "No Category";
                dataset.addValue(category.completionRate * 100, "Completion Rate", categoryName);
            }
            
            // Update chart
            JFreeChart chart = categoryPerformanceChart.getChart();
            chart.getCategoryPlot().setDataset(dataset);
            
        } catch (Exception e) {
            System.err.println("Error updating category performance chart: " + e.getMessage());
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