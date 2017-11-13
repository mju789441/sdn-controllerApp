package com.example.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Kuo Wei Lun on 2017/11/13.
 */

public class Watch_pktActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_pkt_layout);
        Bundle bundle = this.getIntent().getExtras();
        ControllerIP controllerIP = null;
        /*controllerIP.sendMsg("watch_pkt");
        final TextView textView = (TextView) findViewById(R.id.msg);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String str = controllerIP.getMsg();
                        if (str != null) {
                            textView.setText(textView.getText().toString() + str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/
    }
}
