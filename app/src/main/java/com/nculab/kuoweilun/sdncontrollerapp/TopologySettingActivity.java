package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kuo on 2018/3/20.
 */

public class TopologySettingActivity extends AppCompatActivity {

    private android.widget.Switch switch_online;
    private android.widget.Switch switch_flowError;
    private Boolean online = true;
    private Boolean flowError = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_topologysettings);
        initView();
        settListeners();
    }

    private void initView(){
        switch_online = (android.widget.Switch)findViewById(R.id.switch_online);
        switch_online.setChecked(online);
        switch_flowError = (android.widget.Switch) findViewById(R.id.switch_flowError);
        switch_flowError.setChecked(flowError);
    }

    private void settListeners(){

    }
}
