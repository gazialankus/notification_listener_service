package notification.listener.service;

import static notification.listener.service.NotificationUtils.getBitmapFromDrawable;
import static notification.listener.service.models.ActionCache.cachedNotifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import notification.listener.service.models.Action;


@SuppressLint("OverrideAbstract")
@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {

    private TrackInfo trackInfo;

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        handleNotification(notification, false);
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        handleNotification(sbn, true);
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void handleNotification(StatusBarNotification notification, boolean isRemoved) {
        String packageName = notification.getPackageName();
        Bundle extras = notification.getNotification().extras;
        byte[] appIcon = getAppIcon(packageName);
        byte[] largeIcon = null;
        Action action = NotificationUtils.getQuickReplyAction(notification.getNotification(), packageName);

        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            largeIcon = getNotificationLargeIcon(getApplicationContext(), notification.getNotification());
        }

        SbnAndToken sbnAndToken = findTokenForSbn(notification);
        if (sbnAndToken != null && sbnAndToken.token != null) {
            boolean isHandled = handleMediaNotification(sbnAndToken.token, isRemoved);
            if (isHandled) return;
        }

        Intent intent = new Intent(NotificationConstants.INTENT);
        intent.putExtra(NotificationConstants.PACKAGE_NAME, packageName);
        intent.putExtra(NotificationConstants.ID, notification.getId());
        intent.putExtra(NotificationConstants.CAN_REPLY, action != null);

        if (NotificationUtils.getQuickReplyAction(notification.getNotification(), packageName) != null) {
            cachedNotifications.put(notification.getId(), action);
        }

        intent.putExtra(NotificationConstants.NOTIFICATIONS_ICON, appIcon);
        intent.putExtra(NotificationConstants.NOTIFICATIONS_LARGE_ICON, largeIcon);

        if (extras != null) {
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            intent.putExtra(NotificationConstants.NOTIFICATION_TITLE, title == null ? null : title.toString());
            intent.putExtra(NotificationConstants.NOTIFICATION_CONTENT, text == null ? null : text.toString());
            intent.putExtra(NotificationConstants.IS_REMOVED, isRemoved);
            intent.putExtra(NotificationConstants.HAVE_EXTRA_PICTURE, extras.containsKey(Notification.EXTRA_PICTURE));

            if (extras.containsKey(Notification.EXTRA_PICTURE)) {
                Bitmap bmp = (Bitmap) extras.get(Notification.EXTRA_PICTURE);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                intent.putExtra(NotificationConstants.EXTRAS_PICTURE, stream.toByteArray());
            }

        }
        intent.putExtra(NotificationConstants.IS_CLEARABLE, notification.isClearable());
        int flags = notification.getNotification().flags;
        Log.d("FLAGS", "Flags is : " + flags);
        intent.putExtra(NotificationConstants.FLAGS, flags);
        intent.putExtra(NotificationConstants.IS_GROUP, (flags & 512) != 0);
        intent.putExtra(NotificationConstants.PRIORITY, notification.getNotification().priority);

        sendBroadcast(intent);
    }

    private SbnAndToken findTokenForSbn(StatusBarNotification sbn) {
        MediaSession.Token token = getTokenIfAvailable(sbn);
        if (token == null) return null;

        MediaController controller = new MediaController(this, token);
        PlaybackState playbackState = controller.getPlaybackState();
        if (playbackState == null) return null;

        int state = playbackState.getState();
        if (state == PlaybackState.STATE_PLAYING) return new SbnAndToken(sbn, token);
        if (state == PlaybackState.STATE_PAUSED) return new SbnAndToken(sbn, token);

        return null;
    }

    private MediaSession.Token getTokenIfAvailable(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        Bundle bundle = notification.extras;
        return bundle.getParcelable("android.mediaSession");
    }

    private boolean handleMediaNotification(MediaSession.Token token, boolean isRemoved) {
        TrackInfo trackInfo = extractMediaFieldsForToken(token);
        if (trackInfo == null) return false;
        if (isRemoved) {
            finishPlaying(trackInfo);
        } else {
            this.trackInfo = trackInfo;
            sendTrack(trackInfo);
        }
        return true;
    }

    private void finishPlaying(TrackInfo trackInfo) {
        String id = trackInfo.getId();
        if (id == null) return;
        if (id.equals(trackInfo.getId())) {
            sendTrack(new TrackInfo());
        }
    }

    private void sendTrack(TrackInfo trackInfo) {
        Intent intent = new Intent(MediaConstants.INTENT);
        intent.putExtra(MediaConstants.ID, trackInfo.getId());
        intent.putExtra(MediaConstants.TITLE, trackInfo.getTitle());
        intent.putExtra(MediaConstants.ALBUM, trackInfo.getAlbum());
        intent.putExtra(MediaConstants.ARTIST, trackInfo.getArtist());
        intent.putExtra(MediaConstants.GENRE, trackInfo.getGenre());
        intent.putExtra(MediaConstants.SOURCE, trackInfo.getSource());
        intent.putExtra(MediaConstants.DURATION, trackInfo.getDuration());
        intent.putExtra(MediaConstants.POSITION, trackInfo.getPosition());
        intent.putExtra(MediaConstants.STATE, trackInfo.getState());
        intent.putExtra(MediaConstants.VOLUME_PERCENT, trackInfo.getVolumePercent());
        sendBroadcast(intent);
    }

    private TrackInfo extractMediaFieldsForToken(MediaSession.Token token) {
        MediaController controller = new MediaController(getApplicationContext(), token);
        PlaybackState playbackState = controller.getPlaybackState();
        if (playbackState == null) return null;
        MediaMetadata metadata = controller.getMetadata();
        if (metadata == null) return null;
        int state = getPlaybackState(playbackState);
        String id = deriveMediaId(metadata);
        if (state == MediaConstants.STATE_UNKNOWN) {
            trackInfo.clear();
            return null;
        }

        if (state == MediaConstants.STATE_PAUSED && trackInfo.getId() != null && !trackInfo.getId().equals(id)) return null;
        if (state == MediaConstants.STATE_STOPPED && !Objects.equals(trackInfo.getId(), id)) return null;

        TrackInfo trackInfo = new TrackInfo();
        trackInfo.setId(id);
        trackInfo.setTitle(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        trackInfo.setAlbum(metadata.getString(MediaMetadata.METADATA_KEY_ALBUM));
        trackInfo.setArtist(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));
        trackInfo.setGenre(metadata.getString(MediaMetadata.METADATA_KEY_GENRE));
        trackInfo.setSource(metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION));
        trackInfo.setDuration(metadata.getLong(MediaMetadata.METADATA_KEY_DURATION));
        trackInfo.setPosition(controller.getPlaybackState().getPosition());
        trackInfo.setState(state);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        trackInfo.setVolumePercent(volume * 100 / maxVolume);

        return trackInfo;
    }

    private int getPlaybackState(PlaybackState playbackState) {
        if (playbackState == null) return MediaConstants.STATE_UNKNOWN;
        switch (playbackState.getState()) {
            case PlaybackState.STATE_PLAYING:
                return MediaConstants.STATE_PLAYING;
            case PlaybackState.STATE_PAUSED:
                return MediaConstants.STATE_PAUSED;
            case PlaybackState.STATE_STOPPED:
                return MediaConstants.STATE_STOPPED;
            default:
                return MediaConstants.STATE_UNKNOWN;
        }
    }

    private String deriveMediaId(MediaMetadata mediaMetadata) {
        String album = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
        String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
        String artist = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        return title + ":" + artist + ":" + album;
    }

    public byte[] getAppIcon(String packageName) {
        try {
            PackageManager manager = getBaseContext().getPackageManager();
            Drawable icon = manager.getApplicationIcon(packageName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            getBitmapFromDrawable(icon).compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    private byte[] getNotificationLargeIcon(Context context, Notification notification) {
        try {
            Icon largeIcon = notification.getLargeIcon();
            if (largeIcon == null) {
                return null;
            }
            Drawable iconDrawable = largeIcon.loadDrawable(context);
            Bitmap iconBitmap = ((BitmapDrawable) iconDrawable).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROR LARGE ICON", "getNotificationLargeIcon: " + e.getMessage());
            return null;
        }
    }

}

class SbnAndToken {
    public StatusBarNotification sbn;
    public MediaSession.Token token;

    public SbnAndToken(StatusBarNotification sbn, MediaSession.Token token) {
        this.sbn = sbn;
        this.token = token;
    }
}
