package com.bureau.bureauapp.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bureau.bureauapp.R;

public class ZoomViewPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    String mResources[];

    public ZoomViewPagerAdapter(Context context, String[] pager_items) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = pager_items;
    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.custom_zoom_image, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.fullimage);
        String image = mResources[position];
        image = image.replace("\\", "");
        image = image.replace("\"", "");
//        Log.e("Bureau","Image Url "+image);
        Glide.with(mContext).load(image).placeholder(R.drawable.user).error(R.drawable.user).into(imageView);
//		imageView.setImageResource(mResources[position]);
        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
