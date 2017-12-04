package com.nculab.kuoweilun.sdncontrollerapp;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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

public class SwitchHandler {

    Context context;
    View view;
    Controller controller;
    ListView listView;
    private ArrayList<Switch> list;
    private SwitchAdapter adapter;
    //Handler
    Handler handler = new Handler();
    //Thread
    Thread thread_getSwitch;

    public SwitchHandler(Context context, View view, Controller controller) {
        this.context = context;
        this.view = view;
        this.controller = controller;
        initView();
        setListeners();
        setThread();
        thread_getSwitch.start();
    }

    private void initView() {
        listView = (ListView) view.findViewById(R.id.list_switch);
        list = new ArrayList<Switch>();
        adapter = new SwitchAdapter(context, list);
        listView.setAdapter(adapter);
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                adapter.notifyDataSetChanged();
                PopupMenu popupmenu = new PopupMenu(context, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_instruction, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final Switch controller = (Switch) parent.getItemAtPosition(position);
                        Button back;
                        switch (item.getItemId()) {

                        }
                        return true;
                    }
                });
                popupmenu.show();
            }
        });
    }

    private void setThread() {
        thread_getSwitch = new Thread(new Runnable() {
            @Override
            public void run() {
                controller.sendInstruction("GET switch -ID -bytes");
                while (true) {
                    try {
                        //未連線時拋出例外
                        if (!controller.isConnected()) {
                            throw new InterruptedException();
                        }
                        int status = 0;
                        //接收訊息
                        while (true) {
                            final String str = controller.getMsg();
                            final String msg = controller.rsa.decrypt(str.getBytes());
                            //錯誤訊息
                            if (str == null) {
                                throw new Exception();
                            } else {
                                if (status == 0 && msg == "switch_speed") {
                                    status = 1;
                                } else if (status == 1) {
                                    if (msg == "/switch_speed") {
                                        status = 0;
                                        break;
                                    } else {
                                        //到時候可能把各式各樣的訊息在這裡儲存
                                        if (msg.contains(" ")) {
                                            String[] temp = msg.split(" ");
                                            String switchID = "";
                                            String flow = "";
                                            switchID = temp[0];
                                            flow = temp[temp.length - 1];
                                            if (switchID != "" && flow != "") {
                                                list.add(new Switch(switchID, flow));
                                            }
                                        } else {
                                            throw new Exception();
                                        }
                                    }
                                }
                            }
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        controller.disconnection();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

}
