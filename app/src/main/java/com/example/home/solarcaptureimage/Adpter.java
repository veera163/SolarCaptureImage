package com.example.home.solarcaptureimage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by home on 2/7/2018.
 */

public class Adpter extends BaseAdapter {
    Context context;
    ArrayList<String> arrayList;

    public Adpter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    public class Holder
    {

        TextView tv;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Holder holder=new Holder();
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.locationlist, viewGroup, false);
        holder.tv=(TextView) itemView.findViewById(R.id.veera);
        holder.tv.setText(arrayList.get(i));
        return itemView;
    }
}
