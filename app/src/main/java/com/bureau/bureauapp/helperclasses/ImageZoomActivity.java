package com.bureau.bureauapp.helperclasses;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.adapters.ZoomViewPagerAdapter;
import com.bureau.bureauapp.model.MatchInfo;

import de.greenrobot.event.EventBus;

public class ImageZoomActivity extends Activity {

    ViewPager mpager;
    ImageView closeImage;
    MatchInfo matchInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_zoom_activity);

        mpager = (ViewPager) findViewById(R.id.pager);
        closeImage = (ImageView) findViewById(R.id.iv_close_image);

        String[] pager_items = getIntent().getExtras().getStringArray("pager_items");
        int position = getIntent().getExtras().getInt("position", 0);
        if (pager_items != null) {

            if (pager_items.length > 0) {
                mpager.setAdapter(new ZoomViewPagerAdapter(ImageZoomActivity.this, pager_items));
                mpager.setCurrentItem(position);
            }
        }
        matchInfo = EventBus.getDefault().removeStickyEvent(MatchInfo.class);

        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (matchInfo != null) {
                    EventBus.getDefault().postSticky(matchInfo);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (matchInfo != null) {
            EventBus.getDefault().postSticky(matchInfo);
        }
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
