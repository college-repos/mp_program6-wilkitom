//Author: Thomas Wilkinson
//Last update: 11/27/2018

package com.example.thomaswilkinson.program6;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jackandphantom.joystickview.JoyStickView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    String hostName;
    doNetwork stuff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Set EditTexts, assign EditTexts for server connection, initiate server connection
        final EditText hostNameET = findViewById(R.id.HostName);
        Button goBtn = findViewById(R.id.button);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hostName = hostNameET.getText().toString();
                serverConnection();

            }
        });
        //Set onclick listeners to stop bot, scan
        Button stopBtn = findViewById(R.id.stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stuff.moveMessage = "noop";
            }
        });
        Button scanBtn = findViewById(R.id.scan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stuff.tmpMsg = stuff.moveMessage;
                stuff.moveMessage = "scan";
            }
        });
        //Set joysticks and listeners
        JoyStickView joyStickView = findViewById(R.id.joy);
        joyStickView.setOnMoveListener(new JoyStickView.OnMoveListener() {
            @Override
            public void onMove(double angle, float strength) {
                calculateShoot(angle);
            }
        });
        JoyStickView joyStickView2 = findViewById(R.id.joy2);
        joyStickView2.setOnMoveListener(new JoyStickView.OnMoveListener() {
            @Override
            public void onMove(double angle, float strength) {
                calculateMove(angle, strength);
            }
        });
    }

    //take joystick information and set move message
    void calculateMove(double angle, float str)
    {
        if(angle > 23 && angle < 67) stuff.moveMessage=("move 1 -1");
        else if(angle > 68 && angle < 113) stuff.moveMessage=("move 0 -1");
        else if(angle > 112 && angle < 157) stuff.moveMessage=("move -1 -1");
        else if(angle > 158 && angle < 202) stuff.moveMessage=("move -1 0");
        else if(angle > 203 && angle < 247) stuff.moveMessage=("move -1 1");
        else if(angle > 248 && angle < 292) stuff.moveMessage=("move 0 1");
        else if(angle > 293 && angle < 337) stuff.moveMessage=("move 1 1");
        else stuff.moveMessage=("move 1 0");
    }

    //take joystick information and set shoot message
    void calculateShoot(double angle)
    {
        int i = (int) angle;
        i = Math.abs(i-360);
        String str = Integer.toString(i);
        stuff.shootMessage = "fire "+str;
    }
    //Connect to server, run thread
    public void serverConnection(){
        stuff = new doNetwork(hostName);
        Thread myNet = new Thread(stuff);
        myNet.start();
    }

}

class doNetwork implements Runnable {
    doNetwork(String hostName){
        this.hostName = hostName;
    }
    public PrintWriter out;
    public BufferedReader in;
    Activity activity;
    String tmpMsg = "noop";
    String shootMessage = "fire 1";
    String moveMessage = "move -1 -1";
    int port = 3012;
    String hostName;
    Boolean done = true;
    public void run() {


        try {
            //Connect to server
            InetAddress serverAddr = InetAddress.getByName(hostName);
            Socket socket = new Socket(serverAddr, port);

            //setting up read/write streams
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //now send a message to the server and then read back the response.
            try {
                //read server connection message
                String str = in.readLine();
                //write bot values
                out.println("TomBot 0 0 0");
                //read bot info
                str = in.readLine();
                if(str == "setup error")
                {
                    out.println("TomBot 0 0 5");
                }
                while(done)
                {
                    //send shoot message, then send half of necessary move messages, then read, then send, then read
                    out.println(shootMessage);
                    for(int i=0; i<5; i++) {
                        out.println(moveMessage);
                        if(moveMessage == "scan") moveMessage = tmpMsg;
                    }
                    for(int i=0; i<6; i++) {
                        str = in.readLine();
                        if(str.charAt(0)=='I')
                        {
                            i--;
                        }
                        if(str.equals("Info Dead") || str.equals("Info GameOver"))
                        {
                            done = false;
                        }
                        if(str.charAt(0)=='s')
                        {
                            while(true)
                            {
                                str = in.readLine();
                                if(str.charAt(0)!= 's')
                                {
                                    break;
                                }
                            }
                        }
                    }
                    for(int i=0; i<5; i++) {
                        out.println(moveMessage);
                        if(moveMessage == "scan") moveMessage = tmpMsg;
                    }
                    for(int i=0; i<5; i++) {
                        str = in.readLine();
                        if(str.charAt(0)=='I')
                        {
                            i--;
                        }
                        if(str.equals("Info Dead") || str.equals("Info GameOver"))
                        {
                            done = false;
                        }
                        if(str.charAt(0)=='s')
                        {
                            while(true)
                            {
                                str = in.readLine();
                                if(str.charAt(0)!= 's')
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {

            } finally {
                in.close();
                out.close();
                socket.close();
            }

        } catch (Exception e) {
        }
    }


}