package com.worksphere.service;

import com.worksphere.model.Task;
import com.worksphere.model.Category;
import com.worksphere.service.AnalyticsService.ProductivityMetrics;
import com.worksphere.service.AnalyticsService.CategoryPerformance;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting task data to various formats
 */
public class ExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Export tasks to CSV format
     */
    public void exportTasksToCSV(List<Task> tasks, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write CSV header
            writer.append("ID,Title,Description,Priority,Status,Due Date,Assigned To,Created By,")
                  .append("Category,Tags,Estimated Time,Actual Time,Completion Rate,Created Date,")
                  .append("Completed Date,Is Overdue\n");
            
            // Write task data
            for (Task task : tasks) {
                writer.append(String.valueOf(task.getId())).append(",")
                      .append(escapeCsvValue(task.getTitle())).append(",")
                      .append(escapeCsvValue(task.getDescription())).append(",")
                      .append(task.getPriority().name()).append(",")
                      .append(task.getStatus().name()).append(",")
                      .append(task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "").append(",")
                      .append(escapeCsvValue(task.getAssignedToUsername())).append(",")
                      .append(escapeCsvValue(task.getCreatedByUsername())).append(",")
                      .append(task.getCategory() != null ? escapeCsvValue(task.getCategory().getName()) : "").append(",")
                      .append(escapeCsvValue(task.getTagsAsString())).append(",")
                      .append(task.getEstimatedTimeFormatted()).append(",")
                      .append(task.getActualTimeFormatted()).append(",")
                      .append(String.format("%.2f", task.getTimeEfficiency())).append(",")
                      .append(task.getCreatedAt() != null ? task.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "").append(",")
                      .append(task.getCompletedAt() != null ? task.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "").append(",")
                      .append(task.isOverdue() ? "Yes" : "No").append("\n");
            }
        }
    }
    
    /**
     * Export tasks to Excel format
     */
    public void exportTasksToExcel(List<Task> tasks, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create tasks sheet
            Sheet taskSheet = workbook.createSheet("Tasks");
            createTaskSheet(taskSheet, tasks);
            
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, tasks);
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Export analytics report to Excel
     */
    public void exportAnalyticsToExcel(ProductivityMetrics metrics, 
                                     List<CategoryPerformance> categoryPerformance, 
                                     String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create metrics sheet
            Sheet metricsSheet = workbook.createSheet("Productivity Metrics");
            createMetricsSheet(metricsSheet, metrics);
            
            // Create category performance sheet
            Sheet categorySheet = workbook.createSheet("Category Performance");
            createCategoryPerformanceSheet(categorySheet, categoryPerformance);
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
    
    private void createTaskSheet(Sheet sheet, List<Task> tasks) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "Title", "Description", "Priority", "Status", "Due Date",
            "Assigned To", "Created By", "Category", "Tags", "Estimated Time",
            "Actual Time", "Efficiency", "Created Date", "Completed Date", "Overdue"
        };
        
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        int rowNum = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(task.getId());
            row.createCell(1).setCellValue(task.getTitle());
            row.createCell(2).setCellValue(task.getDescription());
            row.createCell(3).setCellValue(task.getPriority().name());
            row.createCell(4).setCellValue(task.getStatus().name());
            row.createCell(5).setCellValue(task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "");
            row.createCell(6).setCellValue(task.getAssignedToUsername());
            row.createCell(7).setCellValue(task.getCreatedByUsername());
            row.createCell(8).setCellValue(task.getCategory() != null ? task.getCategory().getName() : "");
            row.createCell(9).setCellValue(task.getTagsAsString());
            row.createCell(10).setCellValue(task.getEstimatedTimeFormatted());
            row.createCell(11).setCellValue(task.getActualTimeFormatted());
            row.createCell(12).setCellValue(task.getTimeEfficiency());
            row.createCell(13).setCellValue(task.getCreatedAt() != null ? 
                task.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            row.createCell(14).setCellValue(task.getCompletedAt() != null ? 
                task.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            row.createCell(15).setCellValue(task.isOverdue() ? "Yes" : "No");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createSummarySheet(Sheet sheet, List<Task> tasks) {
        // Calculate summary statistics
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus().name().equals("COMPLETED")).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus().name().equals("IN_PROGRESS")).count();
        long todoTasks = tasks.stream().filter(t -> t.getStatus().name().equals("TODO")).count();
        long overdueTasks = tasks.stream().filter(Task::isOverdue).count();
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        // Create summary data
        String[][] summaryData = {
            {"Total Tasks", String.valueOf(totalTasks)},
            {"Completed Tasks", String.valueOf(completedTasks)},
            {"In Progress Tasks", String.valueOf(inProgressTasks)},
            {"Todo Tasks", String.valueOf(todoTasks)},
            {"Overdue Tasks", String.valueOf(overdueTasks)},
            {"Completion Rate", String.format("%.1f%%", completionRate)},
            {"Report Generated", LocalDate.now().format(DATE_FORMATTER)}
        };
        
        // Create header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Task Summary Report");
        
        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        headerRow.getCell(0).setCellStyle(titleStyle);
        
        // Create summary rows
        for (int i = 0; i < summaryData.length; i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(summaryData[i][0]);
            row.createCell(1).setCellValue(summaryData[i][1]);
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createMetricsSheet(Sheet sheet, ProductivityMetrics metrics) {
        // Create title
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Productivity Metrics Report");
        
        // Create metrics data
        String[][] metricsData = {
            {"Total Tasks", String.valueOf(metrics.totalTasks)},
            {"Completed Tasks", String.valueOf(metrics.completedTasks)},
            {"In Progress Tasks", String.valueOf(metrics.inProgressTasks)},
            {"Todo Tasks", String.valueOf(metrics.todoTasks)},
            {"Completion Rate", String.format("%.1f%%", metrics.completionRate * 100)},
            {"Average Completion Days", String.format("%.1f", metrics.averageCompletionDays)},
            {"Total Time Spent", formatMinutes(metrics.totalTimeSpent)},
            {"Total Estimated Time", formatMinutes(metrics.totalEstimatedTime)},
            {"Time Efficiency", String.format("%.1f%%", metrics.timeEfficiency * 100)}
        };
        
        for (int i = 0; i < metricsData.length; i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(metricsData[i][0]);
            row.createCell(1).setCellValue(metricsData[i][1]);
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createCategoryPerformanceSheet(Sheet sheet, List<CategoryPerformance> categoryPerformance) {
        // Create header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Category", "Total Tasks", "Completed Tasks", "Completion Rate", "Time Spent", "Avg Time/Task"};
        
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        // Create data rows
        int rowNum = 1;
        for (CategoryPerformance perf : categoryPerformance) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(perf.category.getName());
            row.createCell(1).setCellValue(perf.totalTasks);
            row.createCell(2).setCellValue(perf.completedTasks);
            row.createCell(3).setCellValue(String.format("%.1f%%", perf.completionRate * 100));
            row.createCell(4).setCellValue(formatMinutes(perf.totalTimeSpent));
            row.createCell(5).setCellValue(formatMinutes((int) perf.averageTimePerTask));
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private String escapeCsvValue(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String formatMinutes(int minutes) {
        if (minutes <= 0) return "0m";
        
        int hours = minutes / 60;
        int mins = minutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, mins);
        } else {
            return String.format("%dm", mins);
        }
    }
}