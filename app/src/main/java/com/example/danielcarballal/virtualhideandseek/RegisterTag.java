package com.example.danielcarballal.virtualhideandseek;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by danielcarballal on 25-2-17.
 */

public class RegisterTag extends AppCompatActivity {
    byte[] magic_bytes = {(byte)0xBA, (byte)0x6E, (byte)0x15};
    BluetoothLeScanner leScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        final Button fab = (Button) findViewById(R.id.scan2);
        final Button fab2 = (Button) findViewById(R.id.scan3);

       if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
           ActivityCompat.requestPermissions(this,
                   new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter ba = bm.getAdapter();
       leScanner = ba.getBluetoothLeScanner();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startScan();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tx = (TextView) findViewById(R.id.textView4);
                EditText editText = (EditText) findViewById(R.id.editText2);
                String mac_add =  (String) tx.getText();
                String user_name = editText.getText().toString();
                if(mac_add.equals("Did not find device") || mac_add.equals("") || user_name.equals("User name")){
                    warning();
                } else{
                    if(!GameLogic.getGameLogic().addPlayer(user_name, mac_add)){
                        mac_in_use_warning(GameLogic.getGameLogic().getPlayer(mac_add));
                    } else {
                        success_added(user_name);
                    };
                }

            }
        });

        Button back = (Button) findViewById(R.id.back);
        back.getBackground().setColorFilter(0x0000FF00, PorterDuff.Mode.MULTIPLY);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDashboard();
            }
        });

    }

    private void goToDashboard(){
        Intent i = new Intent(this, Dashboard.class);
        startActivity(i);
    }

    protected void warning(){
        new AlertDialog.Builder(this).setTitle("No name or device set")
                .setMessage("Please make sure you find a device and set your name")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                }).show();
    }

    protected void mac_in_use_warning(String name){
        new AlertDialog.Builder(this).setTitle("Device already being used!")
                .setMessage("The device you tried to connect is by " + name)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                }).show();
    }

    protected void success_added(String name){
        new AlertDialog.Builder(this).setTitle("Successfully added device!")
                .setMessage("Successfully added the user " + name)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                }).show();
    }

    private void registerDevice(String MACAddress){
        TextView tx = (TextView) findViewById(R.id.textView4);
        tx.setText(MACAddress);
    }

    private void startScan(){
        ScanCallback sc = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                ScanRecord record = result.getScanRecord();
                SparseArray<byte[]> dat = record.getManufacturerSpecificData(); // MAGIC NUM

                int correct_bytes = 0;
                byte[] firstCon = record.getBytes();
                int idx = 0;
                for (int i = 2; i < 5; i++) {
                    byte b = firstCon[i];
                    if( b == magic_bytes[idx])
                        correct_bytes++;
                    idx++;
                }

                if(correct_bytes == 3 && GameLogic.getGameLogic().getPlayer(device.getAddress()) == null){
                    registerDevice(device.getAddress());
                    stopScan();
                }
                else{
                    return;
                }

                super.onScanResult(callbackType, result);
            }
        };
        leScanner.startScan(sc);
    }

    private void stopScan(){
        ScanCallback sc = new ScanCallback() {

        };
        leScanner.stopScan(sc);
    }

}
