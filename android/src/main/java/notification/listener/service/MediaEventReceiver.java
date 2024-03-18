package notification.listener.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import io.flutter.plugin.common.EventChannel.EventSink;

import java.util.HashMap;

public class MediaEventReceiver extends BroadcastReceiver {

    private EventSink eventSink;

    public MediaEventReceiver(EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(MediaConstants.ID);
        String title = intent.getStringExtra(MediaConstants.TITLE);
        String album = intent.getStringExtra(MediaConstants.ALBUM);
        String artist = intent.getStringExtra(MediaConstants.ARTIST);
        String source = intent.getStringExtra(MediaConstants.SOURCE);
        long duration = intent.getLongExtra(MediaConstants.DURATION, 0);
        long position = intent.getLongExtra(MediaConstants.POSITION, 0);
        int state = intent.getIntExtra(MediaConstants.STATE, 0);
        int volumePercent = intent.getIntExtra(MediaConstants.VOLUME_PERCENT, 0);

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", title);
        data.put("album", album);
        data.put("artist", artist);
        data.put("source", source);
        data.put("duration", duration);
        data.put("position", position);
        data.put("state", state);
        data.put("volumePercent", volumePercent);

        eventSink.success(data);
    }
}
