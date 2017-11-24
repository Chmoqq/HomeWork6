package Lesson6;

import sun.security.util.SecurityConstants;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class SystemNotifications {

    private static SystemNotifications notifications;

    private TrayIcon trayIcon;
    private SystemNotifications() {
    }

    private void Initialize() throws AWTException {
        SystemTray systemTray = SystemTray.getSystemTray();

        Image iconImage = Toolkit.getDefaultToolkit().createImage("C:\\Users\\Ivan\\Desktop\\photo12.png");

        trayIcon = new TrayIcon(iconImage, "Test");
        trayIcon.setImageAutoSize(true);
        systemTray.add(trayIcon);
    }

    public static SystemNotifications getInstance() {
        if (notifications == null) {
            notifications = new SystemNotifications();
        }

        return notifications;
    }


    public boolean DisplayTray(String client, String msg) {
        if (!SystemTray.isSupported())
            return false;

        try {
            if (this.trayIcon == null)
                this.Initialize();

        } catch (AWTException e) {
            return false;
        }

        trayIcon.setToolTip(client);
        trayIcon.displayMessage(client, msg, MessageType.WARNING);
        return true;
    }
}
