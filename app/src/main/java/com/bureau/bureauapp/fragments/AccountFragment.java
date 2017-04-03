package com.bureau.bureauapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.accounts.ConfigurationActivity;
import com.bureau.bureauapp.accounts.HelpFeedbackActivity;
import com.bureau.bureauapp.accounts.HowWeWork;
import com.bureau.bureauapp.accounts.InviteFriendActivity;
import com.bureau.bureauapp.accounts.PreferencesActivity;
import com.bureau.bureauapp.accounts.ProfileActivity;
import com.bureau.bureauapp.adapters.ConversationsRecyclerView;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.myapplication.MyApplication;

public class AccountFragment extends Fragment {
    View rootView;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account, container, false);

        rootView.findViewById(R.id.profileIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        rootView.findViewById(R.id.configurationIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ConfigurationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        rootView.findViewById(R.id.helpFeedbackIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HelpFeedbackActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        rootView.findViewById(R.id.howItWorks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HowWeWork.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        rootView.findViewById(R.id.inviteAFriendIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InviteFriendActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        rootView.findViewById(R.id.profileIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        rootView.findViewById(R.id.preferenceIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ConversationsRecyclerView conversationsList = (ConversationsRecyclerView) rootView.findViewById(R.id.conversations_list);
        conversationsList.init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setInitialHistoricMessagesToFetch(10);

        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new GetAvailableGold(getActivity());
        }
    }
}
