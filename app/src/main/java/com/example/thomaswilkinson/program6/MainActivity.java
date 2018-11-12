package com.example.thomaswilkinson.program6;

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
    double shootAngle = 0;
    double moveAngle = 0;
    int port = 3012;
    String ipAdr, hostName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Set joysticks and listeners
        JoyStickView joyStickView = findViewById(R.id.joy);
        joyStickView.setOnMoveListener(new JoyStickView.OnMoveListener() {
            @Override
            public void onMove(double angle, float strength) {
                shootAngle = angle;
            }
        });
        JoyStickView joyStickView2 = findViewById(R.id.joy2);
        joyStickView2.setOnMoveListener(new JoyStickView.OnMoveListener() {
            @Override
            public void onMove(double angle, float strength) {
                moveAngle = angle;
            }
        });


        //Set EditTexts, assign EditTexts for server connection, initiate server connection
        final EditText hostNameET = findViewById(R.id.HostName);
        final EditText ipAddressET = findViewById(R.id.IPAddress);
        Button goBtn = findViewById(R.id.button);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostName = hostNameET.getText().toString();
                ipAdr = ipAddressET.getText().toString();
            }
        });
    }


    //Connect to server, run thread
    public void serverConnection(){
        doNetwork stuff = new doNetwork();
        Thread myNet = new Thread(stuff);
        myNet.start();
    }

}

class doNetwork implements Runnable {
    public PrintWriter out;
    public BufferedReader in;

    public void run() {
        int p = Integer.parseInt(port.getText().toString());
        String h = hostname.getText().toString();
        mkmsg("host is " + h + "\n");
        mkmsg(" Port is " + p + "\n");
        try {
            InetAddress serverAddr = InetAddress.getByName(h);
            mkmsg("Attempt Connecting..." + h + "\n");
            Socket socket = new Socket(serverAddr, p);
            String message = "Hello from Client android emulator";

            //made connection, setup the read (in) and write (out)
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //now send a message to the server and then read back the response.
            try {
                //write a message to the server
                mkmsg("Attempting to send message ...\n");
                out.println(message);
                mkmsg("Message sent...\n");

                //read back a message from the server.
                mkmsg("Attempting to receive a message ...\n");
                String str = in.readLine();
                mkmsg("received a message:\n" + str + "\n");

                mkmsg("We are done, closing connection\n");
            } catch (Exception e) {
                mkmsg("Error happened sending/receiving\n");

            } finally {
                in.close();
                out.close();
                socket.close();
            }

        } catch (Exception e) {
            mkmsg("Unable to connect...\n");
        }
    }
}