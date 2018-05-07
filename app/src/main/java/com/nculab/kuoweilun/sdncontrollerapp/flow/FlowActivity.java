package com.nculab.kuoweilun.sdncontrollerapp.flow;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;
import com.nculab.kuoweilun.sdncontrollerapp.host.Host;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_flow);
        Bundle bundle = this.getIntent().getExtras();
        connect_URL = bundle.getString("controller_URL");
        switch_ID = bundle.getString("switch_ID");
        host = (Host) bundle.getSerializable("host");
        controllerURLConnection = new ControllerURLConnection(connect_URL);
        initView();
        setListeners();
    }

    private void initView() {
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = new Date();
                    date.setTime(date.getTime() + 12 * 60 * 60 * 1000);
                    String date2 = simpleDateFormat.format(date).replace(' ', 'T');
                    date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
                    String date1 = simpleDateFormat.format(date).replace(' ', 'T');
                    JSONObject input = new JSONObject()
                            .put("date", date1 + " " + date2)
                            .put("precision", "1")
                            .put("switch_id", switch_ID)
                            .put("port_no", host.port);
                    final JSONObject output = controllerURLConnection.getDBflow(input.toString());
                    System.out.println(output.toString());
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

}
