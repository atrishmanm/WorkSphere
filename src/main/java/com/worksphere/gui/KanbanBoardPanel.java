package com.worksphere.gui;

import com.worksphere.model.Task;
import com.worksphere.model.TaskStatus;
import com.worksphere.model.User;
import com.worksphere.service.TaskService;
import com.worksphere.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.sql.SQLException;
import java.util.List;

/**
 * Kanban Board Panel for drag-and-drop task management
 */
public class KanbanBoardPanel extends JPanel {
    
    private TaskService taskService;
    private UserService userService;
    private User currentUser;
    
    // Kanban columns
    private JPanel todoColumn;
    private JPanel inProgressColumn;
    private JPanel completedColumn;
    
    // Task lists for each column
    private DefaultListModel<Task> todoModel;
    private DefaultListModel<Task> inProgressModel;
    private DefaultListModel<Task> completedModel;
    
    private JList<Task> todoList;
    private JList<Task> inProgressList;
    private JList<Task> completedList;
    
    public KanbanBoardPanel(TaskService taskService, UserService userService, User currentUser) {
        this.taskService = taskService;
        this.userService = userService;
        this.currentUser = currentUser;
        
        initializePanel();
        setupDragAndDrop();
        refresh();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("📋 Kanban Board");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(70, 130, 180));
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.setBackground(new Color(108, 117, 125));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refresh());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main kanban board
        JPanel boardPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        boardPanel.setBackground(Color.WHITE);
        
        // Create columns
        todoColumn = createKanbanColumn("📝 To-Do", new Color(108, 117, 125));
        inProgressColumn = createKanbanColumn("🔄 In Progress", new Color(255, 193, 7));
        completedColumn = createKanbanColumn("✅ Completed", new Color(40, 167, 69));
        
        boardPanel.add(todoColumn);
        boardPanel.add(inProgressColumn);
        boardPanel.add(completedColumn);
        
        add(boardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createKanbanColumn(String title, Color headerColor) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Header
        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setPreferredSize(new Dimension(0, 50));
        headerLabel.setBackground(headerColor);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        column.add(headerLabel, BorderLayout.NORTH);
        
        return column;
    }
    
    private void setupDragAndDrop() {
        // Initialize list models
        todoModel = new DefaultListModel<>();
        inProgressModel = new DefaultListModel<>();
        completedModel = new DefaultListModel<>();
        
        // Create task lists
        todoList = createTaskList(todoModel);
        inProgressList = createTaskList(inProgressModel);
        completedList = createTaskList(completedModel);
        
        // Add lists to columns
        todoColumn.add(new JScrollPane(todoList), BorderLayout.CENTER);
        inProgressColumn.add(new JScrollPane(inProgressList), BorderLayout.CENTER);
        completedColumn.add(new JScrollPane(completedList), BorderLayout.CENTER);
    }
    
    private JList<Task> createTaskList(DefaultListModel<Task> model) {
        JList<Task> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new TaskCardRenderer());
        list.setBackground(new Color(248, 249, 250));
        
        // Enable drag and drop
        list.setDragEnabled(true);
        list.setTransferHandler(new TaskTransferHandler());
        
        // Add double-click listener to edit task
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        Task selectedTask = model.getElementAt(index);
                        openTaskEditDialog(selectedTask);
                    }
                }
            }
        });
        list.setDropMode(DropMode.INSERT);
        
        return list;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        refresh();
    }
    
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Clear all models
                todoModel.clear();
                inProgressModel.clear();
                completedModel.clear();
                
                // Get tasks based on user role
                List<Task> tasks;
                if (currentUser != null && currentUser.isAdmin()) {
                    tasks = taskService.getAllTasks();
                } else if (currentUser != null) {
                    tasks = taskService.getTasksForUser(currentUser.getId());
                } else {
                    tasks = List.of(); // Empty list if no user
                }
                
                // Sort tasks into appropriate columns
                for (Task task : tasks) {
                    switch (task.getStatus()) {
                        case TODO:
                            todoModel.addElement(task);
                            break;
                        case IN_PROGRESS:
                            inProgressModel.addElement(task);
                            break;
                        case COMPLETED:
                            completedModel.addElement(task);
                            break;
                    }
                }
                
                // Update column headers with counts
                updateColumnHeaders();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading tasks: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void updateColumnHeaders() {
        ((JLabel) todoColumn.getComponent(0)).setText(
            String.format("📝 To-Do (%d)", todoModel.getSize()));
        ((JLabel) inProgressColumn.getComponent(0)).setText(
            String.format("🔄 In Progress (%d)", inProgressModel.getSize()));
        ((JLabel) completedColumn.getComponent(0)).setText(
            String.format("✅ Completed (%d)", completedModel.getSize()));
    }
    
    /**
     * Custom cell renderer for task cards in Kanban board
     */
    private class TaskCardRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof Task) {
                Task task = (Task) value;
                
                JPanel card = new JPanel();
                card.setLayout(new BorderLayout());
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedSoftBevelBorder(),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                card.setPreferredSize(new Dimension(0, 90));
                card.setBackground(isSelected ? new Color(230, 240, 250) : Color.WHITE);
                
                // Task title
                JLabel titleLabel = new JLabel("<html><b>" + task.getTitle() + "</b></html>");
                titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                titleLabel.setForeground(new Color(33, 37, 41));
                
                // Task details
                StringBuilder details = new StringBuilder();
                if (task.getDueDate() != null) {
                    details.append("📅 ").append(task.getDueDate().toString()).append("  ");
                }
                details.append("⭐ ").append(task.getPriority().getDisplayName());
                
                JLabel detailsLabel = new JLabel("<html>" + details.toString() + "</html>");
                detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                detailsLabel.setForeground(new Color(108, 117, 125));
                
                // Assignee (if not current user)
                if (task.getAssignedTo() != null && 
                    (currentUser == null || !task.getAssignedTo().equals(currentUser.getId()))) {
                    JLabel assigneeLabel = new JLabel("👤 " + getAssigneeName(task.getAssignedTo()));
                    assigneeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    assigneeLabel.setForeground(new Color(108, 117, 125));
                    
                    JPanel bottomPanel = new JPanel(new BorderLayout());
                    bottomPanel.setOpaque(false);
                    bottomPanel.add(detailsLabel, BorderLayout.WEST);
                    bottomPanel.add(assigneeLabel, BorderLayout.EAST);
                    
                    card.add(titleLabel, BorderLayout.NORTH);
                    card.add(bottomPanel, BorderLayout.SOUTH);
                } else {
                    card.add(titleLabel, BorderLayout.NORTH);
                    card.add(detailsLabel, BorderLayout.SOUTH);
                }
                
                return card;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        
        private String getAssigneeName(Integer userId) {
            try {
                User user = userService.getUserById(userId);
                return user != null ? user.getFullName() : "Unknown";
            } catch (Exception e) {
                return "Unknown";
            }
        }
    }
    
    /**
     * Transfer handler for drag and drop functionality
     */
    private class TaskTransferHandler extends TransferHandler {
        
        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JList<?>) {
                @SuppressWarnings("unchecked")
                JList<Task> list = (JList<Task>) c;
                Task task = list.getSelectedValue();
                if (task != null) {
                    return new TaskTransferable(task);
                }
            }
            return null;
        }
        
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(TaskTransferable.TASK_FLAVOR);
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            
            try {
                Task task = (Task) support.getTransferable().getTransferData(TaskTransferable.TASK_FLAVOR);
                Component component = support.getComponent();
                if (component instanceof JList<?>) {
                    @SuppressWarnings("unchecked")
                    JList<Task> targetList = (JList<Task>) component;
                
                    // Determine new status based on target list
                    TaskStatus newStatus = getStatusForList(targetList);
                    
                    if (newStatus != null && !newStatus.equals(task.getStatus())) {
                        // Update task status
                        task.setStatus(newStatus);
                        taskService.updateTask(task);
                        
                        // Refresh the board
                        refresh();
                        
                        return true;
                    }
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(KanbanBoardPanel.this,
                    "Error moving task: " + e.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
            return false;
        }
        
        private TaskStatus getStatusForList(JList<Task> list) {
            if (list == todoList) {
                return TaskStatus.TODO;
            } else if (list == inProgressList) {
                return TaskStatus.IN_PROGRESS;
            } else if (list == completedList) {
                return TaskStatus.COMPLETED;
            }
            return null;
        }
    }
    
    /**
     * Transferable implementation for Task objects
     */
    private static class TaskTransferable implements Transferable {
        
        public static final DataFlavor TASK_FLAVOR = new DataFlavor(Task.class, "Task");
        private final Task task;
        
        public TaskTransferable(Task task) {
            this.task = task;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{TASK_FLAVOR};
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return TASK_FLAVOR.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return task;
        }
    }
    
    private void openTaskEditDialog(Task task) {
        try {
            TaskDialog dialog = new TaskDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                taskService,
                userService,
                task
            );
            dialog.setVisible(true);
            
            if (dialog.isTaskSaved()) {
                refresh(); // Refresh the kanban board after editing
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error opening task dialog: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}