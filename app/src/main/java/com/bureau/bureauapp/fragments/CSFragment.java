package com.bureau.bureauapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bureau.bureauapp.R;

public class CSFragment extends Fragment {

    Context context;
    FrameLayout frameLayout;

    public CSFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.empty_activity, container, false);
        context = getActivity();
        frameLayout = (FrameLayout) rootView.findViewById(R.id.frameLayout);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // load data here
            Toast.makeText(context,"Hi",Toast.LENGTH_SHORT).show();
        } else {
            // fragment is no longer visible
        }
    }*/

}
