package com.worksphere.util;

import javax.swing.*;
import java.awt.*;

public class NotificationUtil {
    /**
     * Show a desktop notification using SystemTray if available, otherwise fallback to dialog.
     */
    public static void showNotification(Component parent, String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                // Use a blank image or provide a path to an icon
                Image image = Toolkit.getDefaultToolkit().createImage("");
                TrayIcon trayIcon = new TrayIcon(image, "WorkSphere");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                // Remove the icon after showing notification
                Timer timer = new Timer(3000, e -> tray.remove(trayIcon));
                timer.setRepeats(false);
                timer.start();
            } catch (Exception e) {
                showDialogNotification(parent, title, message);
            }
        } else {
            showDialogNotification(parent, title, message);
        }
    }

    private static void showDialogNotification(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void playNotificationSound() {
        Toolkit.getDefaultToolkit().beep();
    }
}
