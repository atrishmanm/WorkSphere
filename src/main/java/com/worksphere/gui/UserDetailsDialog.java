package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for viewing and editing user details
 */
public class UserDetailsDialog extends JDialog {
    
    private UserService userService;
    private User user;
    private boolean userUpdated = false;
    
    private JTextField usernameField;
    private JTextField nameField;
    private JTextField emailField;
    private JLabel createdAtLabel;
    private JLabel idLabel;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    public UserDetailsDialog(Window parent, User user, UserService userService) {
        super(parent, "User Details - " + user.getUsername(), ModalityType.APPLICATION_MODAL);
        
        this.userService = userService;
        this.user = user;
        
        initializeDialog();
        setupLayout();
        setupEventHandlers();
        populateFields();
    }
    
    private void initializeDialog() {
        setSize(600, 550);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("User Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        // User ID (read-only)
        formPanel.add(createReadOnlyField("User ID:", createIdLabel()));
        formPanel.add(Box.createVerticalStrut(20));
        
        // Username field
        formPanel.add(createFieldSection("Username:", createUsernameField()));
        formPanel.add(Box.createVerticalStrut(20));
        
        // Name field
        formPanel.add(createFieldSection("Full Name:", createNameField()));
        formPanel.add(Box.createVerticalStrut(20));
        
        // Email field
        formPanel.add(createFieldSection("Email:", createEmailField()));
        formPanel.add(Box.createVerticalStrut(20));
        
        // Change Password button
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.setPreferredSize(new Dimension(160, 35));
        changePasswordBtn.setBackground(new Color(70, 130, 180));
        changePasswordBtn.setForeground(Color.WHITE);
        changePasswordBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        changePasswordBtn.addActionListener(e -> showChangePasswordDialog());
        passwordPanel.add(changePasswordBtn);
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Created date (read-only)
        formPanel.add(createReadOnlyField("Created:", createCreatedAtLabel()));
        formPanel.add(Box.createVerticalStrut(10));
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        saveButton = new JButton("Save Changes");
        saveButton.setPreferredSize(new Dimension(140, 40));
        saveButton.setBackground(new Color(60, 160, 60));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFieldSection(String labelText, JComponent field) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        section.add(label);
        section.add(Box.createVerticalStrut(5));
        section.add(field);
        
        return section;
    }
    
    private JPanel createReadOnlyField(String labelText, JLabel valueLabel) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        valueLabel.setOpaque(true);
        valueLabel.setBackground(Color.LIGHT_GRAY);
        valueLabel.setPreferredSize(new Dimension(0, 30));
        valueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        section.add(label);
        section.add(Box.createVerticalStrut(5));
        section.add(valueLabel);
        
        return section;
    }
    
    private JLabel createIdLabel() {
        idLabel = new JLabel();
        return idLabel;
    }
    
    private JTextField createUsernameField() {
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return usernameField;
    }
    
    private JTextField createNameField() {
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return nameField;
    }
    
    private JTextField createEmailField() {
        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        emailField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return emailField;
    }
    
    private JLabel createCreatedAtLabel() {
        createdAtLabel = new JLabel();
        return createdAtLabel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveUser();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Set focus to username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }
    
    private void populateFields() {
        idLabel.setText("  " + user.getId());
        usernameField.setText(user.getUsername());
        nameField.setText(user.getName());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        createdAtLabel.setText("  " + user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
    }
    
    private void saveUser() {
        // Validation
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username is required",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Full name is required",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            email = null;
        }
        
        // Check if username changed and if new username already exists
        if (!username.equals(user.getUsername())) {
            try {
                User existingUser = userService.getUserByUsername(username);
                if (existingUser != null && existingUser.getId() != user.getId()) {
                    JOptionPane.showMessageDialog(this,
                        "Username '" + username + "' already exists.\nPlease choose a different username.",
                        "Username Taken",
                        JOptionPane.WARNING_MESSAGE);
                    usernameField.selectAll();
                    usernameField.requestFocus();
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error checking username: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Update user
        try {
            user.setUsername(username);
            user.setName(name);
            user.setEmail(email);
            
            userService.updateUser(user);
            userUpdated = true;
            
            JOptionPane.showMessageDialog(this,
                "User updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error updating user: " + e.getMessage(),
                "Update Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showChangePasswordDialog() {
        JDialog passwordDialog = new JDialog(this, "Change Password", true);
        passwordDialog.setSize(400, 280);
        passwordDialog.setLocationRelativeTo(this);
        passwordDialog.setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Current password
        JLabel currentPwdLabel = new JLabel("Current Password:");
        currentPwdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField currentPwdField = new JPasswordField();
        currentPwdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        currentPwdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(currentPwdLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(currentPwdField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // New password
        JLabel newPwdLabel = new JLabel("New Password:");
        newPwdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField newPwdField = new JPasswordField();
        newPwdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        newPwdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(newPwdLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(newPwdField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Confirm password
        JLabel confirmPwdLabel = new JLabel("Confirm New Password:");
        confirmPwdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField confirmPwdField = new JPasswordField();
        confirmPwdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        confirmPwdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(confirmPwdLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(confirmPwdField);
        
        passwordDialog.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton changeBtn = new JButton("Change");
        changeBtn.setPreferredSize(new Dimension(100, 35));
        changeBtn.setBackground(new Color(60, 160, 60));
        changeBtn.setForeground(Color.WHITE);
        changeBtn.addActionListener(e -> {
            String currentPassword = new String(currentPwdField.getPassword());
            String newPassword = new String(newPwdField.getPassword());
            String confirmPassword = new String(confirmPwdField.getPassword());
            
            // Validate current password
            if (!currentPassword.equals(user.getPassword())) {
                JOptionPane.showMessageDialog(passwordDialog,
                    "Current password is incorrect",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE);
                currentPwdField.selectAll();
                currentPwdField.requestFocus();
                return;
            }
            
            // Validate new password
            if (newPassword.length() < 4) {
                JOptionPane.showMessageDialog(passwordDialog,
                    "New password must be at least 4 characters",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                newPwdField.selectAll();
                newPwdField.requestFocus();
                return;
            }
            
            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(passwordDialog,
                    "Passwords do not match",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                confirmPwdField.selectAll();
                confirmPwdField.requestFocus();
                return;
            }
            
            // Change password
            try {
                user.setPassword(newPassword);
                userService.updateUser(user);
                
                JOptionPane.showMessageDialog(passwordDialog,
                    "Password changed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                passwordDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(passwordDialog,
                    "Error changing password: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> passwordDialog.dispose());
        
        buttonPanel.add(changeBtn);
        buttonPanel.add(cancelBtn);
        
        passwordDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        SwingUtilities.invokeLater(() -> currentPwdField.requestFocus());
        passwordDialog.setVisible(true);
    }
    
    public boolean isUserUpdated() {
        return userUpdated;
    }
    
    public User getUser() {
        return user;
    }
}
