package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

public class Controller {

    String IP = "140.115.204.156";
    private int port = 9487;
    String status = "未連線";
    //socket
    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintStream writer = null;
    //rsa
    public RSA rsa = null;
    //conponent
    private Context context = null;
    private ControllerAdapter adapter;
    //component handler
    private Handler handler = new Handler();
    //控制thread的變數
    public boolean busy = false;
    //thread
    public Thread thread_connect;

    public Controller(String IP, Context context, ControllerAdapter adapter) {
        this.IP = IP;
        //_IP = "192.168.1.1;//測試用;
        this.context = context;
        this.adapter = adapter;
        setThread();
    }

    public void setStatus(final String status) {
        this.status = status;
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void setThread() {
        thread_connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //線程忙碌或已連線時拋出例外
                    if (busy || isConnected()) {
                        throw new InterruptedException();
                    }

                    busy = true;
                    //連線
                    setStatus("連線中");
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(IP, port));
                    writer = new PrintStream(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    rsa = new RSA();
                    //送出MyPublicKey
                    final String str = rsa.getMyPublicKey();
                    sendMsg(str);
                    //接收對方的PublicKey
                    final String key = getMsg();
                    if (key == null) {
                        throw new Exception();
                    }
                    rsa.setPublicKey(key);
                    setStatus("已連線");
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (final Exception e) {
                    e.printStackTrace();
                    disconnection();
                } finally {
                    busy = false;
                }
            }
        });
    }

    public boolean isConnected() {
        if (status == "已連線") {
            return true;
        }
        return false;
    }

    public void disconnection() {
        setStatus("斷線");
        close();
    }

    public void reset() {
        setStatus("未連線");
        close();
    }

    public void sendEncryptedMsg(final String instruction) {
        try {
            sendMsg(rsa.encrypt(instruction.getBytes()));
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMsg() throws IOException {
        return reader.readLine();
    }

    public void sendMsg(String msg) throws IOException {
        writer.println(msg);
    }

}
