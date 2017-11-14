package com.example.kuoweilun.sdncontrollerapp;

import android.os.Handler;
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
    private int _port = 9487;
    private Socket socket = null;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Handler handler = new Handler();

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

    public void close() {
        try {
            socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(final String msg) {
        try {
            writer.write(msg + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMsg() throws IOException {
        return reader.readLine();
    }

    public void connectStatus(final TextView status) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (socket.isConnected() && !(socket.isClosed())) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    status.setText("連線");
                                }
                            });
                        }
                    } catch (Exception e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                status.setText("未連線");
                            }
                        });
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
