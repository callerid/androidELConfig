package com.callerid.elconfig;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class advanced extends Activity{

    // Advanced variables
    private static String UNIT_NUMBER;
    private static String UNIT_IP;
    private static String UNIT_MAC;
    private static String DEST_IP;
    private static String DEST_PORT;
    private static String DEST_MAC;

    private static String techCode;

    // Buttons
    private Button btnBack;
    private Button btnClearRLog;

    // Tables
    public static TableLayout tableRLog;

    // Scroll views
    public static ScrollView svRLog;

    // Fields
    private EditText tbUnitNumber;
    private EditText tbUnitIP;
    private EditText tbUnitMac;
    private EditText tbDestIP;
    private EditText tbDestMac;
    private EditText tbDestPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);

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
        techCode = getIntent().getStringExtra("tech_code");

        // Refs
        tbUnitNumber = (EditText)findViewById(R.id.tbUnitNum);
        tbDestIP = (EditText)findViewById(R.id.tbDestinationIP);
        tbDestMac = (EditText)findViewById(R.id.tbDestinationMac);
        tbDestPort= (EditText)findViewById(R.id.tbDestinationPort);
        btnBack = (Button)findViewById(R.id.btnBackToSimple);
        tableRLog = (TableLayout)findViewById(R.id.tableRawLog);
        svRLog = (ScrollView)findViewById(R.id.svRawLog);
        btnClearRLog = (Button)findViewById(R.id.btnClearRawLog);

        // Put vars into fields
        tbUnitNumber.setText(UNIT_NUMBER);
        tbDestIP.setText(DEST_IP);
        tbDestMac.setText(DEST_MAC);
        tbDestPort.setText(DEST_PORT);

        // Clicks
        btnBack.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBackClicked(v);
            }
        });

        btnClearRLog.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add row to call log table
                tableRLog.removeAllViews();
            }
        });

    }

    private void btnBackClicked(View view){

        Intent act2 = new Intent(view.getContext(), MainActivity.class);
        act2.putExtra("tech_code",techCode);
        startActivity(act2);

    }

}
