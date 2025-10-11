package com.worksphere;

import com.formdev.flatlaf.FlatLightLaf;
import com.worksphere.cli.WorkSphereCLI;
import com.worksphere.gui.WorkSphereGUI;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for WorkSphere
 * Supports both GUI and CLI modes
 */
public class WorkSphereApp {
    
    public static void main(String[] args) {
        // Check if CLI mode is requested
        boolean useCliMode = args.length > 0 && (args[0].equals("--cli") || args[0].equals("-c"));
        
        if (useCliMode) {
            // Launch CLI version
            WorkSphereCLI cli = new WorkSphereCLI();
            cli.start();
        } else {
            // Launch GUI version (default)
            launchGUI();
        }
    }
    
    private static void launchGUI() {
        // Set system properties for better GUI appearance
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Enable proper high DPI support - let Windows handle the scaling
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.win.uiScaleX", "auto");
        System.setProperty("sun.java2d.win.uiScaleY", "auto");
        // Remove the line that forces 1.0 scaling - this was causing the issue!
        
        // Set look and feel before creating any GUI components
        SwingUtilities.invokeLater(() -> {
            try {
                // Use FlatLaf for modern appearance
                UIManager.setLookAndFeel(new FlatLightLaf());
                
                // Set modern white theme properties
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("Button.background", new Color(248, 249, 250));
                UIManager.put("Button.hoverBackground", new Color(232, 234, 237));
                UIManager.put("Button.pressedBackground", new Color(208, 215, 222));
                UIManager.put("Button.arc", 6);
                UIManager.put("Component.arc", 6);
                UIManager.put("TextField.arc", 6);
                UIManager.put("ComboBox.arc", 6);
                UIManager.put("Table.background", Color.WHITE);
                UIManager.put("Table.alternateRowColor", new Color(250, 251, 252));
                UIManager.put("Table.selectionBackground", new Color(0, 102, 204, 40));
                UIManager.put("Table.selectionForeground", Color.BLACK);
                UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
                UIManager.put("TabbedPane.hoverColor", new Color(240, 241, 242));
                UIManager.put("TitlePane.background", Color.WHITE);
                
                // Alternative: Use system look and feel
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
                
            } catch (Exception e) {
                System.err.println("Failed to set look and feel: " + e.getMessage());
                // Fall back to default look and feel
            }
            
            // Show splash screen (optional)
            showSplashScreen();
            
            // Launch the main GUI application
            try {
                new WorkSphereGUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(),
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        splash.setSize(400, 200);
        splash.setLocationRelativeTo(null);
        
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new java.awt.Color(70, 130, 180));
        content.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Title
        JLabel titleLabel = new JLabel("WorkSphere", SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(java.awt.Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Loading GUI...", SwingConstants.CENTER);
        subtitleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        subtitleLabel.setForeground(java.awt.Color.WHITE);
        
        // Version
        JLabel versionLabel = new JLabel("Version 1.0.0 - Task Management System", SwingConstants.CENTER);
        versionLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        versionLabel.setForeground(java.awt.Color.LIGHT_GRAY);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(subtitleLabel, BorderLayout.CENTER);
        content.add(versionLabel, BorderLayout.SOUTH);
        
        splash.setContentPane(content);
        splash.setVisible(true);
        
        // Show splash for 2 seconds
        Timer timer = new Timer(2000, e -> splash.dispose());
        timer.setRepeats(false);
        timer.start();
        
        // Wait for splash to finish
        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
