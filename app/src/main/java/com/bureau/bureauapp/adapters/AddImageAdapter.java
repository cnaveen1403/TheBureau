package com.bureau.bureauapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bureau.bureauapp.R;

import java.util.ArrayList;

/**
 * Created by hi on 1/18/2017.
 */
public class AddImageAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> images;
    LayoutInflater inflater;

    public AddImageAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_profile_image, null);
        }

        Glide.with(context).load(images.get(position)).placeholder(R.drawable.user).error(R.drawable.user).into(((ImageView) convertView.findViewById(R.id.image)));


        return convertView;
    }
}
