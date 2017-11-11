package com.example.kuoweilun.sdncontrollerapp;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ControllerAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<ControllerIP> list;

    ControllerAdapter(Context context, ArrayList<ControllerIP> list) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_layout, parent, false);
        }
        ControllerIP controllerIP = list.get(position);
        controllerIP.connecctStatus((TextView) convertView.findViewById(R.id.connect_status));
        TextView ip = (TextView) convertView.findViewById(R.id.IP);
        ip.setText(controllerIP.getIP());

        return convertView;
    }
}
