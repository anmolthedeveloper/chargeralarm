package com.anmolthedeveloper.anmolsingh.chargeralarm;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {

    CheckBox checkBoxVibration;
    CheckBox checkBoxTorch;
    SharedPreferences mSharedPreferences;
    Boolean vibratorFlag;
    Boolean flashFlag;
    SharedPreferences.Editor mEditor;
    MediaPlayer mediaPlayer;
    ToggleButton toggleButton,t1,t2,t3;
    RadioButton r1,r2,r3;
    String currSiren;
    boolean isFlashAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Settings");
        checkBoxVibration = findViewById(R.id.checkBoxVibration);
        checkBoxTorch = findViewById(R.id.checkBoxTorch);
        mSharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        currSiren = mSharedPreferences.getString(MainActivity.Siren, "sirenone");
        checkBoxVibration.setChecked(mSharedPreferences.getBoolean(MainActivity.Vibration, false));
        checkBoxTorch.setChecked(mSharedPreferences.getBoolean(MainActivity.Torch, false));
        t1 = findViewById(R.id.toggleButton);
        t2 = findViewById(R.id.toggleButton1);
        t3 = findViewById(R.id.toggleButton2);
        r1 = findViewById(R.id.siren_one);
        r2 = findViewById(R.id.siren_two);
        r3 = findViewById(R.id.siren_three);

        isFlashAvailable = getApplication().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        switch (currSiren){
            case "sirenone":
                r1.setChecked(true);
                break;
            case "sirentwo":
                r2.setChecked(true);
                break;
            case "sirenthree":
                r3.setChecked(true);
                break;
        }

        checkBoxVibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxVibration.isChecked())
                    vibratorFlag = true;
                else
                    vibratorFlag = false;
                mEditor.putBoolean(MainActivity.Vibration, vibratorFlag);
                mEditor.apply();
            }
        });

        if (isFlashAvailable) {
            checkBoxTorch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBoxTorch.isChecked())
                        flashFlag = true;
                    else
                        flashFlag = false;
                    mEditor.putBoolean(MainActivity.Torch, flashFlag);
                    mEditor.apply();
                }
            });
        } else {
            flashFlag = false;
            mEditor.putBoolean(MainActivity.Torch, flashFlag);
            mEditor.apply();
            checkBoxTorch.setChecked(false);
            checkBoxTorch.setPaintFlags(checkBoxTorch.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            checkBoxTorch.setEnabled(false);
        }
    }

    public void playSiren(View view){
        toggleButton = findViewById(view.getId());
        changeStateToggle(view);
        if (toggleButton.isChecked()){

            if (mediaPlayer!=null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer=null;
            }

            if (view.getId()==R.id.toggleButton)
                mediaPlayer = MediaPlayer.create(this, R.raw.sirenone);
            else if(view.getId()==R.id.toggleButton1)
                mediaPlayer = MediaPlayer.create(this, R.raw.sirentwo);
            else
                mediaPlayer = MediaPlayer.create(this, R.raw.sirenthree);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void changeStateToggle(View view){
        switch (view.getId()){
            case R.id.toggleButton:
                t2.setChecked(false);
                t3.setChecked(false);
                break;
            case R.id.toggleButton1:
                t1.setChecked(false);
                t3.setChecked(false);
                break;
            case R.id.toggleButton2:
                t1.setChecked(false);
                t2.setChecked(false);
                break;
        }
    }

    public void onClickRadio(View view){
        boolean checked = ((RadioButton) view).isChecked();
        if (checked) {
            switch (view.getId()) {
                case R.id.siren_one:
                    mEditor.putString(MainActivity.Siren, "sirenone");
                    break;
                case R.id.siren_two:
                    mEditor.putString(MainActivity.Siren, "sirentwo");
                    break;
                case R.id.siren_three:
                    mEditor.putString(MainActivity.Siren, "sirenthree");
                    break;
            }
            mEditor.apply();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
}
