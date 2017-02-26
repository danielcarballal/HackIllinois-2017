package com.example.danielcarballal.virtualhideandseek;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class Dashboard extends AppCompatActivity {
    BluetoothLeScanner leScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button register = (Button) findViewById(R.id.register);
        Button startgame = (Button) findViewById(R.id.startgame);

        startgame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToChaserActivity();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegisterTag();
            }
        });



    }

    private void goToRegisterTag(){
        Intent i = new Intent(this, RegisterTag.class);
        startActivity(i);
    }

    private void goToChaserActivity(){
        Intent i = new Intent(this, ChaserActivity.class);
        startActivity(i);
    }


}
