package com.nculab.kuoweilun.sdncontrollerapp.topology;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.Subscribe;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by kuo on 2018/3/20.
 */

public class TopologySettingActivity extends AppCompatActivity {

    private String connect_URL;
    private android.widget.Switch switch_online;
    private android.widget.Switch switch_flow_warning;
    private Button button_backToTopology;
    private JSONObject setting;
    private AppFile appFile = new AppFile(this);
    private Subscribe subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_topologysettings);
        Bundle bundle = this.getIntent().getExtras();
        connect_URL = bundle.getString("controller_URL");
        subscribe = new Subscribe(this, new ControllerURLConnection(connect_URL));
        initView();
        settListeners();
    }

    private void initView() {
        try {
            setting = appFile.getSetting();
        } catch (Exception e) {
            e.printStackTrace();
            setting = new JSONObject();
            try {
                setting.put("swich_online", true);
                setting.put("flow_warning", true);
                appFile.saveSetting(setting);
                subscribe.subscrbe();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        switch_online = (android.widget.Switch) findViewById(R.id.switch_online);
        switch_flow_warning = (android.widget.Switch) findViewById(R.id.switch_flow_warning);
        try {
            switch_online.setChecked(setting.getBoolean("swich_online"));
            switch_flow_warning.setChecked(setting.getBoolean("flow_warning"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        button_backToTopology = (Button) findViewById(R.id.button_backToTopology);
    }

    private void settListeners() {
        switch_online.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    setting.put("swich_online", isChecked);
                    appFile.saveSetting(setting);
                    if (isChecked) {
                        subscribe.subscrbe();
                    } else {
                        subscribe.unsubscribe();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        switch_flow_warning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    setting.put("flow_warning", isChecked);
                    appFile.saveSetting(setting);
                    if (isChecked) {
                        subscribe.subscrbe();
                    } else {
                        subscribe.unsubscribe();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        button_backToTopology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
