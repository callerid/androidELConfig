package com.callerid.elconfig;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements ServiceCallbacks {

    private String inString = "Waiting...";
    private boolean gotToggles = false;
    private Map<String, Integer> previousReceptions;
    public static UDPListen mService;
    private String suggestedIP;
    private String deviceIP;
    private boolean mBound;
    AlertDialog.Builder dlgAlert;

    // Advanced variables
    private static String UNIT_NUMBER = "";
    private static String UNIT_IP = "";
    private static String UNIT_MAC = "";
    private static String DEST_IP = "";
    private static String DEST_PORT = "";
    private static String DEST_MAC = "";
    private static String UNIT_TIME = "";

    // Scroller list
    private String[] lineCountEntries;
    private ArrayAdapter<String> sprLineCntAdapter;
    private String[] dupCountEntries;
    private ArrayAdapter<String> sprDupCntAdapter;

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

    private Spinner sprLnCnt;
    private Spinner sprDupCnt;

    private Button btnGetToggles;
    private Button btnClearCallLog;
    private Button btnAdvanced;
    private Button btnSetSuggestedIP;
    private Button btnSetIP;
    private TextView lbSuggestedWarning;

    private Button btnT1;
    private Button btnT2;
    private Button btnT3;

    // Textfields
    private EditText tbTechCode;
    private EditText tbUnitIP;

    // UDP variables
    public static int boxPort;
    int connectToTech = 0;
    boolean lineCntLoaded = false;
    boolean dupCntLoaded = false;

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
        tv.setText(line.trim());
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // I/O
        tv = new TextView(this);
        tv.setText("" + myType);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Start/End
        tv = new TextView(this);
        tv.setText("" + myIndicator);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Duration
        tv = new TextView(this);
        tv.setText("" + myDuration);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Checksum
        tv = new TextView(this);
        tv.setText("" + myCheckSum);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Ring
        tv = new TextView(this);
        tv.setText("" + myRings);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Date & Time
        tv = new TextView(this);
        tv.setText("" + myDateTime);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Number
        tv = new TextView(this);
        tv.setText("" + myNumber);
        tv.setPadding(0,0,45,0);
        newRow.addView(tv);

        // Name
        tv = new TextView(this);
        tv.setText("" + myName);
        tv.setPadding(0,0,20,0);
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

    private void addCallToLog(int myLine,String myType, String myDateTime){

        // Print call to call log
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);

        // Line
        String line = "" + myLine;
        if(line.length()==1){
            line = "0" + line;
        }
        tv.setText(line.trim());
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // I/O
        tv = new TextView(this);
        tv.setText("" + myType);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Start/End
        tv = new TextView(this);
        tv.setText(" ");
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Duration
        tv = new TextView(this);
        tv.setText("    ");
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Checksum
        tv = new TextView(this);
        tv.setText(" ");
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Ring
        tv = new TextView(this);
        tv.setText(" ");
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Date & Time
        tv = new TextView(this);
        tv.setText("" + myDateTime);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Number
        tv = new TextView(this);
        tv.setText("              ");
        tv.setPadding(0,0,45,0);
        newRow.addView(tv);

        // Name
        tv = new TextView(this);
        tv.setText("              ");
        tv.setPadding(0,0,20,0);
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

    private void previousReceptions_timer_tick(){

        if(previousReceptions.size()<1)return;

        ArrayList<String> keysToRemove = new ArrayList<>();
        ArrayList<String> keysToIncrement = new ArrayList<>();

        for(String key : previousReceptions.keySet()){

            if(previousReceptions.get(key) > 4){
                keysToRemove.add(key);
            }
            else {
                keysToIncrement.add(key);
            }
        }

        for(String key : keysToIncrement){
            previousReceptions.put(key,previousReceptions.get(key)+1);
        }

        for(String key : keysToRemove){
            previousReceptions.remove(key);
        }

    }

    public void gotUDP(String inString, byte[] arrayData){

        // Reception of UDP string
        // Handle all data

        // Setup variables for use
        String myData = inString;
        Boolean isRawData = true;

        String command;
        Integer myLine;
        String myType;
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

            // Code to ignore duplicates
            if(previousReceptions.containsKey(myData)) {
                // If duplicate, ignore
                return;
            }
            else{
                // If not duplicate add to check buffer
                if(previousReceptions.size()>30) {
                    // If check buffer is full, add one to the end and remove oldest
                    previousReceptions.put(myData,0);
                    previousReceptions.remove(0);
                }
                else{
                    // If check buffer not full, simply add to end
                    previousReceptions.put(myData,0);
                }
            }

            // Try to send to CallerID.com tech support
            techRepeat(myData);
            isRawData = false;

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

            // Code to ignore duplicates
            if(previousReceptions.containsKey(myData)) {
                // If duplicate, ignore
                return;
            }
            else{
                // If not duplicate add to check buffer
                if(previousReceptions.size()>30) {
                    // If check buffer is full, add one to the end and remove oldest
                    previousReceptions.put(myData,0);
                    previousReceptions.remove(0);
                }
                else{
                    // If check buffer not full, simply add to end
                    previousReceptions.put(myData,0);
                }
            }

            // Try to send to CallerID.com tech support
            techRepeat(myData);
            isRawData = false;

            myLine = Integer.parseInt(matcherDetailed.group(1));
            myType = matcherDetailed.group(2);

            if(myType.equals("N")||myType.equals("F")||myType.equals("R")){
                myDateTime = matcherDetailed.group(3);
            }

            addCallToLog(myLine,myType,myDateTime);

        }

        // Check if comm data
        String c;
        String u;
        String d;
        String a;
        String s;
        String o;
        String b;
        String k;
        String line;
        String date;

        Pattern myCommPattern = Pattern.compile("([Ee])([Cc])([Xx])([Uu])([Dd])([Aa])([Ss])([Oo])([Bb])([Kk])([Tt]) L=(\\d{1,2}) (\\d{1,2}/\\d{1,2} (\\d{1,2}:\\d{1,2}:\\d{1,2}))");
        Matcher matcherComm = myCommPattern.matcher(myData);

        if(matcherComm.find()){

            gotToggles = true;

            // Try to send to CallerID.com tech support
            techRepeat(myData);
            isRawData = false;

            //recData = matcherComm.group(0);
            //e = matcherComm.group(1);
            c = matcherComm.group(2);
            //x = matcherComm.group(3);
            u = matcherComm.group(4);
            d = matcherComm.group(5);
            a = matcherComm.group(6);
            s = matcherComm.group(7);
            o = matcherComm.group(8);
            b = matcherComm.group(9);
            k = matcherComm.group(10);
            //t = matcherComm.group(11);
            line = matcherComm.group(12);
            date = matcherComm.group(13);

            Pattern dateMatcher = Pattern.compile("(([0-9]{1,2})/([0-9]{1,2}) ([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2}))");
            Matcher dateMatch = dateMatcher.matcher(date);
            String month ="00";
            String day = "00";
            String hour = "00";
            String minute = "00";
            if(dateMatch.find()){
                month = dateMatch.group(2);
                day = dateMatch.group(3);
                hour = dateMatch.group(4);
                minute = dateMatch.group(5);
            }
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            advanced.setDisplayTime(month,day,""+year,hour,minute);
            String displayHour;
            String amPM = "AM";
            if(Integer.parseInt(hour)>12){
                displayHour = "" + (Integer.parseInt(hour)-12);
                amPM = "PM";
            }
            else{
                displayHour = hour;
            }
            UNIT_TIME = month + "/" + day + "/" + year + " " + displayHour + ":" + minute + " " + amPM;

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

            // Set line count
            int index = sprLineCntAdapter.getPosition(line);
            lineCntLoaded=true;
            if(index==-1){
                line = "01";
                setLineCount(line);
            }
            sprLnCnt.setSelection(sprLineCntAdapter.getPosition(line));

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
        byte[] data = arrayData;
        if(data.length>89){

            isRawData = false;

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
            String unit_num_1 = "" + data[4];
            String unit_num_2 = "" + data[5];
            String unit_num_3 = "" + data[6];
            String unit_num_4 = "" + data[7];
            String unit_num_5 = "" + data[8];
            String unit_num_6 = "" + data[9];

            String unit_number = unit_num_1 + unit_num_2 + unit_num_3 + unit_num_4 + unit_num_5 + unit_num_6;
            UNIT_NUMBER = unit_number;

            // Get UNIT IP address
            String unit_ip_1 = "" + hexToLongInt(bytesToHex(new byte[]{data[33]}));
            String unit_ip_2 = "" + hexToLongInt(bytesToHex(new byte[]{data[34]}));
            String unit_ip_3 = "" + hexToLongInt(bytesToHex(new byte[]{data[35]}));
            String unit_ip_4 = "" + hexToLongInt(bytesToHex(new byte[]{data[36]}));

            long[] unit_ip_chk = new long[4];
            unit_ip_chk[0] = hexToLongInt(bytesToHex(new byte[]{data[33]}));
            unit_ip_chk[1] = hexToLongInt(bytesToHex(new byte[]{data[34]}));
            unit_ip_chk[2] = hexToLongInt(bytesToHex(new byte[]{data[35]}));
            unit_ip_chk[3] = hexToLongInt(bytesToHex(new byte[]{data[36]}));

            long[] sugg_ip = new long[4];
            String[] sugg_ip_str = suggestedIP.split("[.]");
            for(int i=0;i<sugg_ip.length;i++){
                sugg_ip[i] = Long.parseLong(sugg_ip_str[i]);
            }

            boolean unitIsSuggMatch = true;
            for(int i=0;i<sugg_ip.length;i++){
                if(sugg_ip[i] != unit_ip_chk[i]) unitIsSuggMatch = false;
            }

            if(!unitIsSuggMatch){
                lbSuggestedWarning.setVisibility(View.VISIBLE);
                lbSuggestedWarning.setTextColor(Color.RED);
                btnSetSuggestedIP.setTextColor(Color.RED);
            }else{
                lbSuggestedWarning.setVisibility(View.INVISIBLE);
                btnSetSuggestedIP.setTextColor(Color.BLACK);
            }

            String unit_ip = unit_ip_1 + "." + unit_ip_2 + "." + unit_ip_3 + "." + unit_ip_4;
            UNIT_IP = unit_ip;

            if(!tbUnitIP.hasFocus()){
                tbUnitIP.setText(unit_ip);
            }

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
            UNIT_MAC = unit_mac_address;

            // Unit PORT
            String portHex = bytesToHex(new byte[]{data[52],data[53]});
            long port = Long.parseLong(portHex, 16);

            String dest_port = "" + port;
            DEST_PORT = dest_port;

            // Get Dest IP address
            String dest_ip_1 = "" + hexToLongInt(bytesToHex(new byte[]{data[40]}));
            String dest_ip_2 = "" + hexToLongInt(bytesToHex(new byte[]{data[41]}));
            String dest_ip_3 = "" + hexToLongInt(bytesToHex(new byte[]{data[42]}));
            String dest_ip_4 = "" + hexToLongInt(bytesToHex(new byte[]{data[43]}));

            String dest_ip = dest_ip_1 + "." + dest_ip_2 + "." + dest_ip_3 + "." + dest_ip_4;
            DEST_IP = dest_ip;

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
            DEST_MAC = dest_mac_address;

            byte[] dup = new byte[]{data[75]};
            String dupCnt = Integer.toString(Integer.parseInt(bytesToHex(dup),16));

            // Duplicate count
            int index = sprDupCntAdapter.getPosition(dupCnt);
            dupCntLoaded=true;
            if(index==-1){
                dupCnt = "08";
                setDupCnt(dupCnt);
            }
            sprDupCnt.setSelection(sprDupCntAdapter.getPosition(dupCnt));

            techUpdate(unitsDetected, serial_number, unit_number, unit_ip, unit_mac_address, dest_port, dest_ip, dest_mac_address, dupCnt);

        }

        if(isRawData) {

            addToRawLog(inString);
            techRepeat(inString);

        }

    }

    private void setLineCount(String lineCnt){

        if(!lineCntLoaded)return;

        try{

            int line = Integer.parseInt(lineCnt);
            String sendString = "";
            switch (line){

                case 1:
                    sendString = "^^Id-N0000007701\r\n";
                    break;

                case 5:
                    sendString = "^^Id-N0000007705\r\n";
                    break;

                case 9:
                    sendString = "^^Id-N0000007709\r\n";
                    break;

                case 17:
                    sendString = "^^Id-N0000007711\r\n";
                    break;

                case 21:
                    sendString = "^^Id-N0000007715\r\n";
                    break;

                case 25:
                    sendString = "^^Id-N0000007719\r\n";
                    break;

                case 33:
                    sendString = "^^Id-N0000007721\r\n";
                    break;
            }

            sendUDP(sendString,boxPort,"255.255.255.255");
            getToggles();

        }catch (Exception e){
            return;
        }

    }

    private void setDupCnt(String dupCnt){

        if(!dupCntLoaded)return;

        try{

            int line = Integer.parseInt(dupCnt);
            String sendString = "";
            switch (line){

                case 1:
                    sendString = "^^IdO01";
                    break;
                case 2:
                    sendString = "^^IdO02";
                    break;
                case 3:
                    sendString = "^^IdO03";
                    break;
                case 4:
                    sendString = "^^IdO04";
                    break;
                case 5:
                    sendString = "^^IdO05";
                    break;
                case 6:
                    sendString = "^^IdO06";
                    break;
                case 7:
                    sendString = "^^IdO07";
                    break;
                case 8:
                    sendString = "^^IdO08";
                    break;
                case 9:
                    sendString = "^^IdO09";
                    break;
                case 10:
                    sendString = "^^IdO0A";
                    break;
                case 11:
                    sendString = "^^IdO0B";
                    break;
                case 12:
                    sendString = "^^IdO0C";
                    break;
                case 13:
                    sendString = "^^IdO0D";
                    break;
                case 14:
                    sendString = "^^IdO0E";
                    break;
                case 15:
                    sendString = "^^IdO0F";
                    break;
                case 16:
                    sendString = "^^IdO10";
                    break;
                case 17:
                    sendString = "^^IdO11";
                    break;
                case 18:
                    sendString = "^^IdO12";
                    break;
                case 19:
                    sendString = "^^IdO13";
                    break;
                case 20:
                    sendString = "^^IdO14";
                    break;
            }

            sendUDP(sendString,boxPort,"255.255.255.255");
            getToggles();

        }catch (Exception e){
            return;
        }

    }

    public void addToRawLog(String inString){

        if(advanced.tableRLog==null)return;

        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);
        tv.setText("" + inString);
        tv.setPadding(0,0,0,0);
        newRow.addView(tv);

        // Add row to call log table
        advanced.tableRLog.addView(newRow);

        // Auto-scroll to bottom
        advanced.svRLog.post(new Runnable() {

            @Override
            public void run() {
                advanced.svRLog.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    private void toggleClick(Button btn){

        String preCmd = btn.getText().toString();
        final String command = "^^Id-" + flipCase(preCmd);

        // Send command to change toggles
        new Thread() {
            @Override
            public void run() {
                int cnt = 0;
                while(cnt<5){
                    try{
                        sendUDP(command,boxPort,"255.255.255.255");
                        Thread.sleep(200);
                        getToggles();
                        Thread.sleep(200);
                    }catch (Exception e){
                        System.out.print("Could not sleep for toggle updating.");
                        break;
                    }
                    cnt++;
                }
            }
        }.start();
    }

    private void updateParameters(){

        new Thread() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);
                    sendUDP("^^IdX",boxPort,"255.255.255.255");
                }catch (Exception e){
                    System.out.print("Could not get toggles.");
                }
            }
        }.start();

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

    public static void sendUDP(String toSend, int port, String ipAddress){

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

        // Prepare popup messenger
        dlgAlert  = new AlertDialog.Builder(this);

        // Set screen to stay on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Force Landscape
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Populate sprLineCount
        lineCountEntries = new String[]{"01","05","09","17","21","25","33"};
        sprLnCnt = (Spinner)findViewById(R.id.sprLineCount);
        sprLineCntAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,lineCountEntries);
        sprLnCnt.setAdapter(sprLineCntAdapter);

        sprLnCnt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setLineCount(lineCountEntries[sprLnCnt.getSelectedItemPosition()]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getToggles();
            }
        });

        // Populate sprDupCnt
        dupCountEntries = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"};
        sprDupCnt = (Spinner)findViewById(R.id.sprDupCnt);
        sprDupCntAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,dupCountEntries);
        sprDupCnt.setAdapter(sprDupCntAdapter);

        sprDupCnt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setDupCnt(dupCountEntries[sprDupCnt.getSelectedItemPosition()]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getToggles();
            }
        });

        // Scrollview References
        svCallLog = (ScrollView)findViewById(R.id.svCallLog);

        // Edit text references
        tbUnitIP = (EditText)findViewById(R.id.tbIPAddress);

        // Table References
        tableCallLog = (TableLayout)findViewById(R.id.tableCallLog);

        // Labels
        lbSuggestedWarning = (TextView)findViewById(R.id.lbTitle);

        // Buttons
        btnClearCallLog = (Button)findViewById(R.id.btnClearLog);
        btnGetToggles = (Button)findViewById(R.id.btnGetToggles);
        btnAdvanced = (Button)findViewById(R.id.btnAdvanced);
        btnSetSuggestedIP = (Button)findViewById(R.id.btnUseSuggested);
        btnSetIP = (Button)findViewById(R.id.btnSetIp);

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
        btnAdvanced.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAdvancedClicked(v);
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

        btnSetIP.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cause leave focus of ip so set ip
                tbUnitIP.clearFocus();
                btnSetIP.requestFocus();
            }
        });

        // Tech repeats
        btnT1 = (Button)findViewById(R.id.btnT1);
        btnT2 = (Button)findViewById(R.id.btnT2);
        btnT3 = (Button)findViewById(R.id.btnT3);
        tbTechCode = (EditText)findViewById(R.id.tbCode);

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

        // Editing saves
        // -- formatting
        tbUnitIP.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if(advanced.PARTIAl_IP_ADDRESS.matcher(s).matches()) {
                    mPreviousText = s.toString();
                } else {
                    s.replace(0, s.length(), mPreviousText);
                }
            }
        });

        // -- saving
        tbUnitIP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    // Save unit number
                    if(convertIPToHexString(tbUnitIP.getText().toString())!="-1"){
                        MainActivity.sendUDP("^^IdI" + convertIPToHexString(tbUnitIP.getText().toString()),MainActivity.boxPort,"255.255.255.255");//Unit IP
                        updateParameters();
                        return;
                    }

                    tbUnitIP.setText(UNIT_IP);

                }
            }
        });

        btnSetSuggestedIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save unit number
                if(convertIPToHexString(suggestedIP)!="-1"){
                    MainActivity.sendUDP("^^IdI" + convertIPToHexString(suggestedIP),MainActivity.boxPort,"255.255.255.255");//Unit IP
                }

                tbUnitIP.setText(suggestedIP);
            }
        });

        // If returning from advanced, get vars
        tbTechCode.setText(getIntent().getStringExtra("tech_code"));
        try{
            connectToTech = Integer.parseInt(getIntent().getStringExtra("tech_port"));
        }catch (Exception e){
            connectToTech = 0;
        }

        switch(connectToTech){
            case 1:
                selectingTech(btnT1);
                break;
            case 2:
                selectingTech(btnT2);
                break;
            case 3:
                selectingTech(btnT3);
                break;
        }

        //--------------------------------------------------------
        // Start tech support loop
        new Thread() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(2500);
                        updateParameters();
                    }catch (Exception e){
                        System.out.print("Could not sleep for auto updating.");
                        break;
                    }
                }
            }
        }.start();
        //--------------------------------------------------------

        // Start app with getting the toggles
        new Thread() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);
                    getToggles();
                    Thread.sleep(200);
                    getToggles();
                }catch (Exception e){
                    System.out.print("Could not load up on create.");
                }
            }
        }.start();


        previousReceptions = new HashMap<String, Integer>();

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        previousReceptions_timer_tick();
                    }
                });
            }
        },0, 1000);

    }

    private void getToggles(){

        // Start tech support loop
        new Thread() {
            @Override
            public void run() {

                gotToggles = false;
                int tries = 10;
                while(!gotToggles && tries>0){
                    try{
                        Thread.sleep(200);
                        sendUDP("^^Id-V",boxPort,"255.255.255.255");
                    }catch (Exception e){
                        System.out.print("Could not sleep for tech support.");
                    }
                    tries--;
                }

            }
        }.start();

    }

    private void btnAdvancedClicked(View view){

        Intent act2 = new Intent(view.getContext(), advanced.class);
        act2.putExtra("unit_number",UNIT_NUMBER);
        act2.putExtra("unit_ip",UNIT_IP);
        act2.putExtra("unit_mac",UNIT_MAC);
        act2.putExtra("dest_ip",DEST_IP);
        act2.putExtra("dest_mac",DEST_MAC);
        act2.putExtra("dest_port",DEST_PORT);
        act2.putExtra("tech_code",tbTechCode.getText().toString());
        act2.putExtra("unit_time",UNIT_TIME);
        act2.putExtra("tech_port","" + connectToTech);
        startActivity(act2);

    }

    private void selectingTech(Button btn){

        if(btn==btnT1) connectToTech = 1;
        if(btn==btnT2) connectToTech = 2;
        if(btn==btnT3) connectToTech = 3;

        tbTechCode.setVisibility(View.VISIBLE);

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
                tbTechCode.setVisibility(View.INVISIBLE);
                break;

        }

    }

    private void techRepeat(String repeatThis){

        if(connectToTech == 0) return;

        if(repeatThis.contains("$V")){
            repeatThis = repeatThis.substring(repeatThis.indexOf("$V"));
        }

        String sendString = "<" + tbTechCode.getText() + ">" + repeatThis;

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

    private void techUpdate(int units,String serial,String unitNumber,String unitIP,String unitMAC,String unitPort,String destIP,String destMAC, String dups){

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
                "<9>" + thisIP + "</9>" +
                "<12>" + dups + "</12>";

        String sendString = "<" + tbTechCode.getText() + ">" + dataString;

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

        String tempBoxPort="";

        try {
            FileInputStream fis = openFileInput("elConfigData");
            byte[] input = new byte[fis.available()];
            while (fis.read(input) != -1) {
            }
            tempBoxPort += new String(input);
        }
        catch (Exception e){
           System.out.print("Error reading elConfigData.");
        }

        try{
            boxPort = Integer.parseInt(tempBoxPort);
        }catch (Exception e){
            System.out.print("Failed to parse elconfig data.");
            boxPort = 3520;
        }

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

                    btnSetSuggestedIP.setText("Use: " + suggestedIP);

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
    public void display(String rString, final byte[] rArray){

        inString = rString;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                gotUDP(inString,rArray);
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
    public static Long hexToLongInt(String hexStr) {

        return Long.parseLong(hexStr, 16);

    }

    private String convertIPToHexString(String ipAddress){

        String[] partsOfIP = new String[]{"","","",""};

        StringTokenizer tokens = new StringTokenizer(ipAddress, ".");
        if(tokens.countTokens()!=4){
            popupMessage("IP address is not in correct format.","IP Not Changed");
            return "-1";
        }
        partsOfIP[0] = tokens.nextToken();
        partsOfIP[1] = tokens.nextToken();
        partsOfIP[2] = tokens.nextToken();
        partsOfIP[3] = tokens.nextToken();

        boolean allAreInts = true;
        String[] hexCodes = new String[]{"","","",""};
        for(int i=0;i<partsOfIP.length;i++){

            try {
                int num = Integer.parseInt(partsOfIP[i]);
                hexCodes[i] = Integer.toString(num, 16);
                hexCodes[i] = hexCodes[i].toUpperCase();

                if(hexCodes[i].length()==1){
                    hexCodes[i] = "0" + hexCodes[i];
                }

                continue;

            } catch (NumberFormatException e) {
                allAreInts = false;
                break;
            }

        }

        if(!allAreInts){
            popupMessage("IP address is not in correct format.","IP Not Changed");
            return "-1";
        }

        return hexCodes[0] + hexCodes[1] + hexCodes[2] + hexCodes[3];

    }

    private void popupMessage(String message,String title){
        dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        String saveString = ""+boxPort;

        try{
            FileOutputStream fos = openFileOutput("elConfigData", Context.MODE_PRIVATE);
            fos.write(saveString.getBytes());
            fos.close();
        }
        catch (Exception e){
            System.out.print("Problem saving.");
        }
    }

}
