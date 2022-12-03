package io.github.lmikoto.railgun.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.MessageType;

/**
 * @author jinwq
 * @Date 2022/11/27 20:20
 */
public class NotificationUtils {

    public static void simpleNotify(String content) {
        NotificationGroup first_plugin_id = new NotificationGroup("rail-gun", NotificationDisplayType.BALLOON, true);
        Notification notification = first_plugin_id.createNotification(content, MessageType.INFO);
        Notifications.Bus.notify(notification);
    }
}
