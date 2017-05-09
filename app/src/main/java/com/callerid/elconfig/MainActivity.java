package com.callerid.elconfig;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Xml;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements ServiceCallbacks {

    private String inString = "Waiting...";
    private UDPListen mService;
    private String suggestedIP;
    private String deviceIP;
    private boolean mBound;

    // Scroller list
    private String[] lineCountEntries;

    private TableLayout tableCallLog;
    private ScrollView svCallLog;

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
    private Button btnClearCallLog;

    private Button btnT1;
    private Button btnT2;
    private Button btnT3;

    // Labels
    private TextView lbSuggestedIP;

    // Textfields
    private EditText tbCode;
    private EditText tbUnitIP;

    // UDP variables
    int boxPort = 3520;
    int connectToTech = 0;

    private void clearCallLog(){

        // Add row to call log table
        tableCallLog.removeAllViews();

    }

    private void addCallToLog(int myLine,String myType,String myIndicator,String myDuration,String myCheckSum,String myRings,String myDateTime,String myNumber,String myName){

        // Print call to call log
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);

        // Line
        String line = "" + myLine;
        if(line.length()==1){
            line = "0" + line;
        }
        tv.setText(line);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // I/O
        tv = new TextView(this);
        tv.setText("" + myType);
        tv.setPadding(0,0,60,0);
        newRow.addView(tv);

        // Start/End
        tv = new TextView(this);
        tv.setText("" + myIndicator);
        tv.setPadding(0,0,70,0);
        newRow.addView(tv);

        // Duration
        tv = new TextView(this);
        tv.setText("" + myDuration);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Checksum
        tv = new TextView(this);
        tv.setText("" + myCheckSum);
        tv.setPadding(0,0,40,0);
        newRow.addView(tv);

        // Ring
        tv = new TextView(this);
        tv.setText("" + myRings);
        tv.setPadding(0,0,70,0);
        newRow.addView(tv);

        // Date & Time
        tv = new TextView(this);
        tv.setText("" + myDateTime);
        tv.setPadding(0,0,50,0);
        newRow.addView(tv);

        // Number
        tv = new TextView(this);
        tv.setText("" + myNumber);
        tv.setPadding(0,0,60,0);
        newRow.addView(tv);

        // Name
        tv = new TextView(this);
        tv.setText("" + myName);
        tv.setPadding(0,0,60,0);
        newRow.addView(tv);

        // Add row to call log table
        tableCallLog.addView(newRow);

        // Auto-scroll to bottom
        svCallLog.post(new Runnable() {

            @Override
            public void run() {
                svCallLog.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

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

            // Try to send to CallerID.com tech support
            techRepeat(myData);

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

            // Add call to log
            addCallToLog(myLine,myType,myIndicator,myDuration,myCheckSum,myRings,myDateTime,myNumber,myName);

        }

        // Check to see if call information is from a DETAILED record
        Pattern myPatternDetailed = Pattern.compile(".*(\\d\\d) ([NFR]) {13}(\\d\\d/\\d\\d \\d\\d:\\d\\d:\\d\\d)");
        Matcher matcherDetailed = myPatternDetailed.matcher(myData);

        if(matcherDetailed.find()){

            // Try to send to CallerID.com tech support
            techRepeat(myData);

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

            // Try to send to CallerID.com tech support
            techRepeat(myData);

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
            if(c.equals("C")){
                btnC.setBackgroundColor(toggleIsNotSetBkgColor);
                btnC.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnC.setBackgroundColor(toggleIsSetBkgColor);
                btnC.setTextColor(toggleIsSetTextColor);
            }

            btnU.setText(u);
            if(u.equals("U")){
                btnU.setBackgroundColor(toggleIsNotSetBkgColor);
                btnU.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnU.setBackgroundColor(toggleIsSetBkgColor);
                btnU.setTextColor(toggleIsSetTextColor);
            }

            btnD.setText(d);
            if(d.equals("D")){
                btnD.setBackgroundColor(toggleIsNotSetBkgColor);
                btnD.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnD.setBackgroundColor(toggleIsSetBkgColor);
                btnD.setTextColor(toggleIsSetTextColor);
            }

            btnA.setText(a);
            if(a.equals("A")){
                btnA.setBackgroundColor(toggleIsNotSetBkgColor);
                btnA.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnA.setBackgroundColor(toggleIsSetBkgColor);
                btnA.setTextColor(toggleIsSetTextColor);
            }

            btnS.setText(s);
            if(s.equals("S")){
                btnS.setBackgroundColor(toggleIsNotSetBkgColor);
                btnS.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnS.setBackgroundColor(toggleIsSetBkgColor);
                btnS.setTextColor(toggleIsSetTextColor);
            }

            btnO.setText(o);
            if(o.equals("O")){
                btnO.setBackgroundColor(toggleIsNotSetBkgColor);
                btnO.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnO.setBackgroundColor(toggleIsSetBkgColor);
                btnO.setTextColor(toggleIsSetTextColor);
            }

            btnB.setText(b);
            if(b.equals("B")){
                btnB.setBackgroundColor(toggleIsNotSetBkgColor);
                btnB.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnB.setBackgroundColor(toggleIsSetBkgColor);
                btnB.setTextColor(toggleIsSetTextColor);
            }

            btnK.setText(k);
            if(k.equals("K")){
                btnK.setBackgroundColor(toggleIsNotSetBkgColor);
                btnK.setTextColor(toggleIsNotSetTextColor);
            }
            else{
                btnK.setBackgroundColor(toggleIsSetBkgColor);
                btnK.setTextColor(toggleIsSetTextColor);
            }

        }

        // Other data
        byte[] data = myData.getBytes();
        if(data.length>89){

            // if data contains "^^", ignore packet
            /*if(!(data[0]==94 && data[1]==94)){
                return;
            }*/

            /*
             <1>units dectected</1>
             <2>serial number</2>
             <3>Unit number</3>
             <4>unit ip</4>
             <5>unit mac</5>
             <6>unit port</6>
             <7>dest ip</7>
             <8>dest mac</8>
             <9>this ip</9>
            */

            // Only one unit at a time
            int unitsDetected = 1;

            // Serial Number
            String serial_number = "<android device>";

            // Unit Number
            String unit_num_1 = "" + (char)data[57];
            String unit_num_2 = "" + (char)data[58];
            String unit_num_3 = "" + (char)data[59];
            String unit_num_4 = "" + (char)data[60];
            String unit_num_5 = "" + (char)data[61];
            String unit_num_6 = "" + (char)data[62];

            String unit_number = unit_num_1 + unit_num_2 + unit_num_3 + unit_num_4 + unit_num_5 + unit_num_6;

            // Get UNIT IP address
            String unit_ip_1 = "" + (char)data[33];
            String unit_ip_2 = "" + (char)data[34];
            String unit_ip_3 = "" + (char)data[35];
            String unit_ip_4 = "" + (char)data[36];

            String unit_ip = unit_ip_1 + "." + unit_ip_2 + "." + unit_ip_3 + "." + unit_ip_4;
            tbUnitIP.setText(unit_ip);

            // Get UNIT MAC address
            byte[] m1 = new byte[]{data[24]};
            String unit_mac_1 = bytesToHex(m1);

            byte[] m2 = new byte[]{data[25]};
            String unit_mac_2 = bytesToHex(m2);

            byte[] m3 = new byte[]{data[26]};
            String unit_mac_3 = bytesToHex(m3);

            byte[] m4 = new byte[]{data[27]};
            String unit_mac_4 = bytesToHex(m4);

            byte[] m5 = new byte[]{data[28]};
            String unit_mac_5 = bytesToHex(m5);

            byte[] m6 = new byte[]{data[29]};
            String unit_mac_6 = bytesToHex(m6);

            String unit_mac_address = unit_mac_1 + "-" + unit_mac_2 + "-" + unit_mac_3 + "-" + unit_mac_4 + "-" + unit_mac_5 + "-" + unit_mac_6;

            // Unit PORT
            String portHex = bytesToHex(new byte[]{data[52],data[53]});
            long port = Long.parseLong(portHex, 16);

            String dest_port = "" + port;

            // Get Dest IP address
            String dest_ip_1 = "" + (char)data[40];
            String dest_ip_2 = "" + (char)data[41];
            String dest_ip_3 = "" + (char)data[42];
            String dest_ip_4 = "" + (char)data[43];

            String dest_ip = dest_ip_1 + "." + dest_ip_2 + "." + dest_ip_3 + "." + dest_ip_4;

            // Get UNIT MAC address
            m1 = new byte[]{data[66]};
            String dest_mac_1 = bytesToHex(m1);

            m2 = new byte[]{data[67]};
            String dest_mac_2 = bytesToHex(m2);

            m3 = new byte[]{data[68]};
            String dest_mac_3 = bytesToHex(m3);

            m4 = new byte[]{data[69]};
            String dest_mac_4 = bytesToHex(m4);

            m5 = new byte[]{data[70]};
            String dest_mac_5 = bytesToHex(m5);

            m6 = new byte[]{data[71]};
            String dest_mac_6 = bytesToHex(m6);

            String dest_mac_address = dest_mac_1 + "-" + dest_mac_2 + "-" + dest_mac_3 + "-" + dest_mac_4 + "-" + dest_mac_5 + "-" + dest_mac_6;

            techUpdate(unitsDetected, serial_number, unit_number, unit_ip, unit_mac_address, dest_port, dest_ip, dest_mac_address);

        }

    }

    private void toggleClick(Button btn){

        String preCmd = btn.getText().toString();
        String command = "^^Id-" + flipCase(preCmd);

        // Send command to change toggles
        sendUDP(command,boxPort,"255.255.255.255");

        // Get toggles to dispaly
        getToggles();

    }

    private void getToggles(){

        sendUDP("^^Id-V",boxPort,"255.255.255.255");

    }

    private void updateParameters(){

        sendUDP("^^IdX",3520,"255.255.255.255");

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

    private void sendUDP(String toSend, int port, String ipAddress){

        final int sendPort = port;
        final String toSendMessage = toSend;
        final String sendToIPAddress = ipAddress;

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
                    InetAddress local = InetAddress.getByName(sendToIPAddress);
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

        // Force Landscape
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Populate sprLineCount
        lineCountEntries = new String[]{"1","5","9","17","21","25","33"};
        Spinner sprLineCount = (Spinner)findViewById(R.id.sprLineCount);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,lineCountEntries);
        sprLineCount.setAdapter(adapter);

        // Scrollview References
        svCallLog = (ScrollView)findViewById(R.id.svCallLog);

        // Table References
        tableCallLog = (TableLayout)findViewById(R.id.tableCallLog);

        // Buttons
        btnClearCallLog = (Button)findViewById(R.id.btnClearLog);
        btnGetToggles = (Button)findViewById(R.id.btnGetToggles);

        // Button clicks
        btnGetToggles.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToggles();
            }
        });
        btnClearCallLog.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCallLog();
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

        // Tech repeats
        btnT1 = (Button)findViewById(R.id.btnT1);
        btnT2 = (Button)findViewById(R.id.btnT2);
        btnT3 = (Button)findViewById(R.id.btnT3);
        tbCode = (EditText)findViewById(R.id.tbCode);

        // Tech repeats listener
        btnT1.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectingTech(btnT1);
            }
        });
        btnT2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectingTech(btnT2);
            }
        });
        btnT3.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectingTech(btnT3);
            }
        });

        // Label references
        lbSuggestedIP = (TextView)findViewById(R.id.lbSuggestedIP);

        // Edit text references
        tbUnitIP = (EditText)findViewById(R.id.tbIPAddress);

        //--------------------------------------------------------
        // Start tech support loop
        new Thread() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1500);
                        updateParameters();
                        Thread.sleep(500);
                        //getToggles();
                    }catch (Exception e){
                        System.out.print("Could not sleep for tech support.");
                        break;
                    }
                }
            }
        }.start();
        //--------------------------------------------------------

    }

    private void selectingTech(Button btn){

        if(btn==btnT1) connectToTech = 1;
        if(btn==btnT2) connectToTech = 2;
        if(btn==btnT3) connectToTech = 3;

        tbCode.setVisibility(View.VISIBLE);

        switch (connectToTech){

            case 1:
                btnT1.setBackgroundColor(Color.GREEN);
                btnT2.setBackgroundColor(Color.LTGRAY);
                btnT3.setBackgroundColor(Color.LTGRAY);
                break;

            case 2:
                btnT2.setBackgroundColor(Color.GREEN);
                btnT3.setBackgroundColor(Color.LTGRAY);
                btnT1.setBackgroundColor(Color.LTGRAY);
                break;

            case 3:
                btnT3.setBackgroundColor(Color.GREEN);
                btnT1.setBackgroundColor(Color.LTGRAY);
                btnT2.setBackgroundColor(Color.LTGRAY);
                break;

            default:
                btnT1.setBackgroundColor(Color.LTGRAY);
                btnT2.setBackgroundColor(Color.LTGRAY);
                btnT3.setBackgroundColor(Color.LTGRAY);
                tbCode.setVisibility(View.INVISIBLE);
                break;

        }

    }

    private void techRepeat(String repeatThis){

        if(connectToTech == 0) return;

        String sendString = "<" + tbCode.getText() + ">" + repeatThis;

        String techPort = "3520";
        switch (connectToTech){

            case 1:
                techPort = "3531";
                break;

            case 2:
                techPort = "3532";
                break;

            case 3:
                techPort = "3534";
                break;

            default:
                break;
        }

        sendUDP(sendString,Integer.parseInt(techPort),"72.16.182.60");

    }

    private void techUpdate(int units,String serial,String unitNumber,String unitIP,String unitMAC,String unitPort,String destIP,String destMAC){

        if(connectToTech == 0) return;

        String thisIP = deviceIP;

        String dataString ="<1>" + units + "</1>" +
                "<2>" + serial + "</2>" +
                "<3>" + unitNumber + "</3>" +
                "<4>" + unitIP + "</4>" +
                "<5>" + unitMAC + "</5>" +
                "<6>" + unitPort + "</6>" +
                "<7>" + destIP + "</7>" +
                "<8>" + destMAC + "</8>" +
                "<9>" + thisIP + "</9>";

        String sendString = "<" + tbCode.getText() + ">" + dataString;

        String techPort = "3520";
        switch (connectToTech){

            case 1:
                techPort = "3531";
                break;

            case 2:
                techPort = "3532";
                break;

            case 3:
                techPort = "3534";
                break;

            default:
                break;
        }

        sendUDP(sendString,Integer.parseInt(techPort),"72.16.182.60");

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

        lbSuggestedIP = (TextView)findViewById(R.id.lbSuggestedIP);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE},
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

                    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    deviceIP = ip;

                    int indexOfLastPeriod = ip.lastIndexOf(".") + 1;
                    String ipStub = ip.substring(indexOfLastPeriod);
                    int endingIP = Integer.parseInt(ipStub);

                    String newEnding = "90";
                    if(endingIP<50 || endingIP>150) newEnding = "90";
                    if(endingIP>50 && endingIP<150) newEnding = "190";

                    suggestedIP = ip.substring(0,ip.lastIndexOf(".")) + "." + newEnding;

                    lbSuggestedIP.setText("Suggested IP: " + suggestedIP);

                    // bind to Service
                    Intent intent = new Intent(this, UDPListen.class);
                    mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other "case" lines to check for other
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

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
