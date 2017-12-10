package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostActivity {

    Context context;
    View view;
    Controller controller;
    ListView listView;
    private ArrayList<Host> list;
    private HostAdapter adapter;
    //Handler
    Handler handler = new Handler();
    //Thread
    public Thread thread_getHost;

    public HostActivity(Context context, View view, Controller controller) {
        this.context = context;
        this.view = view;
        this.controller = controller;
        initView();
        // setListeners();
        //setThread();
        //thread_getHost.start();
    }

    private void initView() {
        listView = (ListView) view.findViewById(R.id.list_host);
        list = new ArrayList<Host>();
        adapter = new HostAdapter(context, list);
        listView.setAdapter(adapter);
        handler.post(new Runnable() {
            @Override
            public void run() {
                list.add(new Host("123","123"));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                adapter.notifyDataSetChanged();
                PopupMenu popupmenu = new PopupMenu(context, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_switch, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.watch_host:
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
    }

    private void setThread() {
        thread_getHost = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

}
