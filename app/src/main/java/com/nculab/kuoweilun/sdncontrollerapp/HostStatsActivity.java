package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostStatsActivity extends AppCompatActivity {

    //Component
    private View activityView;
    private String connect_IP;
    private String switch_ID;
    private TableLayout tableLayout;
    private Button button_backToHost;
    private Host host;
    private String stat = "unban";
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
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
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

    private void setRunnable() {
        runnable_getHostProperty = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!host.port.equals("LOCAL")) {
                        JSONObject hostStats = controllerURLConnection
                                .getPortStats(switch_ID + "/" + host.port)
                                .getJSONArray(switch_ID).getJSONObject(0);
                        JSONObject input = new JSONObject();
                        stat = "unban";
                        try {
                            input.put("priority", 867);
                            input.put("math", new JSONObject().put("in_port", host.port));
                            System.out.println("switch_id " + switch_ID);
                            System.out.println("host " + host.port);
                            JSONArray output = controllerURLConnection.getFlowStats(switch_ID, input.toString())
                                    .getJSONArray(switch_ID);
                            for (int i = 0; i < output.length(); i++) {
                                if (output.getJSONObject(i).getJSONArray("actions")
                                        .equals(new JSONArray()))
                                    stat = "ban";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        final Context context = this;
        final ArrayList<String> name = new ArrayList<String>();
        final ArrayList<String> text = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    name.add("ID");
                    text.add(host.ID);
                    break;
                case 1:
                    name.add("port");
                    text.add(host.port);
                    break;
                case 2:
                    name.add("mac");
                    text.add(host.mac);
                    break;
                case 3:
                    name.add("speed");
                    text.add(host.speed);
                    break;
            }
        }
        final JSONArray names = hostStats.names();
        for (int i = 0; i < names.length(); i++) {
            try {
                name.add(names.getString(i));
                text.add(hostStats.getString(names.getString(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= name.size(); i++) {
                    //component
                    TableRow tableRow = new TableRow(context);
                    TextView textView = new TextView(context);
                    TextView textView1 = new TextView(context);
                    View view = new View(context);
                    //setting
                    view.setBackgroundColor(Color.GRAY);
                    view.setMinimumHeight(2);
                    //margin
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
                    layoutParams.setMargins(50, 0, 50, 0);
                    textView.setLayoutParams(layoutParams);
                    textView1.setLayoutParams(layoutParams);
                    textView.setTextSize(20);
                    textView1.setTextSize(20);
                    textView1.setGravity(Gravity.RIGHT);
                    if (i == name.size()) {
                        textView.setText("Ban Stat: ");
                        textView1.setText(stat);
                    } else {
                        textView.setText(name.get(i) + ": ");
                        textView1.setText(text.get(i));
                    }
                    //addView
                    if (i != 0)
                        tableLayout.addView(view);
                    tableRow.addView(textView);
                    tableRow.addView(textView1);
                    tableLayout.addView(tableRow);
                }
            }
        });
    }

}
