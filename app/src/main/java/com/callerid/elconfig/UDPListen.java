package com.callerid.elconfig;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;

public class UDPListen extends Service {

    // Setup required variables
    private final IBinder mBinder = new LocalBinder();
    private ServiceCallbacks serviceCallbacks;
    String recString = "";
    DatagramSocket socket = null;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        UDPListen getService() {
            // Return this instance of LocalService so clients can call public methods
            return UDPListen.this;
        }
    }

    @Override
    public void onCreate(){

        // Listening thread start
        if(!idle.isAlive()) {
            idle.start();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Set callbacks for interface
    public void setCallbacks(ServiceCallbacks callbacks){
        serviceCallbacks = callbacks;
    }

    public int getBoxPort(){
        return socket.getLocalPort();
    }

    // ** Main thread that listens for UDP traffic from CallerID.com unit.
    Thread idle = new Thread(new Runnable() {
        public void run()
        {

            // ** This is where CallerID.com Hardware sends
            //    the CallerID records via Ethernet.
            try{

                socket = new DatagramSocket(null);
                int boxPort = MainActivity.boxPort;
                SocketAddress address = new InetSocketAddress("0.0.0.0",boxPort);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                socket.bind(address);

                byte[] buffer = new byte[65507];
                Boolean looping = true;
                byte[] rtnArray;
                while (looping) {
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    try {

                        int currentPort = getBoxPort();
                        if(currentPort!=MainActivity.boxPort){
                            socket.close();
                            rebootIdleThread();
                            break;
                        }

                        socket.receive(dp);
                        recString = new String(dp.getData(), 0, dp.getLength());

                        rtnArray = new byte[dp.getLength()];
                        System.arraycopy(dp.getData(), 0, rtnArray, 0, dp.getLength());

                        // Send UDP packet information to MainActivity through interface
                        // Also filter out small packets
                        if(recString.length()>10){
                            serviceCallbacks.display(recString,rtnArray);
                        }

                    } catch (Exception ex) {
                        System.out.println("Exception: " + ex.toString());
                        looping = false;
                    }
                }

                System.out.println("Thread ended.");

            }catch(Exception exMain){
                System.out.print("Exception: " + exMain.toString());
            }

        }
    });

    private void rebootIdleThread(){

        new Thread() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);
                    idle.run();
                }catch (Exception e){
                    System.out.print("Failed to reboot listening thread.");
                }
            }
        }.start();


    }

}



