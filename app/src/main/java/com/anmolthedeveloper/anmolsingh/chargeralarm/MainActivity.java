package com.anmolthedeveloper.anmolsingh.chargeralarm;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView chargingStatus;
    TextView alarmStatus;
    ToggleButton toggleButton;
    static MainActivity instance;
    public static final String MyPREFERENCES = "features" ;
    public static final String Vibration = "vibration";
    public static final String Torch = "torch";
    public static final String Siren = "siren";
    SharedPreferences mSharedPreferences;
    Editor mEditor;
    public static MainActivity getInstance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        chargingStatus = findViewById(R.id.chargingStatus);
        alarmStatus = findViewById(R.id.alarmStatus);
        toggleButton = findViewById(R.id.onOffSwitch);

        mSharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        if (!mSharedPreferences.contains(MainActivity.Vibration)) {
            mEditor.putBoolean(MainActivity.Vibration, true);
            mEditor.putBoolean(MainActivity.Torch, true);
            mEditor.putString(MainActivity.Siren, "sirenone");
            mEditor.apply();
        }
        BroadcastReceiver receiver = new Power(this, chargingStatus, alarmStatus, toggleButton);
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Intent.ACTION_POWER_CONNECTED);
        ifilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, ifilter);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do not remove the app from background, " +
                "In order to use the app you can just turn it on and " +
                "leave it runnning in the background")
                .setTitle("IMPORTANT")
                .setIcon(R.drawable.ic_warning_black_24dp);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if(isCharging) {
            chargingStatus.setText("ON");
            chargingStatus.setTextColor(getResources().getColor(R.color.green));
        }
        else
            chargingStatus.setText("OFF");
    }

    public void startAService(){
        startService(new Intent(this, MyService.class));
        mEditor.putBoolean("startService", true);
    }

    public void stopAService(){
        stopService(new Intent(this, MyService.class));
        mEditor.putBoolean("startService", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.feedback:
                String[] ar = {"techguyanmol@gmail.com"};
                shareToGMail(ar,"Charger Alarm : [FEEDBACK]");
                break;
            case R.id.about_developer:
//                Long way of opening a URL
//                Uri webpage = Uri.parse("http://anmolthedeveloper.com/about-me");
//                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
//                startActivity(intent);
//              Shortcut for starting a new activity
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://anmolthedeveloper.com/about-me")));
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAService();
    }

    public void shareToGMail(String[] email, String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType("text/plain");
        //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for(final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        if (emailIntent.resolveActivity(getPackageManager())!=null)
            startActivity(emailIntent);
        else
            Toast.makeText(this, "Gmail app not found!", Toast.LENGTH_SHORT).show();
    }
}
