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
    private ArrayList<String> switchID;
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
        controllerSocket.thread_connect.start();
        initView();
        setListeners();
        setThread();
        thread_getSwitch.start();
    }


    private void initView() {
        listView = (ListView) findViewById(R.id.list_switch);
        list = new ArrayList<Switch>();
        adapter = new SwitchAdapter(SwitchActivity.this, list);
        listView.setAdapter(adapter);
        switchID = new ArrayList<String>();
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
                                controllerSocket.close();
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
                controllerSocket.close();
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
                        //未連線時拋出例外
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                        }
                        controllerSocket.sendEncryptedMsg("GET switch -ID -bytes");
                        final String str = controllerSocket.getMsg();
                        if (str == null) {
                            throw new Exception();
                        }
                        final String msg = controllerSocket.rsa.decrypt(str.getBytes());
                        // 接收訊息
                        String[] temp = msg.split("\n");
                        if (temp[0].equals("switch_speed") && temp[temp.length - 1].equals("/switch_speed")) {
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (switchID.contains(temp2[0])) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.set(switchID.indexOf(temp2[0]), new Switch(temp2[0], temp2[temp2.length - 1]));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                } else {
                                    switchID.add(temp2[0]);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.add(new Switch(temp2[0], temp2[temp2.length - 1]));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
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
                        System.out.println(e.getMessage());
                        controllerSocket.disconnection();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SwitchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

}
