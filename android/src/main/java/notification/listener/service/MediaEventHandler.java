package notification.listener.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import io.flutter.plugin.common.EventChannel;

public class MediaEventHandler implements EventChannel.StreamHandler {
    public MediaEventHandler(Context context) {
        this.context = context;
    }
    private Context context;
    private MediaEventReceiver mediaEventReceiver;

    @SuppressLint("WrongConstant")
    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaConstants.INTENT);
        mediaEventReceiver = new MediaEventReceiver(events);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.registerReceiver(mediaEventReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(mediaEventReceiver, intentFilter);
        }
        Intent listenerIntent = new Intent(context, MediaEventReceiver.class);
        context.startService(listenerIntent);
        Log.i("MediaPlugin", "Started the media tracking service.");
    }

    @Override
    public void onCancel(Object arguments) {
        context.unregisterReceiver(mediaEventReceiver);
        mediaEventReceiver = null;
    }
}
