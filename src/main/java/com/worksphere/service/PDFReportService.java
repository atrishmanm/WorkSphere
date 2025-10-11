package com.worksphere.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import com.worksphere.model.Task;
import com.worksphere.model.User;
import com.worksphere.service.AnalyticsService.ProductivityMetrics;
import com.worksphere.service.AnalyticsService.CompletionTrendData;
import com.worksphere.service.AnalyticsService.CategoryPerformance;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating PDF reports with task analytics and summaries
 */
public class PDFReportService {
    
    private AnalyticsService analyticsService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public PDFReportService() {
        this.analyticsService = new AnalyticsService();
    }
    
    /**
     * Generate a weekly productivity report
     */
    public File generateWeeklyReport(User user, LocalDate weekStart, String outputPath) throws Exception {
        LocalDate weekEnd = weekStart.plusDays(6);
        String fileName = outputPath + File.separator + 
            "weekly_report_" + user.getUsername() + "_" + weekStart.format(dateFormatter) + ".pdf";
        
        File reportFile = new File(fileName);
        PdfWriter writer = new PdfWriter(new FileOutputStream(reportFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            // Add title
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            document.add(new Paragraph("Weekly Productivity Report")
                .setFont(titleFont)
                .setFontSize(20)
                .setMarginBottom(10));
            
            document.add(new Paragraph("User: " + user.getUsername())
                .setFont(normalFont)
                .setFontSize(12));
            
            document.add(new Paragraph("Period: " + weekStart.format(dateFormatter) + " to " + weekEnd.format(dateFormatter))
                .setFont(normalFont)
                .setFontSize(12)
                .setMarginBottom(20));
            
            // Get analytics data
            ProductivityMetrics metrics = analyticsService.getProductivityMetrics(weekStart, weekEnd);
            List<CompletionTrendData> trends = analyticsService.getCompletionTrend(weekStart, weekEnd);
            List<CategoryPerformance> categoryStats = analyticsService.getCategoryPerformance();
            
            // Productivity Overview Section
            document.add(new Paragraph("Productivity Overview")
                .setFont(headerFont)
                .setFontSize(16)
                .setMarginBottom(10));
            
            Table overviewTable = new Table(2).setWidth(500);
            
            addTableRow(overviewTable, "Total Tasks", String.valueOf(metrics.totalTasks), headerFont, normalFont);
            addTableRow(overviewTable, "Completed Tasks", String.valueOf(metrics.completedTasks), headerFont, normalFont);
            addTableRow(overviewTable, "Completion Rate", String.format("%.1f%%", metrics.completionRate * 100), headerFont, normalFont);
            addTableRow(overviewTable, "Total Time Worked", formatMinutes(metrics.totalTimeSpent), headerFont, normalFont);
            addTableRow(overviewTable, "Time Efficiency", String.format("%.1f%%", metrics.timeEfficiency * 100), headerFont, normalFont);
            
            document.add(overviewTable);
            document.add(new Paragraph(" "));
            
            // Daily Breakdown Section
            document.add(new Paragraph("Daily Breakdown")
                .setFont(headerFont)
                .setFontSize(16)
                .setMarginBottom(10));
            
            Table dailyTable = new Table(4).setWidth(500);
            
            // Header row
            dailyTable.addHeaderCell(new Cell().add(new Paragraph("Date").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            dailyTable.addHeaderCell(new Cell().add(new Paragraph("Created").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            dailyTable.addHeaderCell(new Cell().add(new Paragraph("Completed").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            dailyTable.addHeaderCell(new Cell().add(new Paragraph("Rate").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            for (CompletionTrendData trend : trends) {
                dailyTable.addCell(new Cell().add(new Paragraph(trend.date.format(dateFormatter)).setFont(normalFont)));
                dailyTable.addCell(new Cell().add(new Paragraph(String.valueOf(trend.tasksCreated)).setFont(normalFont)));
                dailyTable.addCell(new Cell().add(new Paragraph(String.valueOf(trend.tasksCompleted)).setFont(normalFont)));
                double rate = trend.tasksCreated > 0 ? (double) trend.tasksCompleted / trend.tasksCreated * 100 : 0;
                dailyTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", rate)).setFont(normalFont)));
            }
            
            document.add(dailyTable);
            document.add(new Paragraph(" "));
            
            // Category Performance Section
            if (!categoryStats.isEmpty()) {
                document.add(new Paragraph("Category Performance")
                    .setFont(headerFont)
                    .setFontSize(16)
                    .setMarginBottom(10));
                
                Table categoryTable = new Table(5).setWidth(500);
                
                // Header row
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Category").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Tasks").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Completed").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Rate").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Avg Time").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                
                for (CategoryPerformance category : categoryStats) {
                    String categoryName = category.category != null ? category.category.getName() : "No Category";
                    categoryTable.addCell(new Cell().add(new Paragraph(categoryName).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(String.valueOf(category.totalTasks)).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(String.valueOf(category.completedTasks)).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", category.completionRate * 100)).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(formatMinutes((int) category.averageTimePerTask)).setFont(normalFont)));
                }
                
                document.add(categoryTable);
            }
            
        } finally {
            document.close();
        }
        
        return reportFile;
    }
    
    /**
     * Generate a monthly summary report
     */
    public File generateMonthlyReport(User user, LocalDate monthStart, String outputPath) throws Exception {
        LocalDate monthEnd = monthStart.plusDays(monthStart.lengthOfMonth() - 1);
        String fileName = outputPath + File.separator + 
            "monthly_report_" + user.getUsername() + "_" + monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")) + ".pdf";
        
        File reportFile = new File(fileName);
        PdfWriter writer = new PdfWriter(new FileOutputStream(reportFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Title
            document.add(new Paragraph("Monthly Productivity Report")
                .setFont(titleFont)
                .setFontSize(20)
                .setMarginBottom(10));
            
            document.add(new Paragraph("User: " + user.getUsername())
                .setFont(normalFont)
                .setFontSize(12));
            
            document.add(new Paragraph("Month: " + monthStart.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                .setFont(normalFont)
                .setFontSize(12)
                .setMarginBottom(20));
            
            // Get monthly analytics
            ProductivityMetrics metrics = analyticsService.getProductivityMetrics(monthStart, monthEnd);
            List<CategoryPerformance> categoryStats = analyticsService.getCategoryPerformance();
            
            // Monthly Summary
            document.add(new Paragraph("Monthly Summary")
                .setFont(headerFont)
                .setFontSize(16)
                .setMarginBottom(10));
            
            Table summaryTable = new Table(2).setWidth(500);
            
            addTableRow(summaryTable, "Total Tasks Created", String.valueOf(metrics.totalTasks), headerFont, normalFont);
            addTableRow(summaryTable, "Tasks Completed", String.valueOf(metrics.completedTasks), headerFont, normalFont);
            addTableRow(summaryTable, "Overall Completion Rate", String.format("%.1f%%", metrics.completionRate * 100), headerFont, normalFont);
            addTableRow(summaryTable, "Total Time Invested", formatMinutes(metrics.totalTimeSpent), headerFont, normalFont);
            addTableRow(summaryTable, "Time Efficiency", String.format("%.1f%%", metrics.timeEfficiency * 100), headerFont, normalFont);
            addTableRow(summaryTable, "Average Completion Time", String.format("%.1f days", metrics.averageCompletionDays), headerFont, normalFont);
            
            document.add(summaryTable);
            document.add(new Paragraph(" "));
            
            // Weekly trends (4 weeks in month)
            document.add(new Paragraph("Weekly Trends")
                .setFont(headerFont)
                .setFontSize(16)
                .setMarginBottom(10));
            
            Table weeklyTable = new Table(4).setWidth(500);
            
            weeklyTable.addHeaderCell(new Cell().add(new Paragraph("Week").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            weeklyTable.addHeaderCell(new Cell().add(new Paragraph("Tasks").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            weeklyTable.addHeaderCell(new Cell().add(new Paragraph("Completed").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            weeklyTable.addHeaderCell(new Cell().add(new Paragraph("Rate").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            // Calculate weekly stats
            for (int week = 1; week <= 4; week++) {
                LocalDate weekStart = monthStart.plusWeeks(week - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                if (weekEnd.isAfter(monthEnd)) weekEnd = monthEnd;
                
                ProductivityMetrics weekMetrics = analyticsService.getProductivityMetrics(weekStart, weekEnd);
                
                weeklyTable.addCell(new Cell().add(new Paragraph("Week " + week).setFont(normalFont)));
                weeklyTable.addCell(new Cell().add(new Paragraph(String.valueOf(weekMetrics.totalTasks)).setFont(normalFont)));
                weeklyTable.addCell(new Cell().add(new Paragraph(String.valueOf(weekMetrics.completedTasks)).setFont(normalFont)));
                weeklyTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", weekMetrics.completionRate * 100)).setFont(normalFont)));
            }
            
            document.add(weeklyTable);
            document.add(new Paragraph(" "));
            
            // Category performance
            if (!categoryStats.isEmpty()) {
                document.add(new Paragraph("Category Performance")
                    .setFont(headerFont)
                    .setFontSize(16)
                    .setMarginBottom(10));
                
                Table categoryTable = new Table(5).setWidth(500);
                
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Category").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Tasks").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Completed").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Rate").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                categoryTable.addHeaderCell(new Cell().add(new Paragraph("Total Time").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                
                for (CategoryPerformance category : categoryStats) {
                    String categoryName = category.category != null ? category.category.getName() : "No Category";
                    categoryTable.addCell(new Cell().add(new Paragraph(categoryName).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(String.valueOf(category.totalTasks)).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(String.valueOf(category.completedTasks)).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", category.completionRate * 100)).setFont(normalFont)));
                    categoryTable.addCell(new Cell().add(new Paragraph(formatMinutes(category.totalTimeSpent)).setFont(normalFont)));
                }
                
                document.add(categoryTable);
            }
            
        } finally {
            document.close();
        }
        
        return reportFile;
    }
    
    /**
     * Generate a custom task report for specific tasks
     */
    public File generateTaskReport(List<Task> tasks, String title, String outputPath) throws Exception {
        String fileName = outputPath + File.separator + 
            "task_report_" + System.currentTimeMillis() + ".pdf";
        
        File reportFile = new File(fileName);
        PdfWriter writer = new PdfWriter(new FileOutputStream(reportFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Title
            document.add(new Paragraph(title)
                .setFont(titleFont)
                .setFontSize(18)
                .setMarginBottom(10));
            
            document.add(new Paragraph("Generated on: " + LocalDate.now().format(dateFormatter))
                .setFont(normalFont)
                .setFontSize(10)
                .setMarginBottom(20));
            
            // Task summary
            int completed = (int) tasks.stream()
                .filter(task -> task.getStatus() == com.worksphere.model.TaskStatus.COMPLETED)
                .count();
            double completionRate = tasks.isEmpty() ? 0 : (completed * 100.0 / tasks.size());
            
            document.add(new Paragraph("Summary")
                .setFont(headerFont)
                .setFontSize(14)
                .setMarginBottom(5));
            
            document.add(new Paragraph("Total Tasks: " + tasks.size())
                .setFont(normalFont)
                .setFontSize(12));
            document.add(new Paragraph("Completed: " + completed)
                .setFont(normalFont)
                .setFontSize(12));
            document.add(new Paragraph("Completion Rate: " + String.format("%.1f%%", completionRate))
                .setFont(normalFont)
                .setFontSize(12)
                .setMarginBottom(15));
            
            // Detailed task list
            document.add(new Paragraph("Task Details")
                .setFont(headerFont)
                .setFontSize(14)
                .setMarginBottom(10));
            
            Table taskTable = new Table(5).setWidth(500);
            
            taskTable.addHeaderCell(new Cell().add(new Paragraph("Title").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            taskTable.addHeaderCell(new Cell().add(new Paragraph("Status").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            taskTable.addHeaderCell(new Cell().add(new Paragraph("Priority").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            taskTable.addHeaderCell(new Cell().add(new Paragraph("Due Date").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            taskTable.addHeaderCell(new Cell().add(new Paragraph("Time").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            for (Task task : tasks) {
                taskTable.addCell(new Cell().add(new Paragraph(task.getTitle()).setFont(normalFont)));
                taskTable.addCell(new Cell().add(new Paragraph(task.getStatus().toString()).setFont(normalFont)));
                taskTable.addCell(new Cell().add(new Paragraph(task.getPriority().toString()).setFont(normalFont)));
                
                String dueDateStr = task.getDueDate() != null ? task.getDueDate().format(dateFormatter) : "N/A";
                taskTable.addCell(new Cell().add(new Paragraph(dueDateStr).setFont(normalFont)));
                
                taskTable.addCell(new Cell().add(new Paragraph(formatMinutes(task.getActualMinutes())).setFont(normalFont)));
            }
            
            document.add(taskTable);
            
        } finally {
            document.close();
        }
        
        return reportFile;
    }
    
    private void addTableRow(Table table, String label, String value, PdfFont labelFont, PdfFont valueFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(labelFont)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(valueFont)));
    }
    
    private String formatMinutes(int minutes) {
        if (minutes == 0) return "0m";
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return hours + "h " + mins + "m";
        } else {
            return mins + "m";
        }
    }
}