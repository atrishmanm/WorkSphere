package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for creating new users
 */
public class CreateUserDialog extends JDialog {
    
    private UserService userService;
    private User createdUser;
    private boolean userCreated = false;
    private boolean isAdminCreating;
    
    private JTextField usernameField;
    private JTextField nameField;
    private JTextField emailField;
    private JCheckBox isAdminCheckbox;
    private JButton createButton;
    private JButton cancelButton;
    
    public CreateUserDialog(Window parent, UserService userService) {
        this(parent, userService, false);
    }
    
    public CreateUserDialog(Window parent, UserService userService, boolean isAdminCreating) {
        super(parent, "Create New User", ModalityType.APPLICATION_MODAL);
        this.userService = userService;
        this.isAdminCreating = isAdminCreating;
        
        initializeDialog();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeDialog() {
        setSize(550, 500);  // Increased both width and height
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);  // Make it resizable so user can adjust if needed
        setMinimumSize(new Dimension(500, 450));  // Set minimum size
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(60, 160, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("👤 Create New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username (required):");
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Name field
        JLabel nameLabel = new JLabel("Full Name (required):");
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Email field
        JLabel emailLabel = new JLabel("Email (optional):");
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Admin role checkbox (only visible for admin users)
        isAdminCheckbox = new JCheckBox("Make this user an administrator");
        isAdminCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        isAdminCheckbox.setVisible(isAdminCreating);
        
        // Add form components
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(nameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(emailField);
        
        if (isAdminCreating) {
            formPanel.add(Box.createVerticalStrut(15));
            formPanel.add(isAdminCheckbox);
        }
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        createButton = new JButton("Create User");
        createButton.setPreferredSize(new Dimension(100, 30));
        createButton.setBackground(new Color(60, 160, 60));
        createButton.setForeground(Color.WHITE);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createUser();
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
    
    private void createUser() {
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        
        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username is required",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Full name is required",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        // Check if username already exists
        try {
            User existingUser = userService.getUserByUsername(username);
            if (existingUser != null) {
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
        
        // Create user
        try {
            // Set email to null if empty
            if (email.isEmpty()) {
                email = null;
            }
            
            // Determine if user should be admin (only if admin is creating and checkbox is selected)
            boolean isAdmin = isAdminCreating && isAdminCheckbox.isSelected();
            
            createdUser = userService.createUser(username, name, email, isAdmin);
            userCreated = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to create user: " + e.getMessage(),
                "Creation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isUserCreated() {
        return userCreated;
    }
    
    public User getCreatedUser() {
        return createdUser;
    }
}
