package com.bureau.bureauapp.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.layerclasses.ConversationsListFragment;
import com.bureau.bureauapp.myapplication.AppData;

import java.util.ArrayList;
import java.util.List;

import io.smooch.ui.ConversationActivity;

public class ConnectionFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.connections_fragment, container, false);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
//        viewPager.setOffscreenPageLimit(0);
        setupViewPager(viewPager);
//        viewPager.setAdapter(buildAdapter());

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0) {
                    ConversationActivity.show(getActivity());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppData.saveBoolean(getActivity(), BureauConstants.USER_FILE, false);
        tabLayout.getTabAt(1).getCustomView().setSelected(true);
        viewPager.setCurrentItem(1);
        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new GetAvailableGold(getActivity());
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    private void setupTabIcons() {

        View view1 = getActivity().getLayoutInflater().inflate(R.layout.connection_tab_icon, null);
        ImageView myCustomIcon1 = (ImageView) view1.findViewById(R.id.tab_icon);
        myCustomIcon1.setBackgroundResource(R.drawable.connection_247_tab);
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = getActivity().getLayoutInflater().inflate(R.layout.connection_tab_icon, null);
        ImageView myCustomIcon2 = (ImageView) view2.findViewById(R.id.tab_icon);
        myCustomIcon2.setBackgroundResource(R.drawable.connection_chat_tab);
        tabLayout.getTabAt(1).setCustomView(view2);

        View view3 = getActivity().getLayoutInflater().inflate(R.layout.connection_tab_icon, null);
        ImageView myCustomIcon3 = (ImageView) view3.findViewById(R.id.tab_icon);
        myCustomIcon3.setBackgroundResource(R.drawable.connection_rematch);
        tabLayout.getTabAt(2).setCustomView(view3);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(new CSFragment(), "Account1");
        adapter.addFrag(new ConversationsListFragment(), "Match");
        adapter.addFrag(new RematchTab(), "Rematch");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<Fragment>();
//        private final List<String> mFragmentTitleList = new ArrayList<String>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
//            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
            return null;
        }
    }
}
