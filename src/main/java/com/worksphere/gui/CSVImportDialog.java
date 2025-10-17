package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.CSVImportExportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Dialog for importing tasks from CSV files
 */
public class CSVImportDialog extends JDialog {
    private final User currentUser;
    private final CSVImportExportService csvService;
    private final Runnable onImportComplete;
    
    private JTextField filePathField;
    private JButton browseButton;
    private JButton generateTemplateButton;
    private JButton importButton;
    private JProgressBar progressBar;
    private JTextArea resultsArea;
    private JButton closeButton;
    
    public CSVImportDialog(Frame parent, User currentUser, Runnable onImportComplete) {
        super(parent, "Import Tasks from CSV", true);
        this.currentUser = currentUser;
        this.csvService = new CSVImportExportService();
        this.onImportComplete = onImportComplete;
        
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 500);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Import Tasks from CSV");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Instructions
        JTextArea instructionsArea = new JTextArea(
            "1. Generate a sample CSV template to see the required format\n" +
            "2. Fill in your tasks using Excel, Google Sheets, or any text editor\n" +
            "3. Save as CSV and select the file below\n" +
            "4. Click 'Import Tasks' to bulk import your tasks"
        );
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(new Color(240, 248, 255));
        instructionsArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(173, 216, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        instructionsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(instructionsArea);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Generate template section
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        templatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        templatePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        generateTemplateButton = new JButton("📄 Generate Sample Template");
        generateTemplateButton.setToolTipText("Create a sample CSV file to use as a template");
        generateTemplateButton.addActionListener(e -> generateTemplate());
        templatePanel.add(generateTemplateButton);
        
        mainPanel.add(templatePanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // File selection section
        JLabel fileLabel = new JLabel("Select CSV File to Import:");
        fileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(fileLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePanel.add(filePathField, BorderLayout.CENTER);
        
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseFile());
        filePanel.add(browseButton, BorderLayout.EAST);
        
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Import button
        JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        importPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        importButton = new JButton("⬆ Import Tasks");
        importButton.setEnabled(false);
        importButton.setToolTipText("Import tasks from the selected CSV file");
        importButton.addActionListener(e -> importTasks());
        importPanel.add(importButton);
        
        mainPanel.add(importPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        progressBar.setVisible(false);
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Results area
        JLabel resultsLabel = new JLabel("Results:");
        resultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(resultsLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        resultsArea = new JTextArea(8, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(resultsScroll);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void generateTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Sample Template");
        fileChooser.setSelectedFile(new File("sample_tasks_template.csv"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            // Ensure .csv extension
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            boolean success = csvService.generateSampleTemplate(filePath);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Sample template generated successfully!\n\n" +
                    "File: " + filePath + "\n\n" +
                    "You can now:\n" +
                    "1. Open this file in Excel or Google Sheets\n" +
                    "2. Modify the sample tasks or add your own\n" +
                    "3. Save and import using the 'Import Tasks' button",
                    "Template Generated",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to generate template. Please check the file path and try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File to Import");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
            importButton.setEnabled(true);
            resultsArea.setText("");
        }
    }
    
    private void importTasks() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select a CSV file first.",
                "No File Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable buttons during import
        importButton.setEnabled(false);
        browseButton.setEnabled(false);
        generateTemplateButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Importing tasks...");
        resultsArea.setText("Processing CSV file...\n");
        
        // Perform import in background thread
        SwingWorker<CSVImportExportService.ImportResult, Void> worker = 
            new SwingWorker<CSVImportExportService.ImportResult, Void>() {
            
            @Override
            protected CSVImportExportService.ImportResult doInBackground() throws Exception {
                return csvService.importTasksFromCSV(filePath, currentUser.getId());
            }
            
            @Override
            protected void done() {
                try {
                    CSVImportExportService.ImportResult result = get();
                    
                    // Update progress bar
                    progressBar.setIndeterminate(false);
                    if (result.hasErrors()) {
                        int percentage = (int) ((double) result.successCount / result.totalLines * 100);
                        progressBar.setValue(percentage);
                        progressBar.setString("Completed with errors");
                    } else {
                        progressBar.setValue(100);
                        progressBar.setString("Import successful!");
                    }
                    
                    // Display results
                    resultsArea.setText(result.getSummary());
                    
                    // Show summary dialog
                    if (result.hasErrors()) {
                        JOptionPane.showMessageDialog(CSVImportDialog.this,
                            String.format("Import completed with some errors.\n\n" +
                                "Successfully imported: %d tasks\n" +
                                "Errors: %d\n\n" +
                                "Check the results area for details.",
                                result.successCount, result.errorCount),
                            "Import Completed with Errors",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(CSVImportDialog.this,
                            String.format("✓ Import successful!\n\n" +
                                "Successfully imported %d tasks.",
                                result.successCount),
                            "Import Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    // Notify parent to refresh
                    if (result.successCount > 0 && onImportComplete != null) {
                        onImportComplete.run();
                    }
                    
                } catch (Exception e) {
                    progressBar.setString("Import failed");
                    resultsArea.setText("Error during import:\n" + e.getMessage());
                    JOptionPane.showMessageDialog(CSVImportDialog.this,
                        "Import failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable buttons
                    importButton.setEnabled(true);
                    browseButton.setEnabled(true);
                    generateTemplateButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
}
