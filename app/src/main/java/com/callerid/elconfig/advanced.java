package com.callerid.elconfig;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class advanced extends AppCompatActivity {

    // Advanced variables
    private static String UNIT_NUMBER;
    private static String UNIT_IP;
    private static String UNIT_MAC;
    private static String DEST_IP;
    private static String DEST_PORT;
    private static String DEST_MAC;

    private EditText tbUnitNumber;
    private EditText tbUnitIP;
    private EditText tbUnitMac;
    private EditText tbDestIP;
    private EditText tbDestMac;
    private EditText tbDestPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set screen to stay on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Force Landscape
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Advanced variables
        UNIT_NUMBER = getIntent().getStringExtra("unit_number");
        UNIT_IP = getIntent().getStringExtra("unit_ip");
        UNIT_MAC = getIntent().getStringExtra("unit_mac");
        DEST_IP = getIntent().getStringExtra("dest_ip");
        DEST_PORT = getIntent().getStringExtra("dest_mac");
        DEST_MAC = getIntent().getStringExtra("dest_port");

        // Refs
        tbUnitNumber = (EditText)findViewById(R.id.tbUnitNumber);
        tbDestIP = (EditText)findViewById(R.id.tbDestIP);
        tbDestMac = (EditText)findViewById(R.id.tbDestMac);
        tbDestPort= (EditText)findViewById(R.id.tbDestPort);

        // Put vars into fields
        tbUnitNumber.setText(UNIT_NUMBER);
        tbUnitIP.setText(UNIT_IP);
        tbDestIP.setText(DEST_IP);
        tbDestMac.setText(DEST_MAC);
        tbDestPort.setText(DEST_PORT);


    }

}
