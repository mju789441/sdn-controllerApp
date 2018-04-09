package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostStatsActivity extends AppCompatActivity {

    //Component
    private View activityView;
    private String connect_IP;
    private String switch_ID;
    private TextView textView_content;
    private Button button_backToHost;
    private Host host;
    private String hostCotent;
    private ControllerURLConnection controllerURLConnection;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Runnable runnable_getHostProperty;
    private Thread thread_getHostProperty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityView = HostStatsActivity.this.getLayoutInflater().inflate(R.layout.layout_hoststats, null);
        setContentView(activityView);
        Bundle bundle = this.getIntent().getExtras();
        connect_IP = bundle.getString("controller_IP");
        switch_ID = bundle.getString("switch_ID");
        host = (Host) bundle.getSerializable("host");
        controllerURLConnection = new ControllerURLConnection(connect_IP);
        initView();
        setListeners();
        setRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        thread_getHostProperty = new Thread(runnable_getHostProperty);
        thread_getHostProperty.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getHostProperty.interrupt();
    }

    private void initView() {
        textView_content = (TextView) findViewById(R.id.textView_content);
        button_backToHost = (Button) findViewById(R.id.button_backToHost);
        hostCotent = "ID: " + host.ID + "\nport: " + host.port + "\nmac: " + host.mac +
                "\nspeed: " + host.speed + "\n";
    }

    private void setListeners() {
        button_backToHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setRunnable() {
        runnable_getHostProperty = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!host.port.equals( "LOCAL")) {
                        JSONObject hostStats = controllerURLConnection
                                .getPortStats(switch_ID + "/" + host.port)
                                .getJSONArray(switch_ID).getJSONObject(0);
                        setStats(hostStats);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void setStats(final JSONObject hostStats) {
        String temp = "";
        JSONArray names = hostStats.names();
        for (int i = 0; i < names.length(); i++) {
            try {
                temp += names.getString(i) + ": " + hostStats.getString(names.getString(i)) + "\n";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final String stats = temp;
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             textView_content.setText(hostCotent + stats);
                         }
                     }
        );
    }

}
