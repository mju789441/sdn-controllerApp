package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Handler;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

public class ControllerIP {
    private String _IP = "140.115.204.156";
    private int _port = 8000;
    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintStream writer = null;
    private Handler handler = new Handler();
    private final ReentrantLock lock = new ReentrantLock();
    private RSA rsa = null;
    private Context context = null;

    public ControllerIP(String IP, Context context) {
        _IP = IP;
        _IP = "192.168.1.3";
        this.context = context;
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
                //rsa = new RSA();
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(_IP, _port));
                    writer = new PrintStream(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("連線");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("斷線");
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //getKey
                    /*final String str = getMsg();
                    rsa.setPublicKey(str);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                        }
                    });
                    //setKey
                    sendMsg(rsa.getMyPublicKey());*/
            }
        }).start();
    }

    public void watchPkt(final TextView msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMsg("watch_pkt");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "傳遞成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                while (true) {
                    try {
                        final String str = getMsg();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (str == null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    msg.setText(msg.getText().toString() + "\nnull");
                                }
                            });
                            break;
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    msg.setText(msg.getText().toString() + "\n" + str);
                                }
                            });
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
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

    public String getMsg() throws IOException {
        return reader.readLine();
    }

    public void sendMsg(String msg) throws IOException {
        writer.println(msg);
    }

}
