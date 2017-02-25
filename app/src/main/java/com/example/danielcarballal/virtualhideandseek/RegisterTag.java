package com.example.danielcarballal.virtualhideandseek;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.util.SparseArray;
import android.widget.TextView;
import android.view.Menu;

/**
 * Created by danielcarballal on 25-2-17.
 */

public class RegisterTag extends AppCompatActivity {
    byte[] magic_bytes = {(byte)0xAB, (byte)0x6E, (byte)0x15};
    BluetoothLeScanner leScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Creating register tag");
        Button fab = (Button) findViewById(R.id.search);

        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter ba = bm.getAdapter();
        leScanner = ba.getBluetoothLeScanner();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
    }

    private void registerDevice(ScanResult r){
        TextView tx = (TextView) findViewById(R.id.textView4);
        tx.append(r.getScanRecord().getManufacturerSpecificData().get(0).toString());

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
                byte[] firstCon = dat.get(0);
                int idx = 0;
                for (byte b : firstCon) {
                    System.out.println(b);
                    if( b == magic_bytes[idx])
                        correct_bytes++;
                    idx++;
                }

                if(correct_bytes == 3 && rssi > -50){
                    registerDevice(result);
                }
                else{
                    System.out.println("Other result found");
                    return;
                }

                super.onScanResult(callbackType, result);
            }
        };
        leScanner.startScan(sc);
    }

}
