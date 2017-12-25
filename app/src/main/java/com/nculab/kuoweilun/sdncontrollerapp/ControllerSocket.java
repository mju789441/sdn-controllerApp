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

public class ControllerSocket {

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
    //component handler
    private Handler handler = new Handler();
    //thread
    public Thread thread_connect;

    public ControllerSocket(String IP, Context context) {
        this.IP = IP;
        this.context = context;
        setThread();
    }

    public void setThread() {
        thread_connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //已連線時拋出例外
                    if (isConnected()) {
                        throw new InterruptedException();
                    }

                    //連線
                    status = "連線中";
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
                    status = "已連線";
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (final Exception e) {
                    e.printStackTrace();
                    disconnection();
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

    public boolean failConnected() {
        if (status == "斷線") {
            return true;
        }
        return false;
    }

    public void disconnection() {
        status = "斷線";
        close();
    }

    public void reset() {
        status = "未連線";
        close();
    }

    public String getDncryptedMsg() {
        try {
            String msg = getMsg();
            if (msg == null) {
                return null;
            }
            return rsa.decrypt(msg.getBytes());
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "未能接收訊息", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
    }

    public void sendEncryptedMsg(final String instruction) {
        try {
            sendMsg(rsa.encrypt(instruction.getBytes()));
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "未能傳送訊息", Toast.LENGTH_SHORT).show();
                }
            });
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
