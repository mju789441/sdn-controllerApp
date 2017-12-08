package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class SwitchAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Switch> list;

    SwitchAdapter(Context context, ArrayList<Switch> list) {
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
        SwitchViewHolder holder;
        //取得View component
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.switchitem_layout, parent, false);
            holder = new SwitchViewHolder();
            holder.textView_switchID = (TextView) convertView.findViewById(R.id.textview_switchID);
            holder.textView_flow = (TextView) convertView.findViewById(R.id.textview_flow);
            convertView.setTag(holder);
        } else {
            holder = (SwitchViewHolder) convertView.getTag();
        }
        //Switch設定
        Switch item = (Switch) getItem(position);
        //View 內容設定
        holder.textView_switchID.setText(item.switchID);
        holder.textView_flow.setText(item.flow);
        return convertView;
    }

    static class SwitchViewHolder {
        public TextView textView_switchID = null;
        public TextView textView_flow = null;
    }
}
