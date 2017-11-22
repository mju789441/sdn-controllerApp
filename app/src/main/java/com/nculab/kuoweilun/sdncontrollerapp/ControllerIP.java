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

public class ControllerIP {
    private String _IP = "140.115.204.156";
    private int _port = 9487;
    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintStream writer = null;
    private Handler handler = new Handler();
    private final ReentrantLock lock = new ReentrantLock();
    private RSA rsa = null;
    private Context context = null;
    private TextView textView_msg;
    public Thread watch_pkt;

    public ControllerIP(String IP, Context context) {
        _IP = IP;
        _IP = "192.168.1.3";
        this.context = context;
        setThread();
    }

    public String getIP() {
        return _IP;
    }

    public void setIP(String IP) {
        _IP = IP;
    }

    public void setTextView_msg(TextView textView_msg) {
        this.textView_msg = textView_msg;
    }

    public void setThread() {
        watch_pkt = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final String str = new String(rsa.decrypt(Base64.decode(getMsg(), Base64.DEFAULT)));
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
                                    textView_msg.setText(textView_msg.getText().toString() + "\nnull");
                                }
                            });
                            break;
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        textView_msg.setText(textView_msg.getText().toString() + "\n" + rsa.decrypt(str));
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
                        break;
                    }
                }
            }
        });
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

                    rsa = new RSA();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("連線");
                        }
                    });

                    final String str = rsa.getMyPublicKey();
                    sendMsg(str);

                    final String key = getMsg();
                    rsa.setPublicKey(key);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "key: " + key, Toast.LENGTH_SHORT).show();
                        }
                    });

                    //rsa.setPublicKey(getMsg());
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
                            status.setText("斷線");
                        }
                    });
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

    public void sendWatchPkt() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMsg(Base64.encodeToString(rsa.encrypt("watch_pkt"), Base64.DEFAULT);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
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
