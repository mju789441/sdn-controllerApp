package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostStatsActivity extends AppCompatActivity {

    //Component
    private View activityView;
    private String IP;
    private String switch_ID;
    private TextView switchID;
    private TextView port;
    private TextView mac;
    private TextView curr_speed;
    private TextView rx;
    private TextView tx;
    private TextView duration;
    private TextView duration_nsec;
    private TextView rx_errors;
    private TextView ipstat;
    private Button button_backToHost;
    private Host host;
    private ControllerURLConnection controllerURLConnection;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getHostProperty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityView = HostStatsActivity.this.getLayoutInflater().inflate(R.layout.layout_hoststats, null);
        setContentView(activityView);
        Bundle bundle = this.getIntent().getExtras();
        IP = bundle.getString("controller.IP");
        switch_ID = bundle.getString("switch.ID");
        host = (Host) bundle.getSerializable("host");
        controllerURLConnection = new ControllerURLConnection(IP);
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        thread_getHostProperty.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getHostProperty.interrupt();
    }

    private void initView() {
        switchID = (TextView) findViewById(R.id.textView_switchID);
        switchID.setText(switchID.getText() + host.ID);
        port = (TextView) findViewById(R.id.textView_port);
        port.setText(port.getText() + host.port);
        mac = (TextView) findViewById(R.id.textView_mac);
        mac.setText(mac.getText() + host.mac);
        curr_speed = (TextView) findViewById(R.id.textView_curr_speed);
        curr_speed.setText(curr_speed.getText() + host.curr_speed);
        rx = (TextView) findViewById(R.id.textView_rx);
        tx = (TextView) findViewById(R.id.textView_tx);
        duration = (TextView) findViewById(R.id.textView_duration);
        duration_nsec = (TextView) findViewById(R.id.textView_duration_nsec);
        rx_errors = (TextView) findViewById(R.id.textView_rx_errors);
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
                try {
                    if (host.port != "LOCAL") {
                        JSONObject hostStats = controllerURLConnection.getPortStats(switch_ID + "/" + host.port);
                        setStats(hostStats.getJSONArray(switch_ID).getJSONObject(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setStats(JSONObject hostStats) {
        final Stats stats = new Stats(hostStats);
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             rx.setText("rx:" + stats.rx);
                             tx.setText("tx:" + stats.tx);
                             duration.setText("duration:" + stats.duration);
                             duration_nsec.setText("duration_nsec:" + stats.duration_nsec);
                             rx_errors.setText("rx_errors:" + stats.rx_errors);
                         }
                     }
        );
    }

    private class Stats {
        String rx = null;
        String tx = null;
        String duration = null;
        String duration_nsec = null;
        String rx_errors = null;

        public Stats(JSONObject hostStats) {
            try {
                rx = hostStats.getString("rx_bytes");
                tx = hostStats.getString("tx_bytes");
                duration = hostStats.getString("duration_sec");
                duration_nsec = hostStats.getString("duration_nsec");
                rx_errors = hostStats.getString("rx_errors");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
