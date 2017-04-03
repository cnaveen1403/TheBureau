package com.bureau.bureauapp.layerclasses;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.sdk.LayerClient;
import com.squareup.picasso.Picasso;

public abstract class BaseActivity extends AppCompatActivity {
    private final int mLayoutResId;
    private final int mMenuResId;
    private final int mMenuTitleResId;
    private final boolean mMenuBackEnabled;
    public ActionBar actionBar;
    public static ImageView mProfileImage;
    public static TextView mTitle;

    public BaseActivity(int layoutResId, int menuResId, int menuTitleResId, boolean menuBackEnabled) {
        mLayoutResId = layoutResId;
        mMenuResId = menuResId;
        mMenuTitleResId = menuTitleResId;
        mMenuBackEnabled = menuBackEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutResId);
        actionBar = getSupportActionBar();
         /*actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (mMenuBackEnabled) actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mMenuTitleResId);
        actionBar.setIcon(mMenuResId);*/

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflator = LayoutInflater.from(this);
        View v = inflator.inflate(R.layout.custom_actionbar, null);

        //if you need to customize anything else about the text, do it here.
        //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
        mTitle = (TextView) v.findViewById(R.id.item_title_custom);
        mTitle.setText(mMenuTitleResId);
        mTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Comfortaa-Regular.ttf"));

        mProfileImage = (ImageView) v.findViewById(R.id.profile_image_custom);
        mProfileImage.setImageResource(mMenuResId);

        v.findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//assign the view to the actionbar
        actionBar.setCustomView(v);
    }

    @Override
    public void setTitle(CharSequence title) {
        actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflator = LayoutInflater.from(this);
        View v = inflator.inflate(R.layout.custom_actionbar, null);

        //if you need to customize anything else about the text, do it here.
        //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
        mTitle = (TextView) v.findViewById(R.id.item_title_custom);
        mTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Comfortaa-Regular.ttf"));
        v.findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//assign the view to the actionbar
        this.getActionBar().setCustomView(v);


        if (getActionBar() == null) {
            super.setTitle(title);
        } else {
            mTitle.setText(title.toString());

//            actionBar.setTitle(AppData.getString(getApplicationContext(),title.toString()));
        }
    }

    @Override
    public void setTitle(int titleId) {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflator = LayoutInflater.from(this);
        View v = inflator.inflate(R.layout.custom_actionbar, null);

        //if you need to customize anything else about the text, do it here.
        //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
        mTitle = (TextView) v.findViewById(R.id.item_title_custom);
        mTitle.setText(titleId);
        mTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Comfortaa-Regular.ttf"));

        mProfileImage = (ImageView) v.findViewById(R.id.profile_image_custom);
//        mProfileImage.setImageResource(mMenuResId);
//assign the view to the actionbar
        v.findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setCustomView(v);

        if (getActionBar() == null) {
            super.setTitle(titleId);
        } else {
            mTitle.setText(titleId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LayerClient client = MyApplication.getLayerClient();
        if (client == null) return;
        if (client.isAuthenticated()) {
            client.connect();
        } else {
            client.authenticate();
        }
    }

    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(mMenuResId, menu);
         return true;
     }

    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Menu "Navigate Up" acts like hardware back button
//            onBackPressed();
            /*Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            */
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected LayerClient getLayerClient() {
        return MyApplication.getLayerClient();
    }

    protected ParticipantProvider getParticipantProvider() {
        return MyApplication.getParticipantProvider();
    }

    protected Picasso getPicasso() {
        return MyApplication.getPicasso();
    }
}
