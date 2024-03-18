package notification.listener.service;

import static notification.listener.service.NotificationConstants.*;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;

import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.EventChannel.EventSink;

import java.util.HashMap;

public class NotificationReceiver extends BroadcastReceiver {

    private EventSink eventSink;

    public NotificationReceiver(EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra(PACKAGE_NAME);
        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NOTIFICATION_CONTENT);
        byte[] notificationIcon = intent.getByteArrayExtra(NOTIFICATIONS_ICON);
        byte[] notificationExtrasPicture = intent.getByteArrayExtra(EXTRAS_PICTURE);
        byte[] largeIcon = intent.getByteArrayExtra(NOTIFICATIONS_LARGE_ICON);
        boolean haveExtraPicture = intent.getBooleanExtra(HAVE_EXTRA_PICTURE, false);
        boolean hasRemoved = intent.getBooleanExtra(IS_REMOVED, false);
        boolean canReply = intent.getBooleanExtra(CAN_REPLY, false);
        int id = intent.getIntExtra(ID, -1);
        boolean isClearable = intent.getBooleanExtra(IS_CLEARABLE, false);
        boolean isGroup = intent.getBooleanExtra(IS_GROUP, false);
        int priority = intent.getIntExtra(PRIORITY, Notification.PRIORITY_DEFAULT);
        int flags = intent.getIntExtra(FLAGS, -1);


        HashMap<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("packageName", packageName);
        data.put("title", title);
        data.put("content", content);
        data.put("notificationIcon", notificationIcon);
        data.put("notificationExtrasPicture", notificationExtrasPicture);
        data.put("haveExtraPicture", haveExtraPicture);
        data.put("largeIcon", largeIcon);
        data.put("hasRemoved", hasRemoved);
        data.put("canReply", canReply);
        data.put("isClearable", isClearable);
        data.put("isGroup", isGroup);
        data.put("priority", priority);
        data.put("flags", flags);

        eventSink.success(data);
    }
}
