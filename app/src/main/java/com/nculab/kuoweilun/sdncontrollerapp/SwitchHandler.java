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
    private ArrayList<String> switchID;
    //Handler
    Handler handler = new Handler();
    //Thread
    public Thread thread_getSwitch;

    public SwitchHandler(Context context, View view, Controller controller) {
        this.context = context;
        this.view = view;
        this.controller = controller;
        initView();
        //setListeners();
        setThread();
        thread_getSwitch.start();
    }

    private void initView() {
        listView = (ListView) view.findViewById(R.id.list_switch);
        list = new ArrayList<Switch>();
        adapter = new SwitchAdapter(context, list);
        listView.setAdapter(adapter);
        switchID = new ArrayList<String>();
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
                while (true) {
                    try {
                        //未連線時拋出例外
                        if (!controller.isConnected()) {
                            throw new InterruptedException();
                        }
                        controller.sendEncryptedMsg("GET switch -ID -bytes");
                        final String str = controller.getMsg();
                        if (str == null) {
                            throw new Exception();
                        }
                        final String msg = controller.rsa.decrypt(str.getBytes());
                        // 接收訊息
                        String[] temp = msg.split("\n");
                        if (temp[0].equals("switch_speed") && temp[temp.length - 1].equals("/switch_speed")) {
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (switchID.contains(temp2[0])) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.set(switchID.indexOf(temp2[0]), new Switch(temp2[0], temp2[temp2.length - 1], adapter));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                } else {
                                    switchID.add(temp2[0]);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.add(new Switch(temp2[0], temp2[temp2.length - 1], adapter));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        } else {
                            throw new Exception();
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                        controller.disconnection();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

}
