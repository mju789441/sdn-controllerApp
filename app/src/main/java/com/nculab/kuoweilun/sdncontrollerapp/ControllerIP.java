package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Handler;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

public class ControllerIP {
    private String _IP = "";
    private int _port = 8000;
    private Socket socket = new Socket();
    private BufferedReader reader;
    private BufferedWriter writer;
    private Handler handler = new Handler();
    private final ReentrantLock lock = new ReentrantLock();

    public ControllerIP(String IP) {
        _IP = IP;
    }

    public String getIP() {
        return _IP;
    }

    public void setIP(String IP) {
        _IP = IP;
    }

    public void connect(final TextView status) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect(new InetSocketAddress(_IP, _port));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    lock.lock();
                    try {
                        if (socket.isConnected() && !(socket.isClosed())) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    status.setText("連線");
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    status.setText("未連線");
                                }
                            });
                            break;
                        }
                        Thread.sleep(100);
                    } catch (Exception e) {

                    } finally {
                        lock.unlock();
                    }
                }
            }
        }).start();
    }

    public void watchPkt(final TextView msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMsg("watch_pkt");
                while (true) {
                    try {
                        final String str = getMsg();
                        if (str == null) {
                            break;
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    msg.setText(msg.getText().toString() + str + "\n");
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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

    public void close() {
        try {
            socket.close();
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

}
