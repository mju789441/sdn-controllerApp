package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class SwitchActivity extends AppCompatActivity {

    //Component
    private ControllerSocket controllerSocket;
    private ListView listView;
    private String IP;
    private ArrayList<Switch> list;
    private SwitchAdapter adapter;
    private Button button_backToController;
    private ControllerURLConnection controllerURLConnection;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_switchlist);
        Bundle bundle = this.getIntent().getExtras();
        IP = bundle.getString("controller.IP");
        controllerURLConnection = new ControllerURLConnection(IP);
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        thread_getSwitch.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getSwitch.interrupt();
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
                                bundle.putString("controller.IP", IP);
                                bundle.putString("switch.ID", getSwitch.ID);
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

    private void setThread() {
        thread_getSwitch = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        JSONArray switchID = controllerURLConnection.getAllSwitch();
                        for (int i = switchID.length(); i < list.size(); i++) {
                            list.remove(i);
                        }
                        for (int i = 0; i < switchID.length(); i++) {
                            String dpid = String.valueOf(switchID.getInt(i));
                            JSONObject switchObject = controllerURLConnection.getPortDesc(dpid);
                            if (list.size() < i + 1) {
                                list.add(new Switch(String.valueOf(switchID.getInt(i)), switchObject));
                            } else {
                                list.set(i, new Switch(String.valueOf(switchID.getInt(i)), switchObject));
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        Thread.sleep(100);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
