package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class SwitchActivity extends AppCompatActivity {

    //Component
    private ControllerSocket controllerSocket;
    private ListView listView;
    private ArrayList<Switch> list;
    private SwitchAdapter adapter;
    private Button button_backToController;
    //Handler
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_switchlist);
        Bundle bundle = this.getIntent().getExtras();
        String IP = bundle.getString("controller.IP");
        initView();
        setListeners();
        controllerSocket = new ControllerSocket(IP, SwitchActivity.this, list, adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        controllerSocket.thread_getSwitch.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        controllerSocket.thread_getSwitch.interrupt();
        controllerSocket.thread_connect.interrupt();
        controllerSocket.reset();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView_switch);
        list = new ArrayList<Switch>();
        adapter = new SwitchAdapter(SwitchActivity.this, list);
        listView.setAdapter(adapter);
        button_backToController = (Button) findViewById(R.id.button_backToController);
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                adapter.notifyDataSetChanged();
                final Switch getSwitch = (Switch) adapter.getItem(position);
                PopupMenu popupmenu = new PopupMenu(SwitchActivity.this, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_switch, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        switch (item.getItemId()) {
                            case R.id.watch_host:
                                intent.setClass(SwitchActivity.this, HostActivity.class);
                                bundle.putString("controller.IP", controllerSocket.IP);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            case R.id.watch_flow:
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupmenu.show();
            }

        });

        button_backToController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
