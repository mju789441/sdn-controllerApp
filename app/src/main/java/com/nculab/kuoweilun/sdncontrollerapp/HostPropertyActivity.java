package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostPropertyActivity extends AppCompatActivity {

    //Component
    private View activityView;
    private ControllerSocket controllerSocket;
    private TextView switchID;
    private TextView port;
    private TextView mac;
    private TextView IP;
    private TextView speed;
    private TextView rx;
    private TextView tx;
    private TextView duration;
    private TextView duration_nsec;
    private TextView rx_errors;
    private TextView ipstat;
    private Button button_backToHost;
    private Host host;
    private Property property = null;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getHostProperty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityView = HostPropertyActivity.this.getLayoutInflater().inflate(R.layout.layout_hostproperty, null);
        setContentView(activityView);
        Bundle bundle = this.getIntent().getExtras();
        String connect_IP = bundle.getString("controller.IP");
        controllerSocket = new ControllerSocket(connect_IP, HostPropertyActivity.this);
        host = (Host) bundle.getSerializable("host");
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        thread_getHostProperty.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getHostProperty.interrupt();
        controllerSocket.thread_connect.interrupt();
        controllerSocket.reset();
    }

    private void initView() {
        switchID = (TextView) findViewById(R.id.textView_switchID);
        switchID.setText(switchID.getText() + host.ID);
        port = (TextView) findViewById(R.id.textView_port);
        port.setText(port.getText() + host.port);
        mac = (TextView) findViewById(R.id.textView_mac);
        mac.setText(mac.getText() + host.mac);
        IP = (TextView) findViewById(R.id.textView_IP);
        IP.setText(IP.getText() + host.IP);
        speed = (TextView) findViewById(R.id.textView_speed);
        rx = (TextView) findViewById(R.id.textView_rx);
        tx = (TextView) findViewById(R.id.textView_tx);
        duration = (TextView) findViewById(R.id.textView_duration);
        duration_nsec = (TextView) findViewById(R.id.textView_duration_nsec);
        rx_errors = (TextView) findViewById(R.id.textView_rx_errors);
        ipstat = (TextView) findViewById(R.id.textView_ipstat);
        button_backToHost = (Button) findViewById(R.id.button_backToHost);
    }

    private void setListeners() {
        button_backToHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setThread() {
        thread_getHostProperty = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //等待連線
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                            if (controllerSocket.failConnected()) {
                                Toast.makeText(HostPropertyActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        //傳送請求
                        controllerSocket.sendEncryptedMsg("GET /v1.0/traffic/hosts/" + host.ID + "/" + host.port);
                        //接收回復
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        String[] temp = msg.split("\n");
                        if (temp.length > 2 && temp[0].equals("host_traffic") && temp[temp.length - 1].equals("/host_traffic")) {
                            final String[] temp2 = temp[1].split(" ");
                            if (property == null) {
                                getProperty(temp2);
                            } else if (!property.equals(new Property(temp2[0], temp2[1], temp2[2], temp2[3], temp2[4], temp2[5], temp2[6]))) {
                                getProperty(temp2);
                            }
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(HostPropertyActivity.this, "取得資料錯誤", Toast.LENGTH_SHORT).show();
                                }
                            });
                            throw new Exception();
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        controllerSocket.disconnection();
                        break;
                    }
                }
            }
        });
    }

    private void getProperty(String temp2[]) {
        property = new Property(temp2[0], temp2[1], temp2[2], temp2[3], temp2[4], temp2[5], temp2[6]);
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             speed.setText("speed: " + property.speed);
                             tx.setText("rx" + property.rx);
                             tx.setText("tx: " + property.tx);
                             duration.setText("duration: " + property.duration);
                             duration_nsec.setText("duration_nsec: " + property.duration_nsec);
                             rx_errors.setText("rx_errors: " + property.rx_errors);
                             ipstat.setText("ipstat: " + property.ipstat);
                         }
                     }
        );
    }

    private class Property {
        String speed = "";
        String rx = "";
        String tx = "";
        String duration = "";
        String duration_nsec = "";
        String rx_errors = "";
        String ipstat = "";

        public Property(String speed, String rx, String tx, String duration, String duration_nsec, String rx_errors, String ipstat) {
            this.speed = speed;
            this.rx = rx;
            this.tx = tx;
            this.duration = duration;
            this.duration_nsec = duration_nsec;
            this.rx_errors = rx_errors;
            this.ipstat = ipstat;
        }
    }


}
