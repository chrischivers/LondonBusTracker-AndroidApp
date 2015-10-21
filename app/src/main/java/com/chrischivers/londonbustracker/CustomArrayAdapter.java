package com.chrischivers.londonbustracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** class to act as list adapter for rows List */
 public class CustomArrayAdapter extends ArrayAdapter<RouteFromToObj> {
    public ArrayList<RouteFromToObj> routeList;

    /** To cache views of item */
    private static class ViewHolder {
        private TextView routeID;
        private TextView fromTo;
        private CheckBox checkBox;

        ViewHolder() {
        }
    }


    private final LayoutInflater inflater;

    public CustomArrayAdapter (final Context context,
                               final int resource,
                               final int textViewResourceId,
                               final List<RouteFromToObj> objects) {
        super(context, resource, textViewResourceId, objects);
        this.routeList = new ArrayList<RouteFromToObj>();
        this.routeList.addAll(objects);

        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.route_list_item, parent, false);

            holder = new ViewHolder();
            holder.routeID = (TextView) convertView.findViewById(R.id.routeIDText);
            holder.fromTo = (TextView) convertView.findViewById(R.id.routeFromToText);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.routeSelectedCheckbox);
            convertView.setTag(holder);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    CheckBox cb = (CheckBox) buttonView;
                    RouteFromToObj routeFromToObj = (RouteFromToObj) cb.getTag();
                    routeFromToObj.setSelected(cb.isChecked());
                }
            });

            holder.routeID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = ((CheckBox) v.getTag());
                    cb.setChecked(!cb.isChecked());
                }
            });

            holder.fromTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = ((CheckBox) v.getTag());
                    cb.setChecked(!cb.isChecked());
                }
            });
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        RouteFromToObj routeFromToObj = routeList.get(position);
        holder.routeID.setText(routeFromToObj.routeID);
        holder.fromTo.setText(routeFromToObj.from + " <-> " + routeFromToObj.to);
        holder.checkBox.setTag(routeFromToObj);
        holder.routeID.setTag(holder.checkBox);
        holder.fromTo.setTag(holder.checkBox);

        return convertView;

    }

}
