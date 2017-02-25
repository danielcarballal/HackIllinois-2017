package com.example.danielcarballal.virtualhideandseek;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.util.SparseArray;
import android.widget.TextView;
import android.view.Menu;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

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

                System.out.println("GAAAAAAAME ON");
            }
        });

    }

    private void registerDevice(String MACAddress){
        TextView tx = (TextView) findViewById(R.id.textView4);
        tx.setText(MACAddress);
    }

    private void startScan(){
        System.out.println("Start scan");
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
                System.out.println("Scan result: " + firstCon.length);
                System.out.println(firstCon);
                for (int i = 2; i < 5; i++) {
                    byte b = firstCon[i];
                    System.out.println(b);
                    if( b == magic_bytes[idx])
                        correct_bytes++;
                    idx++;
                }

                if(correct_bytes == 3){
                    registerDevice(device.getAddress());
                }
                else{
                    System.out.println("Other result found");
                    return;
                }

                super.onScanResult(callbackType, result);
            }
        };
        leScanner.startScan(sc);

        ScanCallback stp_scan = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                System.out.print("Ending");
                super.onScanResult(callbackType, result);
            }
        };
        long curTime = System.currentTimeMillis();
        
        leScanner.stopScan(stp_scan);

    }

}
