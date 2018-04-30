package com.nculab.kuoweilun.sdncontrollerapp.switcher;

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
            convertView = layoutInflater.inflate(R.layout.layout_switchitem, parent, false);
            holder = new SwitchViewHolder();
            holder.textView_switch_ID = (TextView) convertView.findViewById(R.id.textView_switch_ID);
            holder.textView_speed = (TextView) convertView.findViewById(R.id.textView_speed);
            convertView.setTag(holder);
        } else {
            holder = (SwitchViewHolder) convertView.getTag();
        }
        //Switch設定
        Switch getSwitch = (Switch) getItem(position);
        //View 內容設定
        holder.textView_switch_ID.setText(getSwitch.ID);
        holder.textView_speed.setText(getSwitch.speed);
        return convertView;
    }

    static class SwitchViewHolder {
        public TextView textView_switch_ID = null;
        public TextView textView_speed = null;
    }
}
