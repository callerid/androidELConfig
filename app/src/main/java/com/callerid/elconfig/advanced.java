package com.callerid.elconfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class advanced extends Activity{

    // Commons
    private static final Pattern PARTIAl_IP_ADDRESS =
            Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+
                    "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");

    // Advanced variables
    private static String UNIT_NUMBER;
    private static String UNIT_IP;
    private static String UNIT_MAC;
    private static String DEST_IP;
    private static String DEST_PORT;
    private static String DEST_MAC;

    private static String techCode;

    AlertDialog.Builder dlgAlert;

    // Buttons
    private Button btnBack;
    private Button btnClearRLog;
    private Button btnResetELDefaults;
    private Button btnResetUDefaults;

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

        // Prepare popup messenger
        dlgAlert  = new AlertDialog.Builder(this);

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
        btnResetELDefaults = (Button)findViewById(R.id.btnResetEthernetDefaults);
        btnResetUDefaults = (Button)findViewById(R.id.btnResetUnitDefaults);

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

        btnResetELDefaults.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Reset Ethernet Defaults? Cannot be undone.").setPositiveButton("Yes", btnResetELDefaultsClick)
                        .setNegativeButton("No", btnResetELDefaultsClick).show();

            }
        });

        btnResetUDefaults.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Reset Unit Defaults? Cannot be undone.").setPositiveButton("Yes", btnResetUDefaultsClick)
                        .setNegativeButton("No", btnResetUDefaultsClick).show();

            }
        });

        // Changing UNIT NUMBER
        tbUnitNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    // Save unit number
                    String unitNumber = tbUnitNumber.getText().toString();
                    while(unitNumber.length()<6){
                        unitNumber = "0" + unitNumber;
                    }

                    MainActivity.sendUDP("^^IdU000000" + unitNumber,MainActivity.boxPort,"255.255.255.255");//Unit ID
                    updateParameters();

                }
            }
        });

        // Changing DEST IP
        // -- formatting
        tbDestIP.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if(PARTIAl_IP_ADDRESS.matcher(s).matches()) {
                    mPreviousText = s.toString();
                } else {
                    s.replace(0, s.length(), mPreviousText);
                }
            }
        });

        // -- saving
        tbDestIP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    // Save unit number
                    if(convertIPToHexString(tbDestIP.getText().toString())!="-1"){
                        MainActivity.sendUDP("^^IdD" + tbDestIP.getText().toString(),MainActivity.boxPort,"255.255.255.255");//External IP
                        updateParameters();
                        return;
                    }

                    tbDestIP.setText(DEST_IP);

                }
            }
        });

        // Changing DEST PORT
        tbDestPort.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    // Save destination port
                    try{
                        int num = Integer.parseInt(tbDestPort.getText().toString());

                        String hexPort = Integer.toString(num, 16);

                        while(hexPort.length()<4){
                            hexPort = "0" + hexPort;
                        }

                        hexPort = hexPort.toUpperCase();

                        MainActivity.sendUDP("^^IdT"+hexPort,MainActivity.boxPort,"255.255.255.255");//Port Number
                        updateParameters();

                    }catch (Exception e){
                        popupMessage("Invalid destination port.","Port Not Changed");
                        tbDestPort.setText(DEST_PORT);
                        return;
                    }
                }
            }
        });

        // Changing DEST MAC --------------------------------------------------------------------------
        // -- Formatting
        tbDestMac.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        tbDestMac.setSingleLine();
        InputFilter[] filters = new InputFilter[1];

        filters[0] = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (resultingTxt.matches("([0-9a-fA-F][0-9a-fA-F]-){0,5}[0-9a-fA-F]"))
                    {

                    }
                    else if(resultingTxt.matches("([0-9a-fA-F][0-9a-fA-F]-){0,4}[0-9a-fA-F][0-9a-fA-F]")){
                        return source.subSequence(start, end)+"-" ;
                    }
                    else if(resultingTxt.matches("([0-9a-fA-F][0-9a-fA-F]-){0,5}[0-9a-fA-F][0-9a-fA-F]")){

                    }
                    else
                    {
                        return "";
                    }
                }
                return null;
            }
        };
        tbDestMac.setFilters(filters);

        //-- Saving
        tbDestMac.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    // Save destination MAC
                    StringTokenizer tokens = new StringTokenizer(tbDestMac.getText().toString(), "-");
                    if(tokens.countTokens()!=6){
                        popupMessage("MAC address is not in correct format.","MAC Not Changed");
                        tbDestMac.setText(DEST_MAC);
                        return;
                    }
                    String[] partsOfMac = new String[]{"","","","","",""};
                    partsOfMac[0] = tokens.nextToken();
                    partsOfMac[1] = tokens.nextToken();
                    partsOfMac[2] = tokens.nextToken();
                    partsOfMac[3] = tokens.nextToken();
                    partsOfMac[4] = tokens.nextToken();
                    partsOfMac[5] = tokens.nextToken();

                    for(int i=0;i<partsOfMac.length;i++){
                        partsOfMac[i] = partsOfMac[i].toUpperCase();
                    }

                    if(partsOfMac.length!=6){
                        popupMessage("MAC address is not in correct format.","MAC Not Changed");
                        tbDestMac.setText(DEST_MAC);
                        return;
                    }

                    String hexMac = partsOfMac[0] + partsOfMac[1] + partsOfMac[2] + partsOfMac[3] + partsOfMac[4] + partsOfMac[5];

                    MainActivity.sendUDP("^^IdC"+hexMac,MainActivity.boxPort,"255.255.255.255");//Destination MAC address
                    updateParameters();

                }
            }
        });

        //---------------------------------------------------------------------------------------------

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

    private void btnBackClicked(View view){

        Intent act2 = new Intent(view.getContext(), MainActivity.class);
        act2.putExtra("tech_code",techCode);
        startActivity(act2);

    }

    DialogInterface.OnClickListener btnResetELDefaultsClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    new Thread() {
                        @Override
                        public void run() {
                            while(true){
                                try{

                                    MainActivity.sendUDP("^^IdDFFFFFFFF",MainActivity.boxPort,"255.255.255.255");//Destination IP
                                    Thread.sleep(400);
                                    MainActivity.sendUDP("^^IdU000000000001",MainActivity.boxPort,"255.255.255.255");//Unit ID
                                    Thread.sleep(400);
                                    MainActivity.sendUDP("^^IdIC0A8005A",MainActivity.boxPort,"255.255.255.255");//Internal IP
                                    Thread.sleep(400);
                                    MainActivity.sendUDP("^^IdCFFFFFFFFFFFF",MainActivity.boxPort,"255.255.255.255");//Destination MAC address
                                    Thread.sleep(400);
                                    MainActivity.sendUDP("^^IdM0620101332CC",MainActivity.boxPort,"255.255.255.255");//Internal MAC address
                                    Thread.sleep(400);
                                    MainActivity.sendUDP("^^IdT0DC0",MainActivity.boxPort,"255.255.255.255");//Port Number
                                    Thread.sleep(400);
                                    updateParameters();

                                }catch (Exception e){
                                    System.out.print("Could not reset ethernet defaults.");
                                    break;
                                }
                            }
                        }
                    }.start();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //Do nothing
                    break;
            }
        }
    };

    DialogInterface.OnClickListener btnResetUDefaultsClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    new Thread() {
                        @Override
                        public void run() {
                            while(true){
                                try{

                                    MainActivity.sendUDP("^^Id-N0000007701",MainActivity.boxPort,"255.255.255.255");//External IP
                                    Thread.sleep(400);
                                    MainActivity.sendUDP("^^Id-R",MainActivity.boxPort,"255.255.255.255");//Reset
                                    updateParameters();

                                }catch (Exception e){
                                    System.out.print("Could not reset unit defaults.");
                                    break;
                                }
                            }
                        }
                    }.start();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //Do nothing
                    break;
            }
        }
    };

    private void updateParameters(){
        new Thread() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);
                    MainActivity.sendUDP("^^IdX",3520,"255.255.255.255");
                }catch (Exception e){
                    System.out.print("Could not sleep for updating params.");
                }
            }
        }.start();
    }

}