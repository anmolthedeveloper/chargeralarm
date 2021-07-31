package com.anmolthedeveloper.anmolsingh.chargeralarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Power extends BroadcastReceiver {
    TextView chargingStatus;
    TextView alarmStatus;
    MediaPlayer mediaPlayer;
    Boolean flag;
    Context context;
    Intent serviceIntent;
    static Power instance;
    ToggleButton toggleButton;
    public static Power getInstance(){
        return instance;
    }
    public Power(Context context, TextView chargingStatus, TextView alarmStatus, final ToggleButton toggleButton) {
        this.chargingStatus = chargingStatus;
        this.alarmStatus = alarmStatus;
        this.context = context;
        instance = this;
        mediaPlayer = MediaPlayer.create(context, R.raw.sirenone);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(100,100);
        serviceIntent = new Intent(context.getApplicationContext(), MyService.class);
        flag = false;
        this.toggleButton = toggleButton;
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOnAlarm();
                }
                else
                    turnOffAlarm();
            }
        });
    }

    public void turnOnAlarm(){
        flag = true;
        alarmStatus.setText("ON");
        alarmStatus.setTextColor(context.getResources().getColor(R.color.green));
    }

    public void turnOffAlarm(){
        flag = false;
        alarmStatus.setText("OFF");
        alarmStatus.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        toggleButton.setChecked(false);
        MainActivity.getInstance().stopAService();
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            //Toast.makeText(context, "The device is charging", Toast.LENGTH_SHORT).show();
            chargingStatus.setText("ON");
            chargingStatus.setTextColor(context.getResources().getColor(R.color.green));

        } else if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            //Toast.makeText(context, "The device is not charging", Toast.LENGTH_SHORT).show();
            chargingStatus.setText("OFF");
            chargingStatus.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            if (flag){
                //mediaPlayer.start();
                MainActivity.getInstance().startAService();
            }
        }
    }
}
