package com.bureau.bureauapp.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.myapplication.AppData;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class InviteFriendActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_invite_friend);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        String referralCode = AppData.getString(InviteFriendActivity.this, BureauConstants.referralCode);
        final String bodyText = "Hi! Join me on TheBureau by downloading the app https://play.google.com/store/apps/details?id=com.bureau.bureauapp&hl=en . Use my referral code " + referralCode + " to sign up and get 150 free gold coins.";

        findViewById(R.id.emailLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, "");
                i.putExtra(Intent.EXTRA_SUBJECT, "Check out TheBureau MyApplication");
                i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(bodyText));
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(InviteFriendActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.smsLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "");
                smsIntent.putExtra("sms_body", "" + Html.fromHtml(bodyText));
                try {
                    startActivity(smsIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(InviteFriendActivity.this, "Unable to send SMS from this device. Please contact your service provider.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
