package com.example.kuoweilun.sdncontrollerapp;

import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

public class ControllerIP {
    private String _IP = "";
    private int _port = 8000;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;


    public ControllerIP(String IP) {
        _IP = IP;
        setThread();
        connectSocket();
    }

    public String getIP() {
        return _IP;
    }

    public void setIP(String IP) {
        _IP = IP;
    }

    private void setThread() {

    }

    public void connectSocket() {
        try {
            socket = new Socket(_IP, _port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String msg) throws IOException {
        writer.write(msg);
        writer.flush();
    }

    private String getMsg() throws IOException {
        return reader.readLine();
    }

    public void connecctStatus(final TextView status) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (socket.isConnected()) {
                            status.setText("連線");
                        } else {
                            status.setText("未連線");
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
