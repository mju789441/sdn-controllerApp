package com.nculab.kuoweilun.sdncontrollerapp;

import android.app.FragmentBreadCrumbs;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostActivity extends AppCompatActivity {

    View activityView;
    ControllerSocket controllerSocket;
    String switchID;
    ListView listView;
    private ArrayList<Host> list;
    private HostAdapter adapter;
    private ArrayList<Port_mac> HostMac;
    Button button_backToSwitch;
    //Handler
    Handler handler = new Handler();
    //Thread
    public Thread thread_getHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityView = HostActivity.this.getLayoutInflater().inflate(R.layout.layout_hostlist, null);
        setContentView(activityView);
        Bundle bundle = this.getIntent().getExtras();
        String IP = bundle.getString("controller.IP");
        switchID = bundle.getString("switchID");
        controllerSocket = new ControllerSocket(IP, HostActivity.this);
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


    private void initView() {
        listView = (ListView) findViewById(R.id.list_host);
        list = new ArrayList<Host>();
        adapter = new HostAdapter(HostActivity.this, list);
        listView.setAdapter(adapter);
        HostMac = new ArrayList<Port_mac>();
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
                                //換View
                                View view_temp = HostActivity.this.getLayoutInflater().inflate(R.layout.layout_hostproperty, null);
                                setContentView(view_temp);
                                //取得Id
                                TextView ID = (TextView) view_temp.findViewById(R.id.textViewSwitchID);
                                TextView port = (TextView) view_temp.findViewById(R.id.textViewPort);
                                TextView mac = (TextView) view_temp.findViewById(R.id.textViewMac);
                                TextView IP = (TextView) view_temp.findViewById(R.id.textViewIP);
                                Button backToHost = (Button) view_temp.findViewById(R.id.button_backToHost);
                                //設定Text
                                ID.setText(ID.getText() + host.ID);
                                port.setText(port.getText() + host.port);
                                mac.setText(mac.getText() + host.mac);
                                IP.setText(IP.getText() + host.IP);
                                //返回原先ViewView
                                backToHost.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        setContentView(activityView);
                                    }
                                });
                                break;
                            case R.id.Ban_IP:
                                if (host.IP == "None") {
                                    break;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        controllerSocket.sendEncryptedMsg("POST /ban/" + host.IP);
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
                thread_getHost.interrupt();
                controllerSocket.thread_connect.interrupt();
                controllerSocket.reset();
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
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                            if (controllerSocket.failConnected()) {
                                Toast.makeText(HostActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        controllerSocket.sendEncryptedMsg("GET /v1.0/topology/hosts/");
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        // 接收訊息
                        String[] temp = msg.split("\n");
                        if (temp[0].equals("host") && temp[temp.length - 1].equals("/host") && temp.length != 2) {
                            boolean hostChanged = false;
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (!HostMac.contains(new Port_mac(false, temp2[2], temp2[3]))) {
                                    hostChanged = true;
                                    HostMac.add(new Port_mac(true, temp2[2], temp2[3]));
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.add(new Host(temp2[0], temp2[1], temp2[2], temp2[temp2.length - 1]));
                                        }
                                    });
                                } else {
                                    HostMac.get(HostMac.indexOf(new Port_mac(false, temp2[2], temp2[3]))).alive = true;
                                }
                            }
                            //判斷,Host是否還存在
                            for (int i = HostMac.size() - 1; i >= 0; i--) {
                                if (HostMac.get(i).alive == false) {
                                    hostChanged = true;
                                    HostMac.remove(i);
                                    list.remove(i);
                                } else {
                                    HostMac.get(i).alive = false;
                                }
                            }
                            if (hostChanged) {
                                hostChanged = false;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } else {
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

    private class Port_mac {
        boolean alive = false;
        String port;
        String mac;

        Port_mac(boolean alive, String port, String mac) {
            this.alive = alive;
            this.port = port;
            this.mac = mac;
        }

    }

}
