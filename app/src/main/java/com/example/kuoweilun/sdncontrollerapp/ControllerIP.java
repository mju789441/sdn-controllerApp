package com.example.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

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
    Handler handler = new Handler();


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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(_IP, _port);
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMsg(String msg) throws IOException {
        writer.write(msg);
        writer.flush();
    }

    private String getMsg() throws IOException {
        return reader.readLine();
    }

    public void connectStatus(final TextView status) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (socket.isConnected()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    status.setText("連線");
                                }
                            });
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("未連線");
                        }
                    });
                }
            }
        }).start();
    }
}
