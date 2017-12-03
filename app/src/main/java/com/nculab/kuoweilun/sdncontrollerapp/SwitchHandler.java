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
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class SwitchHandler{

    Context context;
    View view;
    Controller controller;
    ListView listView;
    private ArrayList<Switch> list;
    private SwitchAdapter adapter;

    public SwitchHandler(Context context, View view, Controller controller) {
        this.context = context;
        this.view = view;
        this.controller = controller;
        initView();
        setListeners();
        list.add(new Switch("12","60"));
    }

    public void initView() {
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

}
