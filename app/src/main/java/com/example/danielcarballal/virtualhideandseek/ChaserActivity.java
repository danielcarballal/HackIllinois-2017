package com.example.danielcarballal.virtualhideandseek;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.bluetooth.le.AdvertiseSettings.*;

/**
 * Created by danielcarballal on 25-2-17.
 */

public class ChaserActivity extends AppCompatActivity {
    byte[] magic_bytes = {(byte)0xBA, (byte)0x6E, (byte)0x15};
    byte[] magic_bytes_send = {(byte)0xC0, (byte)0xFF, (byte)0xEE};
    BluetoothLeScanner leScanner;
    BluetoothLeAdvertiser leAdvertiser;
    GameLogic gl;
    long time_elapsed = 300000;
    Timer stopWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        gl = GameLogic.getGameLogic();
        gl.startTimer();
        gl.addPlayer("JAMES THE TURTLE", "abc");
        gl.addPlayer("CARL THE HEDGEHOG", "bca");
        gl.addPlayer("CHUCKY THE CHEETAH", "arc");
        gl.addPlayer("EDDIE THE NARWHAL", "etn");
        gl.addLocation("etn", -77);
        gl.addLocation("bca", -51);
        gl.addLocation("arc", -67);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chaser_layout);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        this.populateListView();
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter ba = bm.getAdapter();
        leScanner = ba.getBluetoothLeScanner();
        leAdvertiser = ba.getBluetoothLeAdvertiser();

        int MANUFACTURE_ID = 14;
        int AVMODE = ADVERTISE_MODE_LOW_LATENCY;
        boolean CONNECT = false;
        int TIMEOUT = 10000;
        int TX_POWER = ADVERTISE_TX_POWER_HIGH;

        AdvertiseData.Builder ADbuild = new AdvertiseData.Builder().addManufacturerData(MANUFACTURE_ID, magic_bytes_send);
        AdvertiseData AD = ADbuild.build();
        AdvertiseSettings.Builder ASbuild = new AdvertiseSettings.Builder().setAdvertiseMode(AVMODE).setConnectable(CONNECT).setTimeout(TIMEOUT).setTxPowerLevel(TX_POWER);
        AdvertiseSettings AS = ASbuild.build();
        AdvertiseCallback AC = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }
        };

        leAdvertiser.startAdvertising(AS, AD, AC);
        startScan();
        Timer time = new Timer();

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread( new Runnable() {
                    @Override
                    public void run(){
                        updateTime();
                    }
                });
            }
        }, 0, 1000);

        stopWatch = new Timer();
        stopWatch.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread( new Runnable() {
                    @Override
                    public void run(){
                        hiderWins();
                    }
                });
            }
        }, 300000);
    }

    private void hiderWins(){
        new AlertDialog.Builder(this).setTitle("Successfully hid from the chasers!")
                .setMessage("Through your cunningness and ingenuity, you have escaped." )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goToDashboard();
                    }
                }).show();
    }

    private void hiderLoses(String name){
        new AlertDialog.Builder(this).setTitle("Got caught by " + name)
                .setMessage("You have failed to escape." )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goToDashboard();
                    }
                }).show();
    }

    private void goToDashboard(){
        Intent i = new Intent(this, Dashboard.class);
        startActivity(i);
    }

    private void updateTime(){
        TextView tv = (TextView) findViewById(R.id.clock);
        time_elapsed = gl.startTime + 300000 - SystemClock.elapsedRealtime();
        long minutes = (time_elapsed % 60000)/1000;
        tv.setText(time_elapsed / 60000 + ":" + (minutes >= 10 ? "" : "0") + minutes);
    }

    private void startScan(){
        ScanCallback sc = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                ScanRecord record = result.getScanRecord();

                int correct_bytes = 0;
                byte[] firstCon = record.getBytes();
                int idx = 0;
                for (int i = 2; i < 5; i++) {
                    byte b = firstCon[i];
                    if( b == magic_bytes[idx])
                        correct_bytes++;
                    idx++;
                }

                if(correct_bytes == 3 && gl.playerExists(device.getAddress())){
                    if(rssi > -50){
                        hiderLoses(gl.getPlayer(device.getAddress()));
                    }
                    if(getColorFromDist(gl.playerDists.get(device.getAddress())) != getColorFromDist(rssi)){
                        gl.addLocation(device.getAddress(), rssi);
                        populateListView();
                    } else{
                        gl.addLocation(device.getAddress(), rssi);
                    }

                }
                else{
                    return;
                }

                super.onScanResult(callbackType, result);

            }

        };
        leScanner.startScan(sc);
    }

    private static int getColorFromDist(int rsiiDistance){
        if(rsiiDistance > -60) {
            return Color.rgb(255, 0, 0); // HUE 0
        }
        if(rsiiDistance > -70)
           // return Color.rgb(255,205,152);// HUE 335
            return Color.rgb(255,0,150);
        if(rsiiDistance > -80){
            return Color.rgb(150,0,255); //HUE
        }
        return Color.rgb(0,0,255);
    }
    private void populateListView(){
        ArrayList<String> chasers = gl.getTopPlayerNames();
        ArrayList<Integer> distances = gl.getTopDistances();
        int i = 0;
        for(String chaser : chasers){
            int distance = (int) distances.get(i);
            TextView tv = (TextView) findViewById(R.id.name1 + i);
            FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) tv.getLayoutParams();
            lParams.topMargin = 270 + 200*i; //220 is the height of the clock
            tv.setLayoutParams(lParams);
            tv.setText(gl.getPlayer(chaser));
            tv.setBackgroundColor(getColorFromDist(distance));
            i++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
