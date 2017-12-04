package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
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
    private ControlerViewHolder holder = new ControlerViewHolder();
    //component handler
    private Handler handler = new Handler();
    //控制thread的變數
    public boolean busy = false;
    //thread

    public Controller(String IP, Context context) {
        this.IP = IP;
        //_IP = "192.168.1.1;//測試用;
        this.context = context;
        setThread();
    }

    public void setTextView_IP(TextView textView_IP) {
        holder.textView_IP = textView_IP;
    }

    public void setTextView_status(TextView textView_status) {
        holder.textView_status = textView_status;
    }

    public void setStatus(final String status) {
        this.status = status;
        handler.post(new Runnable() {
            @Override
            public void run() {
                holder.textView_status.setText(status);
            }
        });
    }

    public void setThread() {

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

    public void connect() {
        new Thread(new Runnable() {
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
                    //除錯
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    busy = false;
                }
            }
        }).start();
    }

    public void sendInstruction(final String instruction) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMsg(rsa.encrypt(instruction.getBytes()));
                } catch (final Exception e) {
                    e.printStackTrace();
                    disconnection();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
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
