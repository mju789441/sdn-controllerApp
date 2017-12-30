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
        controllerSocket = new ControllerSocket(connect_IP, HostActivity.this);
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        thread_getHost.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getHost.interrupt();
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
                                if (host.IP == "None") {
                                    break;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        thread_getHost.interrupt();
                                        controllerSocket.sendEncryptedMsg("POST /ban/" + host.IP);
                                        try {
                                            controllerSocket.getMsg();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        thread_getHost.start();
                                    }
                                }).start();
                                break;
                            case R.id.unBan_IP:
                                if (host.IP == "None") {
                                    break;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        thread_getHost.interrupt();
                                        controllerSocket.sendEncryptedMsg("POST /unban/" + host.IP);
                                        try {
                                            controllerSocket.getMsg();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        thread_getHost.start();
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
                while (true) {
                    try {
                        //等待連線
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                            if (controllerSocket.failConnected()) {
                                Toast.makeText(HostActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        //傳送請求
                        controllerSocket.sendEncryptedMsg("GET /v1.0/topology/hosts/");
                        //接收回復
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        String[] temp = msg.split("\n");
                        if (temp.length > 2 && temp[0].equals("host") && temp[temp.length - 1].equals("/host")) {
                            boolean hostChanged = false;
                            if (list.size() > temp.length - 2) {
                                hostChanged = true;
                                for (int i = temp.length - 2; i < list.size(); i++) {
                                    list.remove(i);
                                }
                            }
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (list.size() < temp.length - 2) {
                                    hostChanged = true;
                                    list.add(new Host(temp2[0], temp2[1], temp2[2], temp2[temp2.length - 1]));
                                } else if (!list.get(i - 1).equals(new Host(temp2[0], temp2[1], temp2[2], temp2[temp2.length - 1]))) {
                                    hostChanged = true;
                                    list.set(i - 1, new Host(temp2[0], temp2[1], temp2[2], temp2[temp2.length - 1]));
                                }
                            }
                            if (hostChanged) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(HostActivity.this, "取得資料錯誤", Toast.LENGTH_SHORT).show();
                                }
                            });
                            throw new Exception();
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        controllerSocket.disconnection();
                        break;
                    }
                }
            }
        });
    }

}
