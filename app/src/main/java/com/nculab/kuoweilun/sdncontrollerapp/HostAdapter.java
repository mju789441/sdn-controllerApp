package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Host> list;

    HostAdapter(Context context, ArrayList<Host> list) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HostViewHolder holder;
        //取得View component
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_hostitem, parent, false);
            holder = new HostViewHolder();
            holder.textView_hostID = (TextView) convertView.findViewById(R.id.textview_hostID);
            holder.textView_hostIP = (TextView) convertView.findViewById(R.id.textview_hostIP);
            convertView.setTag(holder);
        } else {
            holder = (HostViewHolder) convertView.getTag();
        }
        //Host設定
        Host host = (Host) getItem(position);
        //View 內容設定
        holder.textView_hostID.setText(host.ID);
        holder.textView_hostIP.setText(host.IP);
        return convertView;
    }

    static class HostViewHolder {
        public TextView textView_hostID = null;
        public TextView textView_hostIP = null;
    }
}
