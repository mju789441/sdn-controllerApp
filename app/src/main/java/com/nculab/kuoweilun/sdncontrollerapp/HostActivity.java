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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostActivity extends AppCompatActivity {

    //Component
    private View activityView;
    private ListView listView;
    private ArrayList<Host> list;
    private HostAdapter adapter;
    private Button button_backToSwitch;
    private ControllerURLConnection controllerURLConnection;
    private String controller_IP;
    private String switch_ID;
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
        controller_IP = bundle.getString("controller.IP");
        switch_ID = bundle.getString("switch.ID");
        controllerURLConnection = new ControllerURLConnection(controller_IP);
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        thread_getHost.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getHost.interrupt();
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
                                intent.setClass(HostActivity.this, HostStatsActivity.class);
                                bundle.putString("controller.IP", controller_IP);
                                bundle.putString("switch.ID", switch_ID);
                                bundle.putSerializable("host", host);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            case R.id.ban:
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JSONObject flow = new JSONObject();
                                        try {
                                            flow.put("dpid", switch_ID);
                                            flow.put("priority", "867");
                                            flow.put("match", new JSONObject()
                                                    .put("in_port", host.port));
                                            flow.put("actions", new JSONArray("[]"));
                                            controllerURLConnection.addFlowEntry(flow.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                                break;
                            case R.id.unban:
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JSONObject flow = new JSONObject();
                                        try {
                                            flow.put("dpid", switch_ID);
                                            flow.put("priority", "867");
                                            flow.put("match", new JSONObject()
                                                    .put("in_port", host.port));
                                            flow.put("actions", new JSONArray("[]"));
                                            controllerURLConnection.deleteFlowEntry(flow.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
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

    private void setThread() {
        thread_getHost = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        JSONArray host = controllerURLConnection.getPortDesc(switch_ID).getJSONArray(switch_ID);
                        int host_length = host.length() - 1;
                        for (int i = host_length; i < list.size(); i++) {
                            list.remove(i);
                        }
                        for (int i = 0; i < host_length; i++) {
                            JSONObject hostObject = host.getJSONObject(i + 1);
                            if (list.size() < i + 1) {
                                list.add(new Host(switch_ID, hostObject));
                            } else {
                                list.set(i, new Host(switch_ID, hostObject));
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
