package com.radix.anglerdebuzzer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class BackgroundService extends Service {

    public static final String TAG = BackgroundService.class.getName();
    public static final int NOTIFICATION_ID = 96;
    public static final String STOP_SERVICE_INTENT_FILTER = "stopService";

    private MediaPlayer mMediaPlayer;
    private HeadsetPlugStateReceiver mPlugStateReceiver;
    private StopServiceFromNotificationBarReceiver mStopServiceReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = MediaPlayer.create(this, R.raw.noaudio);
        mMediaPlayer.setLooping(true);

        Log.d(TAG, "started the headset state receiver");
        mPlugStateReceiver = new HeadsetPlugStateReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mPlugStateReceiver, filter);

        Log.d(TAG, "started the notification closure receiver");
        mStopServiceReceiver = new StopServiceFromNotificationBarReceiver();
        IntentFilter stopServiceFilter = new IntentFilter(STOP_SERVICE_INTENT_FILTER);
        registerReceiver(mStopServiceReceiver, stopServiceFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "started service");
        startAudioForegroundNotification();

        return Service.START_NOT_STICKY;
    }

    /**
     * Calling this method updates the foreground notification since the ID's are the same each time.
     */
    private void startAudioForegroundNotification() {
        // start a foreground notification for the playback
        String contentText = mMediaPlayer.isPlaying() ? "Audio playing" : "Audio not playing";
        int smallIconDrawable = android.R.drawable.divider_horizontal_dim_dark;

        // this intent fires off a "close service" broadcast that we're listening for earlier
        final PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(STOP_SERVICE_INTENT_FILTER), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(smallIconDrawable)
                .setContentTitle("Nexus 6p Debuzzer")
                .setContentText(contentText)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "close app", stopServicePendingIntent);

        Intent backToActivityIntent = new Intent(this, BlankActivity.class);
        final PendingIntent pi = PendingIntent.getActivity(this, 0, backToActivityIntent, 0);
        mBuilder.setContentIntent(pi);

        Notification mNotification = mBuilder.build();
        startForeground(NOTIFICATION_ID, mNotification);
    }

    private void startAudio() {
        // start the audio
        Log.d(TAG, "starting the audio");
        mMediaPlayer.start();
    }

    private void pauseAudio() {
        // pause the audio
        if (mMediaPlayer.isPlaying()) {
            Log.d(TAG, "pausing the audio");
            mMediaPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        unregisterReceiver(mPlugStateReceiver);
        unregisterReceiver(mStopServiceReceiver);
    }

    /**
     * If the headset is plugged in, play the audio. If not, pause it
     */
    private class HeadsetPlugStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        pauseAudio();

                        startAudioForegroundNotification();
                        break;

                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        startAudio();

                        startAudioForegroundNotification();
                        break;

                    default:
                        Log.d(TAG, "Headset in unknown state.");
                }
            }
        }
    }

    private class StopServiceFromNotificationBarReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "stopping the service from broadcast");
            stopSelf();
        }
    }
}
