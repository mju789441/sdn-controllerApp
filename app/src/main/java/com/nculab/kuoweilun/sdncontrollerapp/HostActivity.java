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

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostActivity extends AppCompatActivity {

    //Component
    private View activityView;
    private ControllerSocket controllerSocket;
    private ListView listView;
    private ArrayList<Host> list;
    private HostAdapter adapter;
    private Button button_backToSwitch;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityView = HostActivity.this.getLayoutInflater().inflate(R.layout.layout_hostlist, null);
        setContentView(activityView);
        Bundle bundle = this.getIntent().getExtras();
        String connect_IP = bundle.getString("controller.IP");
        initView();
        setListeners();
        controllerSocket = new ControllerSocket(connect_IP, HostActivity.this, list, adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        controllerSocket.thread_getHost.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        controllerSocket.thread_getHost.interrupt();
        controllerSocket.thread_connect.interrupt();
        controllerSocket.reset();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView_host);
        list = new ArrayList<Host>();
        adapter = new HostAdapter(HostActivity.this, list);
        listView.setAdapter(adapter);
        button_backToSwitch = (Button) findViewById(R.id.button_backToSwitch);
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                final Host host = (Host) adapter.getItem(position);
                PopupMenu popupmenu = new PopupMenu(HostActivity.this, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_host, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        switch (item.getItemId()) {
                            case R.id.watch_property:
                                intent.setClass(HostActivity.this, HostPropertyActivity.class);
                                bundle.putString("controller.IP", controllerSocket.IP);
                                bundle.putSerializable("host", host);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            case R.id.Ban_IP:
                                controllerSocket.ban(host);
                                break;
                            case R.id.unBan_IP:
                                controllerSocket.unban(host);
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

        button_backToSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
