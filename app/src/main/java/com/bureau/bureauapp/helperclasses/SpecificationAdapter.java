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

public class SpecificationAdapter extends ArrayAdapter<SpecificationInfo> {

    private Context context;
    private ArrayList<SpecificationInfo> arrayList;
    Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Comfortaa-Regular.ttf");
    LayoutInflater layoutInflater;

    public SpecificationAdapter(Context context, int textViewResourceId,
                                ArrayList<SpecificationInfo> arrayList) {
        super(context, textViewResourceId, arrayList);
        this.context = context;
        this.arrayList = arrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return arrayList.size();
    }

    public SpecificationInfo getItem(int position) {
        return arrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        TextView label = new TextView(context);
        convertView = layoutInflater.inflate(R.layout.custom_spinner_item, null);
        TextView label = (TextView) convertView.findViewById(R.id.text1);
        label.setTextColor(Color.BLACK);
        label.setText(arrayList.get(position).getSpecificationName());
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
        label.setText(arrayList.get(position).getSpecificationName());

        return label;
    }
}
