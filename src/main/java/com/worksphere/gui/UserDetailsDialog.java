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
        
        JLabel titleLabel = new JLabel("👤 User Details");
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
    
    public boolean isUserUpdated() {
        return userUpdated;
    }
    
    public User getUser() {
        return user;
    }
}
