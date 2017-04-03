package com.bureau.bureauapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bureau.bureauapp.R;

public class HorizantalListAdapter extends BaseAdapter {

    Context context;
    String[] images;
    LayoutInflater layoutInflater;

    public HorizantalListAdapter(Context context, String[] images) {
        this.context = context;
        this.images = images;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return images.length;
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
            convertView = layoutInflater.inflate(R.layout.custom_horizantal_list_image, null);
        }
        ImageView userProfileIMG = (ImageView) convertView.findViewById(R.id.userProfileIMG);//.setImageResource(images[position]);
        String image = images[position];
        image = image.replace("\\", "");
        image = image.replace("\"", "");


        Glide.with(context).load(image).placeholder(R.drawable.user).error(R.drawable.user).into(userProfileIMG);
        return convertView;
    }
}
