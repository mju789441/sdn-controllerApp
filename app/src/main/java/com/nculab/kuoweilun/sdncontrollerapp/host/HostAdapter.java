package com.nculab.kuoweilun.sdncontrollerapp.host;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nculab.kuoweilun.sdncontrollerapp.R;

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
            holder.ttextView_host_port = (TextView) convertView.findViewById(R.id.textView_host_port);

            holder.textView_host_speed = (TextView) convertView.findViewById(R.id.textView_host_speed);
            convertView.setTag(holder);
        } else {
            holder = (HostViewHolder) convertView.getTag();
        }
        //Host設定
        Host host = (Host) getItem(position);
        //View 內容設定
        holder.ttextView_host_port.setText(host.port);

        holder.textView_host_speed.setText(host.speed);
        return convertView;
    }

    private static class HostViewHolder {
        TextView ttextView_host_port = null;
        TextView textView_host_speed = null;
    }
}
