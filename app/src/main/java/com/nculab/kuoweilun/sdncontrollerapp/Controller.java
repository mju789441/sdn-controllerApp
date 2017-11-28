package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;

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
    private RSA rsa = null;
    //conponent
    private Context context = null;
    private ViewHolder holder = new ViewHolder();
    private TextView textView_msg = null;
    //component handler
    private Handler handler = new Handler();
    //控制thread的變數
    public boolean busy = false;
    //thread
    public Thread watch_pkt;

    public Controller(String IP, Context context) {
        this.IP = IP;
        //_IP = "10.115.49.97";
        this.context = context;
        setThread();
    }

    public void setTextView_msg(TextView textView_msg) {
        this.textView_msg = textView_msg;
    }

    public void setTextView_IP(TextView textView_IP) {
        holder.textView_IP = textView_IP;
    }

    public void setTextView_status(TextView textView_status) {
        holder.textView_status = textView_status;
    }

    public void setThread() {
        watch_pkt = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        busy = true;
                        if (busy) {
                            throw new IOException();
                        }
                        final String str = rsa.decrypt(getMsg().getBytes());
                        if (str == null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textView_msg.setText(textView_msg.getText().toString() + "\nnull");
                                }
                            });
                            break;
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        textView_msg.setText(textView_msg.getText().toString() + "\n" + str);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        Thread.sleep(100);
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView_msg.setText(textView_msg.getText().toString() + "\nwrong");
                            }
                        });
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView_msg.setText(textView_msg.getText().toString() + "\nwrong");
                            }
                        });
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView_msg.setText(textView_msg.getText().toString() + "\nwrong");
                            }
                        });
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        busy = false;
                        break;
                    }
                }
            }
        });
    }

    public void setStatus(final String status) {
        this.status = status;
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    holder.textView_status.setText(status);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    busy = true;
                    if (busy) {
                        throw new IOException();
                    }
                    //連線
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(IP, port));
                    writer = new PrintStream(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    rsa = new RSA();
                    setStatus("連線");
                    //送出MyPublicKey
                    final String str = rsa.getMyPublicKey();
                    sendMsg(str);
                    //接收對方的PublicKey
                    final String key = getMsg();
                    rsa.setPublicKey(key);
                    //除錯
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "key: " + key, Toast.LENGTH_SHORT).show();
                        }
                    });
                    busy = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("斷線");
                    close();
                    busy = false;
                } catch (final Exception e) {
                    e.printStackTrace();
                    setStatus("斷線");
                    close();
                    //除錯
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("斷線");
                    close();
                } catch (final Exception e) {
                    e.printStackTrace();
                    setStatus("斷線");
                    close();
                    //除錯
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
