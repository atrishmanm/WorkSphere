package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Login Dialog for user authentication
 */
public class LoginDialog extends JDialog {
    
    private UserService userService;
    private User loggedInUser;
    private boolean loginSuccessful = false;
    
    private JTextField usernameField;
    private JButton loginButton;
    private JButton createUserButton;
    private JButton exitButton;
    
    public LoginDialog(Frame parent, UserService userService) {
        super(parent, "Login - WorkSphere", true);
        this.userService = userService;
        
        initializeDialog();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeDialog() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        // Add window listener to handle close button
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // When user clicks X button, show confirmation dialog
                int option = JOptionPane.showConfirmDialog(
                    LoginDialog.this,
                    "Are you sure you want to exit WorkSphere?",
                    "Exit Application",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("WorkSphere");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Username section
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(80, 30));
        
        createUserButton = new JButton("Create User");
        createUserButton.setPreferredSize(new Dimension(100, 30));
        
        exitButton = new JButton("Exit");
        exitButton.setPreferredSize(new Dimension(80, 30));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(createUserButton);
        buttonPanel.add(exitButton);
        
        // Add components with spacing
        mainPanel.add(usernameLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel infoLabel = new JLabel("<html><center>💡 Try username: <b>admin</b><br/>Or create a new user</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Create user button action
        createUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateUserDialog();
            }
        });
        
        // Exit button action
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // Enter key in username field
        usernameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        // Set focus to username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a username",
                "Login Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            loggedInUser = userService.getUserByUsername(username);
            
            if (loggedInUser != null) {
                loginSuccessful = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "User '" + username + "' not found.\nPlease create a new user or try a different username.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
                usernameField.selectAll();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Login failed: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showCreateUserDialog() {
        CreateUserDialog createUserDialog = new CreateUserDialog(this, userService);
        createUserDialog.setVisible(true);
        
        if (createUserDialog.isUserCreated()) {
            User newUser = createUserDialog.getCreatedUser();
            usernameField.setText(newUser.getUsername());
            
            JOptionPane.showMessageDialog(this,
                "User '" + newUser.getUsername() + "' created successfully!\nYou can now login.",
                "User Created",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
    
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
