package com.anmolthedeveloper.anmolsingh.chargeralarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class MyService extends Service {
    private static final String CHANNEL_ID = "101";
    MediaPlayer mediaPlayer;
    Vibrator vibrator;
    CameraManager cameraManager;
    String cameraId;
    Thread t;
    boolean isFlashAvailable;
    public boolean vibratorFlag;
    public boolean flashFlag;
    SharedPreferences mSharedPreferences;
    NotificationCompat.Builder builder;
    AudioManager audioManager;
    NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = null;
        if(vibratorFlag)
            vibrator.cancel();

        manager.cancelAll();
        if(flashFlag && isFlashAvailable) {
            try {
                    cameraManager.setTorchMode(cameraId, false);
            } catch ( CameraAccessException e) {
                e.printStackTrace();
            }

            t.interrupt();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mSharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, MODE_PRIVATE);
        vibratorFlag = mSharedPreferences.getBoolean(MainActivity.Vibration, true);
        flashFlag = mSharedPreferences.getBoolean(MainActivity.Torch, true);

        // Notification
        createNotificationChannel();
        Intent stopIntent = new Intent(this, StopMyService.class);
        Intent landingIntent = new Intent(this, MainActivity.class);
        landingIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        PendingIntent landingPendingIntent = PendingIntent.getActivity(this, 0, landingIntent, PendingIntent.FLAG_ONE_SHOT);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_arrow)
                .setContentTitle("ALARMING!!!")
                .setContentText("Charger removed, Action Required...")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(landingPendingIntent)
                .addAction(R.drawable.ic_stop, "STOP",stopPendingIntent)
                .setAutoCancel(true);
        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(101, builder.build());
        manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.cancel(101);


        // Increasing Audio
        audioManager = (AudioManager) MyService.this.getSystemService(Context.AUDIO_SERVICE);
        int media_max_volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int media_current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (media_current_volume<media_max_volume)
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC, // Stream type
                    media_max_volume, // Index
                    AudioManager.FLAG_VIBRATE // Flags
            );
        audioManager = null;


        if(flashFlag && isFlashAvailable) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch ( CameraAccessException e) {
                e.printStackTrace();
            }
        }

        if (mediaPlayer==null) {
            Resources res = getResources();
            String url = mSharedPreferences.getString(MainActivity.Siren,"sirenone");
            Integer resIdSound = res.getIdentifier (url,  "raw", this.getPackageName());
            mediaPlayer = MediaPlayer.create( this, resIdSound);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        if (vibratorFlag){
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                long[] pattern = {0, 100, 1000};
                vibrator.vibrate(pattern, 1);
            }
        }
        isFlashAvailable = getApplication().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (flashFlag && isFlashAvailable){
            //flashFlag = false;


            try {
                cameraId = cameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                    e.printStackTrace();
            }
            try {
                t.interrupt();
            } catch (NullPointerException e){

            }
            t = new Thread() {
                public void run() {
                    String myString = "101010";
                    long blinkDelay = 100;//Delay in ms
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            for (int i = 0; i < myString.length(); i++) {
                                try {
                                    if (myString.charAt(i) == '0')
                                        cameraManager.setTorchMode(cameraId, false);
                                    else
                                        cameraManager.setTorchMode(cameraId, true);
                                    Thread.sleep(blinkDelay);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodError e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }

        return START_STICKY;
    }

    private void createNotificationChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "personal_channel";
            String description = "Charger Removed!!!!";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            //channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
