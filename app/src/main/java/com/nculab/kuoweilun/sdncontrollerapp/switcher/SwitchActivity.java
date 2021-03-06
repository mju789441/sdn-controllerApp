package com.nculab.kuoweilun.sdncontrollerapp.switcher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.Subscribe;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerSocket;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;
import com.nculab.kuoweilun.sdncontrollerapp.database.URL_table;
import com.nculab.kuoweilun.sdncontrollerapp.flow.FlowActivity;
import com.nculab.kuoweilun.sdncontrollerapp.host.Host;
import com.nculab.kuoweilun.sdncontrollerapp.host.HostActivity;

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
    private String connect_URL;
    private ArrayList<Switch> list;
    private SwitchAdapter adapter;
    private Button button_backToController;
    private ControllerURLConnection controllerURLConnection;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getSwitch;
    private Runnable runnable_getSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_switchlist);
        Bundle bundle = this.getIntent().getExtras();
        connect_URL = bundle.getString("controller_URL");
        controllerURLConnection = new ControllerURLConnection(connect_URL, this);
        try {
            new AppFile(this).saveCurrentURL(connect_URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        //Subscribe
        new Subscribe(this, controllerURLConnection).subscrbe();
        thread_getSwitch = new Thread(runnable_getSwitch);
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
                                bundle.putString("controller_URL", connect_URL);
                                bundle.putString("switch_ID", getSwitch.ID);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            case R.id.watch_flow:
                                intent.setClass(SwitchActivity.this, FlowActivity.class);
                                bundle.putString("controller_URL", connect_URL);
                                bundle.putString("switch_ID", getSwitch.ID);
                                bundle.putSerializable("host", new Host());
                                intent.putExtras(bundle);
                                startActivity(intent);
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

    private void setRunnable() {
        runnable_getSwitch = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean first = true;
                    while (true) {
                        JSONObject allSpeed = controllerURLConnection.getAllSpeed();
                        if (first) {
                            first = false;
                            //儲存url
                            URL_table url_table = new URL_table(getApplicationContext());
                            JSONObject item = new JSONObject()
                                    .put(URL_table.URL_COLUMN, connect_URL)
                                    .put(URL_table.TOKEN_COLUMN, FirebaseInstanceId.getInstance().getToken());
                            if (!url_table.update(item))
                                url_table.insert(item);
                            Log.d("url_table: ", url_table.getAll().toString());
                        }
                        for (int i = allSpeed.names().length(); i < list.size(); i++) {
                            list.remove(i);
                        }
                        for (int i = 0; i < allSpeed.names().length(); i++) {
                            String dpid = allSpeed.names().getString(i);
                            JSONObject switchSpeed = allSpeed.getJSONObject(dpid);
                            int speed = 0;
                            for (int j = 1; j <= switchSpeed.length(); j++) {
                                speed += switchSpeed.getInt("" + j);
                            }

                            if (list.size() < i + 1) {
                                list.add(new Switch(dpid, speed));
                            } else {
                                list.set(i, new Switch(dpid, speed));
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
