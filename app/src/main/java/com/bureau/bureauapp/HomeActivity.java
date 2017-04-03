package com.bureau.bureauapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bureau.bureauapp.flavor.DemoAuthenticationProvider;
import com.bureau.bureauapp.flavor.DemoParticipantProvider;
import com.bureau.bureauapp.fragments.AccountFragment;
import com.bureau.bureauapp.fragments.ConnectionFragment;
import com.bureau.bureauapp.fragments.GoldStoreFragment;
import com.bureau.bureauapp.fragments.MatchFragment;
import com.bureau.bureauapp.fragments.PoolFragment;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.ChatData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.bureau.bureauapp.util.AuthenticationProvider;
import com.bureau.bureauapp.util.Util;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity {
    private final String LOG_TAG = HomeActivity.class.getSimpleName();

    BottomBar mBottomBar;
    public static TextView tvGoldAvailable;
    TextView title;
    int tabPosition = 0, totalFriendsCount = 0;
    int previousTabPos = 0, currentTabPos = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.setDefaultTab(R.id.tab_match);

        init(savedInstanceState);
        new GetChatFriendsList().execute();

        if (getIntent().hasExtra("pager_position")) {
            tabPosition = getIntent().getExtras().getInt("pager_position", 0);
        } else {
            tabPosition = 0;
        }

        //for transitions
        currentTabPos = tabPosition;
        previousTabPos = currentTabPos;

        //select the tab programatically
        mBottomBar.selectTabAtPosition(tabPosition);

        MatchFragment matchFragment = new MatchFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottombar_container, matchFragment).commit();

        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId) {
                    case R.id.tab_match:
                        new GetChatFriendsList().execute();
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos;
                        currentTabPos = 0;
                        matchOfTheDaySelected();
                        break;
                    case R.id.tab_pool:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos;
                        currentTabPos = 1;
                        poolSelected();
                        break;
                    case R.id.tab_connections:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos;
                        currentTabPos = 2;
                        connectionsSelected();
                        break;
                    case R.id.tab_gold:
                        title.setText("Gold Store");
                        previousTabPos = currentTabPos;
                        currentTabPos = 3;
                        goldSelected();
                        break;
                    case R.id.tab_account:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos;
                        currentTabPos = 4;
                        accountSelected();
                        break;
                }
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_match:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos = 0;
                        matchOfTheDaySelected();
                        break;
                    case R.id.tab_pool:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos = 1;
                        poolSelected();
                        break;
                    case R.id.tab_connections:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos = 2;
                        connectionsSelected();
                        break;
                    case R.id.tab_gold:
                        title.setText("Gold Store");
                        previousTabPos = currentTabPos = 3;
                        goldSelected();
                        break;
                    case R.id.tab_account:
                        title.setText("TheBureau");
                        previousTabPos = currentTabPos = 4;
                        accountSelected();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.bottombar_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetChatFriendsList().execute();
        new GetAvailableGold(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void commitFragment(Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (previousTabPos > currentTabPos) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        } else if (previousTabPos < currentTabPos) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        fragmentTransaction.replace(R.id.bottombar_container, fragment);
        fragmentTransaction.commit();
    }

    private void matchOfTheDaySelected() {
        commitFragment(new MatchFragment());
    }

    private void poolSelected() {
        commitFragment(new PoolFragment());
    }

    private void connectionsSelected() {
        commitFragment(new ConnectionFragment());
    }

    private void goldSelected() {
        commitFragment(new GoldStoreFragment());
    }

    private void accountSelected() {
        commitFragment(new AccountFragment());
    }

    private void init(@Nullable Bundle savedInstanceState) {
        new GetAvailableGold(this);
        tvGoldAvailable = (TextView) findViewById(R.id.tv_gold_count);
        String goldAvailable = AppData.getString(getApplicationContext(), BureauConstants.goldAvailable);

        if (goldAvailable == null) {
            new GetAvailableGold(this);
            goldAvailable = AppData.getString(getApplicationContext(), BureauConstants.goldAvailable);
        }

        tvGoldAvailable.setVisibility(View.VISIBLE);
        tvGoldAvailable.setText(goldAvailable);
        title = (TextView) findViewById(R.id.title);

        //authenticate layer after login
        authenticateLayer(AppData.getString(getApplicationContext(), BureauConstants.userid));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAffinity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class GetChatFriendsList extends AsyncTask<Void, Void, Void> {

        String resultStr;

        @Override
        protected Void doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(HomeActivity.this, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.GET_CHAT_VIEW;
            url += "?";
            url += paramString;

            resultStr = new ConnectBureau().getDataFromUrl(url);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            android.util.Log.e("MessageListActivity", "chat list " + resultStr);
            totalFriendsCount = 0;
            try {
                if (resultStr != null && resultStr.length() > 1) {
                    String s = resultStr.substring(0, 1);
                    if (s.equalsIgnoreCase("{")) {
                        JSONObject jsonObject = new JSONObject(resultStr);
                        if (jsonObject.has("msg")) {
                            Log.e("HomeActivity", "Fetching friends count " + jsonObject.getString("response"));
                            if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                                Util.invalidateUserID(HomeActivity.this, HomeActivity.this);
                            }
                        }
                    } else if (s.equalsIgnoreCase("[")) {
                        JSONArray jsonArray = new JSONArray(resultStr);
                        if (jsonArray.length() > 0) {
                            DemoParticipantProvider demoParticipantProvider = new DemoParticipantProvider(HomeActivity.this);
                            demoParticipantProvider.fetchParticipants();

                            totalFriendsCount = jsonArray.length();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectChild = jsonArray.getJSONObject(i);
//                                ChatData.saveString(HomeActivity.this, jsonObjectChild.getString("userid"), jsonObjectChild.getString("First Name"));
                                ChatData.saveString(HomeActivity.this, jsonObjectChild.getString("userid") + "I", jsonObjectChild.getString("img_url"));
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BottomBarTab nearby = mBottomBar.getTabWithId(R.id.tab_connections);
            Log.d("HomeActivity", "Fetching totalFriendsCount " + totalFriendsCount);

            nearby.setBadgeCount(totalFriendsCount);
        }
    }

    private void authenticateLayer(final String userid) {
//        mName.setEnabled(false);
        com.bureau.bureauapp.util.Log.v("authenticateLayer id `" + userid);

        MyApplication.authenticate(new DemoAuthenticationProvider.Credentials(MyApplication.getLayerAppId(), userid),
                new AuthenticationProvider.Callback() {
                    @Override
                    public void onSuccess(AuthenticationProvider provider, String userId) {
//                        progressDialog.dismiss();
                        if (com.bureau.bureauapp.util.Log.isLoggable(com.bureau.bureauapp.util.Log.VERBOSE)) {
                            com.bureau.bureauapp.util.Log.v("Successfully authenticated as `" + userid + "` with userId `" + userId + "`");
                        }
                    }

                    @Override
                    public void onError(AuthenticationProvider provider, final String error) {
//                        progressDialog.dismiss();
                        if (com.bureau.bureauapp.util.Log.isLoggable(com.bureau.bureauapp.util.Log.ERROR)) {
                            com.bureau.bureauapp.util.Log.e("Failed to authenticate as `" + userid + "`: " + error);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_LONG).show();
//                                mName.setEnabled(true);
                            }
                        });
                    }
                });
    }
}
