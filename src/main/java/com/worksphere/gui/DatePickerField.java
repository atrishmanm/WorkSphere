package com.worksphere.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom date picker component that shows a popup calendar when clicked
 */
public class DatePickerField extends JPanel {
    private JTextField dateField;
    private JButton calendarButton;
    private JDialog calendarDialog;
    private Calendar selectedDate;
    private boolean dateEnabled;
    private JCheckBox enableCheckbox;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public DatePickerField() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDateEnabled(false);
    }
    
    private void initializeComponents() {
        // Checkbox to enable/disable date
        enableCheckbox = new JCheckBox("Set Due Date", false);
        enableCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Date text field (read-only)
        dateField = new JTextField(10);
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        dateField.setFont(new Font("Arial", Font.PLAIN, 12));
        dateField.setText("Select Date");
        
        // Calendar button
        calendarButton = new JButton("Cal");
        calendarButton.setPreferredSize(new Dimension(35, 25));
        calendarButton.setFont(new Font("Arial", Font.BOLD, 10));
        calendarButton.setToolTipText("Open Calendar");
        calendarButton.setBackground(new Color(52, 144, 220));
        calendarButton.setForeground(Color.WHITE);
        
        selectedDate = Calendar.getInstance();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(5, 0));
        
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(dateField, BorderLayout.CENTER);
        fieldPanel.add(calendarButton, BorderLayout.EAST);
        
        add(enableCheckbox, BorderLayout.WEST);
        add(fieldPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        enableCheckbox.addActionListener(e -> {
            setDateEnabled(enableCheckbox.isSelected());
        });
        
        calendarButton.addActionListener(e -> showCalendarDialog());
        
        dateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (dateEnabled) {
                    showCalendarDialog();
                }
            }
        });
    }
    
    private void showCalendarDialog() {
        if (!dateEnabled) return;
        
        if (calendarDialog == null) {
            createCalendarDialog();
        }
        
        // Position dialog to ensure it's fully visible
        Point location = getLocationOnScreen();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = calendarDialog.getPreferredSize();
        
        // Calculate position to avoid going off screen
        int x = location.x;
        int y = location.y + getHeight();
        
        // Adjust if dialog would go off the right edge of screen
        if (x + dialogSize.width > screenSize.width) {
            x = screenSize.width - dialogSize.width - 10;
        }
        
        // Adjust if dialog would go off the bottom edge of screen
        if (y + dialogSize.height > screenSize.height) {
            y = location.y - dialogSize.height;
        }
        
        calendarDialog.setLocation(x, y);
        calendarDialog.setVisible(true);
    }
    
    private void createCalendarDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        calendarDialog = new JDialog(parentWindow, "Select Date", Dialog.ModalityType.APPLICATION_MODAL);
        calendarDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        JPanel calendarPanel = createCalendarPanel();
        calendarDialog.add(calendarPanel);
        calendarDialog.pack();
        calendarDialog.setResizable(false);
        
        // Ensure minimum size
        Dimension size = calendarDialog.getSize();
        if (size.width < 300) size.width = 300;
        if (size.height < 350) size.height = 350;
        calendarDialog.setSize(size);
    }
    
    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);
        
        // Header with month/year navigation
        JPanel headerPanel = createHeaderPanel();
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Calendar grid
        JPanel calendarGrid = createCalendarGrid();
        panel.add(calendarGrid, BorderLayout.CENTER);
        
        // Footer with buttons
        JPanel footerPanel = createFooterPanel();
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        
        JButton prevButton = new JButton("<");
        prevButton.setFont(new Font("Arial", Font.BOLD, 12));
        prevButton.setPreferredSize(new Dimension(30, 25));
        prevButton.addActionListener(e -> {
            selectedDate.add(Calendar.MONTH, -1);
            updateCalendarDialog();
        });
        
        JButton nextButton = new JButton(">");
        nextButton.setFont(new Font("Arial", Font.BOLD, 12));
        nextButton.setPreferredSize(new Dimension(30, 25));
        nextButton.addActionListener(e -> {
            selectedDate.add(Calendar.MONTH, 1);
            updateCalendarDialog();
        });
        
        JLabel monthYearLabel = new JLabel();
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 14));
        monthYearLabel.setForeground(new Color(52, 144, 220));
        updateMonthYearLabel(monthYearLabel);
        
        header.add(prevButton, BorderLayout.WEST);
        header.add(monthYearLabel, BorderLayout.CENTER);
        header.add(nextButton, BorderLayout.EAST);
        
        return header;
    }
    
    private void updateMonthYearLabel(JLabel label) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        String monthYear = months[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.YEAR);
        label.setText(monthYear);
    }
    
    private JPanel createCalendarGrid() {
        JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
        grid.setBackground(Color.WHITE);
        
        // Day headers
        String[] dayHeaders = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String day : dayHeaders) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 11));
            dayLabel.setForeground(Color.GRAY);
            grid.add(dayLabel);
        }
        
        // Calendar days
        Calendar cal = (Calendar) selectedDate.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Empty cells before first day
        for (int i = 0; i < firstDayOfWeek; i++) {
            grid.add(new JLabel(""));
        }
        
        // Days of month
        Calendar today = Calendar.getInstance();
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 11));
            dayButton.setPreferredSize(new Dimension(30, 25));
            dayButton.setBorder(BorderFactory.createEmptyBorder());
            dayButton.setBackground(Color.WHITE);
            dayButton.setOpaque(true);
            
            // Highlight today
            cal.set(Calendar.DAY_OF_MONTH, day);
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                dayButton.setBackground(new Color(52, 144, 220));
                dayButton.setForeground(Color.WHITE);
                dayButton.setBorder(BorderFactory.createRaisedBevelBorder());
            }
            
            final int selectedDay = day;
            dayButton.addActionListener(e -> {
                selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);
                updateDateField();
                calendarDialog.setVisible(false);
            });
            
            dayButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!dayButton.getBackground().equals(new Color(52, 144, 220))) {
                        dayButton.setBackground(new Color(230, 240, 250));
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!dayButton.getBackground().equals(new Color(52, 144, 220))) {
                        dayButton.setBackground(Color.WHITE);
                    }
                }
            });
            
            grid.add(dayButton);
        }
        
        return grid;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton todayButton = new JButton("Today");
        todayButton.setFont(new Font("Arial", Font.PLAIN, 11));
        todayButton.addActionListener(e -> {
            selectedDate = Calendar.getInstance();
            updateDateField();
            calendarDialog.setVisible(false);
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 11));
        cancelButton.addActionListener(e -> calendarDialog.setVisible(false));
        
        footer.add(todayButton);
        footer.add(cancelButton);
        
        return footer;
    }
    
    private void updateCalendarDialog() {
        if (calendarDialog != null) {
            calendarDialog.getContentPane().removeAll();
            calendarDialog.add(createCalendarPanel());
            calendarDialog.revalidate();
            calendarDialog.repaint();
        }
    }
    
    private void updateDateField() {
        if (selectedDate != null) {
            LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dateField.setText(localDate.format(DATE_FORMATTER));
        }
    }
    
    public void setDateEnabled(boolean enabled) {
        this.dateEnabled = enabled;
        enableCheckbox.setSelected(enabled);
        dateField.setEnabled(enabled);
        calendarButton.setEnabled(enabled);
        
        if (!enabled) {
            dateField.setText("Select Date");
            dateField.setBackground(Color.LIGHT_GRAY);
        } else {
            dateField.setBackground(Color.WHITE);
            if (selectedDate != null) {
                updateDateField();
            }
        }
    }
    
    public boolean isDateEnabled() {
        return dateEnabled;
    }
    
    public LocalDate getSelectedDate() {
        if (!dateEnabled || selectedDate == null) {
            return null;
        }
        return selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            selectedDate = Calendar.getInstance();
            selectedDate.setTime(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            setDateEnabled(true);
            updateDateField();
        } else {
            setDateEnabled(false);
        }
    }
}
