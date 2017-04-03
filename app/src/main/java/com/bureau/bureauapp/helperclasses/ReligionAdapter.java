package com.bureau.bureauapp.helperclasses;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bureau.bureauapp.R;

import java.util.ArrayList;

public class ReligionAdapter extends ArrayAdapter<ReligionInfo> {

    private Context context;
    private ArrayList<ReligionInfo> arrayList;
    Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Comfortaa-Regular.ttf");
    LayoutInflater layoutInflater;

    public ReligionAdapter(Context context, int textViewResourceId,
                           ArrayList<ReligionInfo> arrayList) {
        super(context, textViewResourceId, arrayList);
        this.context = context;
        this.arrayList = arrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return arrayList.size();
    }

    public ReligionInfo getItem(int position) {
        return arrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.custom_spinner_item, null);
        TextView label = (TextView) convertView.findViewById(R.id.text1);
//        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(arrayList.get(position).getReligionName());
        label.setTextSize(14);
        label.setTypeface(typeface);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
//        TextView label = new TextView(context);
        convertView = layoutInflater.inflate(R.layout.custom_spinner_item, null);
        TextView label = (TextView) convertView.findViewById(R.id.text1);
        label.setTextColor(Color.BLACK);
        label.setText(arrayList.get(position).getReligionName());

        return label;
    }
}
