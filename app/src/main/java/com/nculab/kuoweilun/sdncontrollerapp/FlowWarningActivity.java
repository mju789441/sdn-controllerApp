package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by kuo on 2018/3/19.
 */

public class FlowWarningActivity extends AppCompatActivity {

    //component
    private EditText editText_speed;
    private EditText editText_day;
    private EditText editText_hour;
    private EditText editText_min;
    private TextView textView_content;
    private Button button_submit;
    private String connect_IP;
    private String switch_ID;
    private String port_no;
    private AppFile appFile = new AppFile(this);
    private ControllerURLConnection controllerURLConnection;
    private Subscribe subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_flowwarning);
        Bundle bundle = this.getIntent().getExtras();
        connect_IP = bundle.getString("connect_IP");
        switch_ID = bundle.getString("switch_ID");
        port_no = bundle.getString("port_no");
        controllerURLConnection = new ControllerURLConnection(connect_IP);
        subscribe = new Subscribe(appFile, controllerURLConnection);
        initView();
        setListeners();
    }

    private void initView() {
        editText_speed = (EditText) findViewById(R.id.editText_speed);
        editText_day = (EditText) findViewById(R.id.editText_day);
        editText_hour = (EditText) findViewById(R.id.editText_hour);
        editText_min = (EditText) findViewById(R.id.editText_min);
        textView_content = (TextView) findViewById(R.id.textView_content);
        button_submit = (Button) findViewById(R.id.button_submit);
    }

    private void setListeners() {
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speed = editText_speed.getText().toString();
                int day = Integer.valueOf(editText_day.getText().toString());
                int hour = Integer.valueOf(editText_hour.getText().toString());
                int min = Integer.valueOf(editText_min.getText().toString());
                int sec = ((day * 24 + hour) * 60 + min) * 60;
                String toast_msg = "";
                if (hour >= 24) {
                    toast_msg += "hour >= 24\n";
                }
                if (min >= 60) {
                    toast_msg += "min >= 60\n";
                }
                if (!toast_msg.equals("")) {
                    Toast.makeText(getApplicationContext(), toast_msg, Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject flow_warning = null;
                    try {
                        flow_warning = appFile.getFlowWarning();
                        flow_warning.put(switch_ID, new JSONObject().put(port_no,
                                new JSONObject().put("uuid", UUID.randomUUID().toString())
                                        .put("speed", speed)
                                        .put("duration", sec)));
                        appFile.saveFlowWarning(flow_warning);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    subscribe.subscrbe();
                }
            }
        });
    }

}
