package com.example.danielcarballal.virtualhideandseek;

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
    String time_shown = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        gl = GameLogic.getGameLogic();
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
        gl.startGame();

    }

    private void updateTime(){
        TextView tv = (TextView) findViewById(R.id.clock);
        time_elapsed = gl.startTime + 300000 - SystemClock.elapsedRealtime();
        tv.setText(time_elapsed / 60000 + " : " + (time_elapsed % 60000)/1000);
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
                /*
                System.out.println(time_elapsed);
                System.out.println(gl.startTime);
                System.out.println(SystemClock.elapsedRealtime());

                if(  gl.startTime + 300000 - (SystemClock.elapsedRealtime() + time_elapsed) < 1000 ){
                    //System.out.println("Entered here");

                    updateTime();
                }*/

                if(correct_bytes == 3 && gl.playerExists(device.getAddress())){
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
            return Color.rgb(255, 15, 15); // HUE 0
        }
        if(rsiiDistance > -70)
            return Color.rgb(244,137,14);// HUE 335
        if(rsiiDistance > -80){
            return Color.rgb(220,244,14); //HUE
        }
        return Color.rgb(14,240,244);
    }
    private void populateListView(){
        ArrayList<String> chasers = gl.getTopPlayerNames();
        ArrayList<Integer> distances = gl.getTopDistances();
        int i = 0;
        for(String chaser : chasers){
            int distance = (int) distances.get(i);
            TextView tv = (TextView) findViewById(R.id.name1 + i);
            FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) tv.getLayoutParams();
            lParams.topMargin = 200*i;
            tv.setLayoutParams(lParams);
            tv.setText(gl.getPlayer(chaser) + " " + distance);
            tv.setBackgroundColor(getColorFromDist(distance));
            i++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
