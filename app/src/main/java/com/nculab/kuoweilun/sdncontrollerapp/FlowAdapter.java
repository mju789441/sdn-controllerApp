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

public class FlowAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Flow> list;

    FlowAdapter(Context context, ArrayList<Flow> list) {
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
        FlowViewHolder holder;
        //取得View component
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_flowitem, parent, false);
            holder = new FlowViewHolder();
            holder.textView_flowSwitchID = (TextView) convertView.findViewById(R.id.textView_flowSwitchID);
            holder.textView_flowPort = (TextView) convertView.findViewById(R.id.textView_flowPort);
            convertView.setTag(holder);
        } else {
            holder = (FlowViewHolder) convertView.getTag();
        }
        //Flow設定
        Flow flow = (Flow) getItem(position);
        //View 內容設定
        holder.textView_flowSwitchID.setText(flow.switchID);
        holder.textView_flowPort.setText(flow.port);
        return convertView;
    }

    static class FlowViewHolder {
        public TextView textView_flowSwitchID = null;
        public TextView textView_flowPort = null;
    }
}
