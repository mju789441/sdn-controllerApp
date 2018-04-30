package com.nculab.kuoweilun.sdncontrollerapp.topology;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.Subscribe;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;
import com.nculab.kuoweilun.sdncontrollerapp.database.FlowWarn_table;
import com.nculab.kuoweilun.sdncontrollerapp.database.UUID_table;

import org.json.JSONArray;
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
    private String connect_URL;
    private String switch_ID;
    private String port_no;
    private AppFile appFile = new AppFile(this);
    private ControllerURLConnection controllerURLConnection;
    private Subscribe subscribe;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_flowwarning);
        Bundle bundle = this.getIntent().getExtras();
        connect_URL = bundle.getString("connect_URL");
        switch_ID = bundle.getString("switch_ID");
        port_no = bundle.getString("port_no");
        controllerURLConnection = new ControllerURLConnection(connect_URL);
        subscribe = new Subscribe(this, controllerURLConnection);
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
        //content資料
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray contentArray = controllerURLConnection.getTopologySwitch(switch_ID)
                            .getJSONObject(0).getJSONArray("ports");
                    for (int i = 0; i < contentArray.length(); i++) {
                        if (Integer.valueOf(contentArray.getJSONObject(i).getString("port_no"))
                                .equals(Integer.valueOf(port_no))) {
                            JSONObject content = contentArray.getJSONObject(i);
                            String content_msg = "hw_addr: " + content.getString("hw_addr") + "\n";
                            content_msg += "name: " + content.getString("name") + "\n";
                            content_msg += "port_no: " + content.getString("port_no") + "\n";
                            content_msg += "dpid: " + content.getString("dpid") + "\n";
                            final String msg = content_msg;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textView_content.setText(msg);
                                }
                            });
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setListeners() {
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int speed = Integer.valueOf(editText_speed.getText().toString());
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
                    if (sec == 0) {
                        toast_msg += "time must > 0\n";
                    }
                    if (speed == 0) {
                        toast_msg += "flow must > 0\n";
                    }
                    if (!toast_msg.equals("")) {
                        Toast.makeText(getApplicationContext(), toast_msg, Toast.LENGTH_SHORT).show();
                    } else {
                        UUID_table uuid_table = new UUID_table(getApplicationContext());
                        FlowWarn_table flowWarn_table = new FlowWarn_table(getApplicationContext());
                        //不看URL 檢查相符的UUID
                        JSONArray uuidArray = flowWarn_table.getUUID(switch_ID, port_no, "" + speed, "" + sec);
                        if (uuidArray.length() == 0) {
                            //新增資料
                            JSONObject item = new JSONObject();
                            String uuid = UUID.randomUUID().toString();
                            item.put(UUID_table.URL_COLUMN, connect_URL)
                                    .put(UUID_table.UUID_COLUMN, uuid)
                                    .put(UUID_table.EVENT_COLUMN, UUID_table.EVENT_FLOWWARN);
                            uuid_table.insert(item);
                            JSONObject flowWarn = new JSONObject();
                            flowWarn.put(FlowWarn_table.UUID_COLUMN, uuid)
                                    .put(FlowWarn_table.SWITCH_ID_COLUMN, switch_ID)
                                    .put(FlowWarn_table.PORT_NO_COLUMN, port_no)
                                    .put(FlowWarn_table.SPEED_COLUMN, "" + speed)
                                    .put(FlowWarn_table.DURATION_COLUMN, "" + sec);
                            flowWarn_table.insert(flowWarn);
                        } else {
                            //查詢符合URL的資料 理論上只有一個
                            JSONArray valid_uuidArray = new JSONArray();
                            for (int i = 0; i < uuidArray.length(); i++) {
                                if (uuid_table.checkUrl(connect_URL, uuidArray.getString(i)))
                                    valid_uuidArray.put(uuidArray.getString(i));
                            }
                            //無相符URL
                            if (valid_uuidArray.length() == 0) {
                                //新增資料
                                JSONObject item = new JSONObject();
                                String uuid = UUID.randomUUID().toString();
                                item.put(UUID_table.URL_COLUMN, connect_URL)
                                        .put(UUID_table.UUID_COLUMN, uuid)
                                        .put(UUID_table.EVENT_COLUMN, UUID_table.EVENT_FLOWWARN);
                                uuid_table.insert(item);
                                JSONObject flowWarn = new JSONObject();
                                flowWarn.put(FlowWarn_table.UUID_COLUMN, uuid)
                                        .put(FlowWarn_table.SWITCH_ID_COLUMN, switch_ID)
                                        .put(FlowWarn_table.PORT_NO_COLUMN, port_no)
                                        .put(FlowWarn_table.SPEED_COLUMN, "" + speed)
                                        .put(FlowWarn_table.DURATION_COLUMN, "" + sec);
                                flowWarn_table.insert(flowWarn);
                            }
                            uuid_table.close();
                            flowWarn_table.close();
                            subscribe.subscrbe();
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
