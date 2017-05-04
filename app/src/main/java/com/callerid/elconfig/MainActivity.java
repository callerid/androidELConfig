package com.callerid.elconfig;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements ServiceCallbacks {

    private String inString = "Waiting...";
    private UDPListen mService;
    private boolean mBound;

    // Scroller list
    private String[] lineCountEntries;

    // Buttons
    private Button btnC;
    private Button btnU;
    private Button btnD;
    private Button btnA;
    private Button btnS;
    private Button btnO;
    private Button btnB;
    private Button btnK;
    private Button btnGetToggles;

    // UDP variables
    int boxPort = 3520;

    public void gotUDP(String inString){

        // Reception of UDP string
        // Handle all data

        // Setup variables for use
        String myData = inString;

        String command;
        Integer myLine=0;
        String myType="";
        String myIndicator="";

        String myDuration="";
        String myCheckSum="";
        String myRings="";
        String myDateTime="";
        String myNumber="n/a";
        String myName="";

        // Check if matches a call record
        Pattern myPattern = Pattern.compile(".*(\\d\\d) ([IO]) ([ES]) (\\d{4}) ([GB]) (.)(\\d) (\\d\\d/\\d\\d \\d\\d:\\d\\d [AP]M) (.{8,15})(.*)");
        Matcher matcher = myPattern.matcher(myData);

        if(matcher.find()){

            myLine = Integer.parseInt(matcher.group(1));
            myType = matcher.group(2);

            if(myType.equals("I")||myType.equals("O")){

                myIndicator = matcher.group(3);

                myDuration = matcher.group(4);
                myCheckSum = matcher.group(5);
                myRings = matcher.group(6);
                myDateTime = matcher.group(8);
                myNumber = matcher.group(9);
                myName = matcher.group(10);

            }

        }

        // Check to see if call information is from a DETAILED record
        Pattern myPatternDetailed = Pattern.compile(".*(\\d\\d) ([NFR]) {13}(\\d\\d/\\d\\d \\d\\d:\\d\\d:\\d\\d)");
        Matcher matcherDetailed = myPatternDetailed.matcher(myData);

        if(matcherDetailed.find()){

            myLine = Integer.parseInt(matcherDetailed.group(1));
            myType = matcherDetailed.group(2);

            if(myType.equals("N")||myType.equals("F")||myType.equals("R")){
                myDateTime = matcherDetailed.group(3);
            }

        }

        // Check if comm data
        String recData = "n/a";
        String e = "n/a";
        String c = "n/a";
        String x = "n/a";
        String u = "n/a";
        String d = "n/a";
        String a = "n/a";
        String s = "n/a";
        String o = "n/a";
        String b = "n/a";
        String k = "n/a";
        String t = "n/a";
        String line = "n/a";
        String date = "n/a";

        Pattern myCommPattern = Pattern.compile("([Ee])([Cc])([Xx])([Uu])([Dd])([Aa])([Ss])([Oo])([Bb])([Kk])([Tt]) L=(\\d{1,2}) (\\d{1,2}/\\d{1,2} (\\d{1,2}:\\d{1,2}:\\d{1,2}))");
        Matcher matcherComm = myCommPattern.matcher(myData);

        if(matcherComm.find()){

            recData = matcherComm.group(0);
            e = matcherComm.group(1);
            c = matcherComm.group(2);
            x = matcherComm.group(3);
            u = matcherComm.group(4);
            d = matcherComm.group(5);
            a = matcherComm.group(6);
            s = matcherComm.group(7);
            o = matcherComm.group(8);
            b = matcherComm.group(9);
            k = matcherComm.group(10);
            t = matcherComm.group(11);
            line = matcherComm.group(12);
            date = matcherComm.group(13);

            // if code gets here then toggles are used
            // enable toggle buttons
            btnC.setEnabled(true);
            btnU.setEnabled(true);
            btnD.setEnabled(true);
            btnA.setEnabled(true);
            btnS.setEnabled(true);
            btnO.setEnabled(true);
            btnB.setEnabled(true);
            btnK.setEnabled(true);

            // Set all toggle colors
            int toggleIsSetBkgColor = Color.GREEN;
            int toggleIsNotSetBkgColor = Color.LTGRAY;
            int toggleIsSetTextColor = Color.BLACK;
            int toggleIsNotSetTextColor = Color.BLACK;

            // Set all toggles
            btnC.setText(c);
            if(c=="C"){
                btnC.setBackgroundColor(toggleIsNotSetBkgColor);
                btnC.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnC.setBackgroundColor(toggleIsSetBkgColor);
                btnC.setTextColor(toggleIsSetTextColor);
            }

            btnU.setText(u);
            if(u=="U"){
                btnU.setBackgroundColor(toggleIsNotSetBkgColor);
                btnU.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnU.setBackgroundColor(toggleIsSetBkgColor);
                btnU.setTextColor(toggleIsSetTextColor);
            }

            btnD.setText(d);
            if(d=="D"){
                btnD.setBackgroundColor(toggleIsNotSetBkgColor);
                btnD.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnD.setBackgroundColor(toggleIsSetBkgColor);
                btnD.setTextColor(toggleIsSetTextColor);
            }

            btnA.setText(a);
            if(a=="A"){
                btnA.setBackgroundColor(toggleIsNotSetBkgColor);
                btnA.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnA.setBackgroundColor(toggleIsSetBkgColor);
                btnA.setTextColor(toggleIsSetTextColor);
            }

            btnS.setText(s);
            if(s=="S"){
                btnS.setBackgroundColor(toggleIsNotSetBkgColor);
                btnS.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnS.setBackgroundColor(toggleIsSetBkgColor);
                btnS.setTextColor(toggleIsSetTextColor);
            }

            btnO.setText(o);
            if(o=="O"){
                btnO.setBackgroundColor(toggleIsNotSetBkgColor);
                btnO.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnO.setBackgroundColor(toggleIsSetBkgColor);
                btnO.setTextColor(toggleIsSetTextColor);
            }

            btnB.setText(b);
            if(b=="B"){
                btnB.setBackgroundColor(toggleIsNotSetBkgColor);
                btnB.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnB.setBackgroundColor(toggleIsSetBkgColor);
                btnB.setTextColor(toggleIsSetTextColor);
            }

            btnK.setText(k);
            if(k=="K"){
                btnK.setBackgroundColor(toggleIsNotSetBkgColor);
                btnK.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnK.setBackgroundColor(toggleIsSetBkgColor);
                btnK.setTextColor(toggleIsSetTextColor);
            }

        }

        // ------------- OUTPUT data on UI ------------------



    }

    private void toggleClick(Button btn){

        String preCmd = btn.getText().toString();
        String command = "^^Id-" + flipCase(preCmd);

        sendUDP(command,boxPort);

    }

    private void getToggles(){

        sendUDP("^^Id-V",boxPort);

    }

    private String flipCase(String s){

        String lower = s.toLowerCase();
        boolean isLower =  s.equals(lower);

        if(isLower){
            return s.toUpperCase();
        }
        else{
            return s.toLowerCase();
        }

    }

    private void sendUDP(String toSend, int port){

        final int sendPort = port;
        final String toSendMessage = toSend;

        new Thread() {
            @Override
            public void run() {

                try{

                    // Gather
                    int server_port = sendPort;
                    int msg_length= toSendMessage.length();
                    byte[] message = toSendMessage.getBytes();

                    // Prepare
                    DatagramSocket s = new DatagramSocket();
                    InetAddress local = InetAddress.getByName("255.255.255.255");
                    DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);

                    // Send
                    s.send(p);

                }
                catch (Exception e){
                    System.out.print("Exception: " + e.toString());
                }

            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set screen to stay on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Populate sprLineCount
        lineCountEntries = new String[]{"1","5","9","17","21","25","33"};
        Spinner sprLineCount = (Spinner)findViewById(R.id.sprLineCount);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,lineCountEntries);
        sprLineCount.setAdapter(adapter);

        // Buttons
        btnGetToggles = (Button)findViewById(R.id.btnGetToggles);

        // Button clicks
        btnGetToggles.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToggles();
            }
        });

        // Toggle Reference buttons
        btnC = (Button)findViewById(R.id.btnC);
        btnU = (Button)findViewById(R.id.btnU);
        btnD = (Button)findViewById(R.id.btnD);
        btnA = (Button)findViewById(R.id.btnA);
        btnS = (Button)findViewById(R.id.btnS);
        btnO = (Button)findViewById(R.id.btnO);
        btnB = (Button)findViewById(R.id.btnB);
        btnK = (Button)findViewById(R.id.btnK);

        // Toggle Button clicks
        btnC.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnC);
            }
        });
        btnU.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnU);
            }
        });
        btnD.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnD);
            }
        });
        btnA.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnA);
            }
        });
        btnS.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnS);
            }
        });
        btnO.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnO);
            }
        });
        btnB.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnB);
            }
        });
        btnK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClick(btnK);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.INTERNET},
                1);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // bind to Service
                    Intent intent = new Intent(this, UDPListen.class);
                    mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    boolean stop = false;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Setup connection/binder to service
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            UDPListen.LocalBinder binder = (UDPListen.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            mService.setCallbacks(MainActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }

    };

    // Link Display to Update so the UI gets updated through interface
    @Override
    public void display(String rString){

        inString = rString;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                gotUDP(inString);
            }
        });

    }

}
