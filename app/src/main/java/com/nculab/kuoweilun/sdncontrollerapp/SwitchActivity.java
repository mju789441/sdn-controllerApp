package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
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

    ControllerSocket controllerSocket;
    ListView listView;
    private ArrayList<Switch> list;
    private SwitchAdapter adapter;
    private ArrayList<SwitchID> switchID;
    Button button_backToController;
    //Handler
    Handler handler = new Handler();
    //Thread
    public Thread thread_getSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_switchlist);
        Bundle bundle = this.getIntent().getExtras();
        String IP = bundle.getString("controller.IP");
        controllerSocket = new ControllerSocket(IP, SwitchActivity.this);
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        thread_getSwitch.start();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.list_switch);
        list = new ArrayList<Switch>();
        adapter = new SwitchAdapter(SwitchActivity.this, list);
        listView.setAdapter(adapter);
        switchID = new ArrayList<SwitchID>();
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
                        Button back;
                        switch (item.getItemId()) {
                            case R.id.watch_host:
                                thread_getSwitch.interrupt();
                                controllerSocket.thread_connect.interrupt();
                                controllerSocket.reset();
                                Intent intent = new Intent();
                                intent.setClass(SwitchActivity.this, HostActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("controller.IP", controllerSocket.IP);
                                bundle.putString("switchID", getSwitch.ID);
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
                thread_getSwitch.interrupt();
                controllerSocket.thread_connect.interrupt();
                controllerSocket.reset();
                finish();
            }
        });
    }

    private void setThread() {
        thread_getSwitch = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                            if (controllerSocket.failConnected()) {
                                Toast.makeText(SwitchActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        controllerSocket.sendEncryptedMsg("GET switch -ID -bytes");
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        // 接收訊息
                        String[] temp = msg.split("\n");
                        if (temp[0].equals("switch_speed") && temp[temp.length - 1].equals("/switch_speed")) {
                            boolean switchChanged = false;
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (!switchID.contains(new SwitchID(false, temp2[0]))) {
                                    switchChanged = true;
                                    switchID.add(new SwitchID(true, temp2[0]));
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.add(new Switch(temp2[0], temp2[temp2.length - 1]));
                                        }
                                    });
                                } else {
                                    int index = switchID.indexOf(new SwitchID(false, temp2[0]));
                                    switchID.get(index).alive = true;
                                    String flow = list.get(index).flow;
                                    if (flow == temp2[temp2.length - 1]) {
                                        switchChanged = true;
                                        list.get(index).flow = temp2[temp2.length - 1];
                                    }
                                }
                            }
                            //判斷,Host是否還存在
                            for (int i = switchID.size() - 1; i >= 0; i--) {
                                if (switchID.get(i).alive == false) {
                                    switchChanged = true;
                                    switchID.remove(i);
                                    list.remove(i);
                                } else {
                                    switchID.get(i).alive = false;
                                }
                            }
                            if (switchChanged) {
                                switchChanged = false;
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

    private class SwitchID {
        boolean alive = false;
        String ID;

        SwitchID(boolean alive, String ID) {
            this.alive = alive;
            this.ID = ID;
        }

    }

}
