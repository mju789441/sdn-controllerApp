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
    //Thread
    private Thread thread_getSwitch;

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

    @Override
    public void onPause() {
        super.onPause();
        thread_getSwitch.interrupt();
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

    private void setThread() {
        thread_getSwitch = new Thread(new Runnable() {
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
                                Toast.makeText(SwitchActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        //傳送請求
                        controllerSocket.sendEncryptedMsg("GET switch -ID -bytes");
                        //接收回復
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        String[] temp = msg.split("\n");
                        if (temp.length > 2 && temp[0].equals("switch_speed") && temp[temp.length - 1].equals("/switch_speed")) {
                            boolean switchChanged = false;
                            if (list.size() > temp.length - 2) {
                                switchChanged = true;
                                for (int i = temp.length - 2; i < list.size(); i++) {
                                    list.remove(i);
                                }
                            }
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (list.size() < temp.length - 2) {
                                    switchChanged = true;
                                    list.add(new Switch(temp2[0], temp2[temp2.length - 1]));
                                } else if (!list.get(i - 1).equals(new Switch(temp2[0], temp2[temp2.length - 1]))) {
                                    switchChanged = true;
                                    list.set(i - 1, new Switch(temp2[0], temp2[temp2.length - 1]));
                                }
                            }
                            if (switchChanged) {
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
                                    Toast.makeText(SwitchActivity.this, "取得資料錯誤", Toast.LENGTH_SHORT).show();
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
