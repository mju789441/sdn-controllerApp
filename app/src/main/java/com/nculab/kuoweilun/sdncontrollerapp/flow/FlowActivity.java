package com.nculab.kuoweilun.sdncontrollerapp.flow;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;
import com.nculab.kuoweilun.sdncontrollerapp.host.Host;
import com.nculab.kuoweilun.sdncontrollerapp.topology.FlowWarningActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FlowActivity extends AppCompatActivity {

    private String connect_URL;
    private String switch_ID;
    private Host host;
    private ControllerURLConnection controllerURLConnection;
    private Handler handler = new Handler();
    private LinearLayout linearLayout;
    private Thread thread_getFlow;
    private Runnable runnable_getFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_flow);
        Bundle bundle = this.getIntent().getExtras();
        connect_URL = bundle.getString("controller_URL");
        switch_ID = bundle.getString("switch_ID");
        host = (Host) bundle.getSerializable("host");
        controllerURLConnection = new ControllerURLConnection(connect_URL, this);
        initView();
        setListeners();
        setRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            controllerURLConnection.ssid = new AppFile(this).getSSID();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread_getFlow = new Thread(runnable_getFlow);;
        thread_getFlow.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getFlow.interrupt();
    }


    private void initView() {
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = new Date();
                    date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
                    String date2 = simpleDateFormat.format(date).replace(' ', 'T');
                    date.setTime(date.getTime() - 48 * 60 * 60 * 1000);
                    String date1 = simpleDateFormat.format(date).replace(' ', 'T');
                    JSONObject input = new JSONObject()
                            .put("date", date1 + " " + date2)
                            .put("precision", "60")
                            .put("switch_id", switch_ID)
                            .put("port_no", host.port);
                    final JSONObject output = controllerURLConnection.getDBflow(input);
                    Log.d("DBflow: ", output.toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.addView(new ChartView(getApplicationContext(), output).getView());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setListeners() {

    }

    private void setRunnable() {
        runnable_getFlow = new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = new Date();
                    date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
                    String date2 = simpleDateFormat.format(date).replace(' ', 'T');
                    date.setTime(date.getTime() - 48 * 60 * 60 * 1000);
                    String date1 = simpleDateFormat.format(date).replace(' ', 'T');
                    JSONObject input = new JSONObject()
                            .put("date", date1 + " " + date2)
                            .put("precision", "60")
                            .put("switch_id", switch_ID)
                            .put("port_no", host.port);
                    final JSONObject output = controllerURLConnection.getDBflow(input);
                    Log.d("DBflow: ", output.toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.addView(new ChartView(getApplicationContext(), output).getView());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
