package com.worksphere.gui;

import com.worksphere.model.User;
import com.worksphere.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementPanel extends JPanel {
    
    private UserService userService;
    private WorkSphereGUI mainFrame;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private static final String[] COLUMN_NAMES = {"ID", "Username", "Full Name", "Email", "Created Date", "Actions"};
    
    public UserManagementPanel(UserService userService, WorkSphereGUI mainFrame) {
        this.userService = userService;
        this.mainFrame = mainFrame;
        initializePanel();
        setupLayout();
        loadUsers();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupLayout() {
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        createUserTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Users"));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> applySearch());
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> applySearch());
        searchPanel.add(searchBtn);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearSearch());
        searchPanel.add(clearBtn);
        return searchPanel;
    }
    
    private void createUserTable() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(30);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(0).setMaxWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(5).setCellRenderer(new UserActionButtonRenderer());
        userTable.getColumnModel().getColumn(5).setCellEditor(new UserActionButtonEditor());
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = userTable.getSelectedRow();
                    if (row >= 0) {
                        viewUserDetails(row);
                    }
                }
            }
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newUserBtn = new JButton("New User");
        newUserBtn.addActionListener(e -> createNewUser());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        buttonPanel.add(newUserBtn);
        buttonPanel.add(refreshBtn);
        return buttonPanel;
    }
    
    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            updateTableData(users);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTableData(List<User> users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            Object[] row = {user.getId(), user.getUsername(), user.getName(), user.getEmail() != null ? user.getEmail() : "N/A", user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), "Actions"};
            tableModel.addRow(row);
        }
    }
    
    private void applySearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadUsers();
            return;
        }
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> filteredUsers = allUsers.stream().filter(user -> user.getUsername().toLowerCase().contains(searchText) || user.getName().toLowerCase().contains(searchText) || (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText))).collect(Collectors.toList());
            updateTableData(filteredUsers);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearSearch() {
        searchField.setText("");
        loadUsers();
    }
    
    private void createNewUser() {
        User currentUser = mainFrame.getCurrentUser();
        boolean isAdminCreating = currentUser != null && currentUser.isAdmin();
        CreateUserDialog createUserDialog = new CreateUserDialog(SwingUtilities.getWindowAncestor(this), userService, isAdminCreating);
        createUserDialog.setVisible(true);
        if (createUserDialog.isUserCreated()) {
            refresh();
            JOptionPane.showMessageDialog(this, "User '" + createUserDialog.getCreatedUser().getUsername() + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void viewUserDetails(int row) {
        try {
            int userId = (Integer) tableModel.getValueAt(row, 0);
            User user = userService.getUserById(userId);
            if (user != null) {
                UserDetailsDialog detailsDialog = new UserDetailsDialog(SwingUtilities.getWindowAncestor(this), user, userService);
                detailsDialog.setVisible(true);
                if (detailsDialog.isUserUpdated()) {
                    refresh();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error viewing user details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteUser(int row) {
        try {
            int userId = (Integer) tableModel.getValueAt(row, 0);
            String username = (String) tableModel.getValueAt(row, 1);
            int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user:\n'" + username + "'?\n\nWarning: This will also delete all tasks assigned to this user!", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                userService.deleteUser(userId);
                refresh();
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void refresh() {
        if (searchField.getText().trim().isEmpty()) {
            loadUsers();
        } else {
            applySearch();
        }
    }
    
    private class UserActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editBtn;
        private JButton deleteBtn;
        
        public UserActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            setOpaque(true);
            editBtn = new JButton("Edit");
            editBtn.setPreferredSize(new Dimension(55, 25));
            editBtn.setFont(new Font("Arial", Font.PLAIN, 10));
            editBtn.setBackground(new Color(52, 144, 220));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            editBtn.setFocusPainted(false);
            deleteBtn = new JButton("Delete");
            deleteBtn.setPreferredSize(new Dimension(60, 25));
            deleteBtn.setFont(new Font("Arial", Font.PLAIN, 10));
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            deleteBtn.setFocusPainted(false);
            add(editBtn);
            add(deleteBtn);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }
    
    private class UserActionButtonEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private JButton editBtn;
        private JButton deleteBtn;
        private int currentRow;
        
        public UserActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            panel.setOpaque(true);
            editBtn = new JButton("Edit");
            editBtn.setPreferredSize(new Dimension(55, 25));
            editBtn.setFont(new Font("Arial", Font.PLAIN, 10));
            editBtn.setBackground(new Color(52, 144, 220));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            editBtn.setFocusPainted(false);
            editBtn.addActionListener(e -> {
                viewUserDetails(currentRow);
                fireEditingStopped();
            });
            deleteBtn = new JButton("Delete");
            deleteBtn.setPreferredSize(new Dimension(60, 25));
            deleteBtn.setFont(new Font("Arial", Font.PLAIN, 10));
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            deleteBtn.setFocusPainted(false);
            deleteBtn.addActionListener(e -> {
                deleteUser(currentRow);
                fireEditingStopped();
            });
            panel.add(editBtn);
            panel.add(deleteBtn);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }
}
