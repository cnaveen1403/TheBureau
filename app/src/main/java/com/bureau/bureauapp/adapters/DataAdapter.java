package com.bureau.bureauapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.model.MatchInfo;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private Context context;
    ArrayList<MatchInfo> matchInfos;

    public DataAdapter(Context context, ArrayList<MatchInfo> matchInfos) {
        this.matchInfos = matchInfos;
        this.context = context;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_remach, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {
        if (matchInfos.get(i).getUserAction().equalsIgnoreCase("Liked")) {
            viewHolder.tv_android.setBackgroundResource(R.drawable.btn_liked);
        } else {
            viewHolder.tv_android.setBackgroundResource(R.drawable.btn_passed);
        }

        String image = null;
        JSONArray jsonArray = matchInfos.get(i).getProfileImage();
        for (int j = 0; j < jsonArray.length(); j++) {
            try {
                image = jsonArray.getString(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        String image = mResources[position];
        image = image.replace("\\", "");
        image = image.replace("\"", "");
        Picasso.with(context).load(image).placeholder(R.drawable.user).error(R.drawable.user).into(viewHolder.img_android);
    }

    @Override
    public int getItemCount() {
        return matchInfos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_android;
        private ImageView img_android;

        public ViewHolder(View view) {
            super(view);

            tv_android = (TextView) view.findViewById(R.id.likedPassedTV);
            img_android = (ImageView) view.findViewById(R.id.userImage);
        }
    }

}
