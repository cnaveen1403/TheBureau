package com.bureau.bureauapp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.adapters.ConversationsRecyclerView;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.LikeView;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class GoldStoreFragment extends Fragment {

    Context mContext;
    Activity activity;
    CallbackManager callbackManager;
    LinearLayout ll_referral_code;
    EditText referralCodeET;
    public static TextView tvTotalGold;
    TextView textView;
    String referralCode = "";
    View rootView;
    String TAG = "GoldStoreFragment";
    BillingProcessor bp;
    String addGoldAmount = "0", inAppProduct1 = "product1", inAppProduct2 = "product2", inAppProduct3 = "product3", inAppProduct1GoldAMount = "140",
            inAppProduct2GoldAMount = "380", inAppProduct3GoldAMount = "1000";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.gold_store_fragment, container, false);
        mContext = getActivity();
        activity = getActivity();
        referralCodeET = (EditText) rootView.findViewById(R.id.referralCodeET);
        ll_referral_code = (LinearLayout) rootView.findViewById(R.id.ll_referral_code);
        tvTotalGold = (TextView) rootView.findViewById(R.id.goldAmountTV);
        tvTotalGold.setText(AppData.getString(mContext, BureauConstants.goldAvailable));

        if (AppData.getString(mContext, BureauConstants.referralCodeApplied).equalsIgnoreCase("y")) {
            ll_referral_code.setVisibility(View.GONE);
        } else {
            ll_referral_code.setVisibility(View.VISIBLE);
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.sdkInitialize(mContext);
        callbackManager = CallbackManager.Factory.create();

        LikeView likeView = (LikeView) rootView.findViewById(R.id.fbLikeView);
        FrameLayout.LayoutParams buttonLayout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
//        likeView.setScaleX(2.8f);
//        likeView.setScaleY(1.8f);
//        likeView.setLikeViewStyle(R.style.spinner_style);
        likeView.setLikeViewStyle(LikeView.Style.BUTTON);
        if (savedInstanceState == null) {
            likeView.setObjectIdAndType(
                    "https://www.facebook.com/thebureauapp/?fref=pb&hc_location=profile_browser",
                    LikeView.ObjectType.PAGE);
            // Set foreground color fpr Like count text
            likeView.setForegroundColor(-256);
            likeView.setLayoutParams(buttonLayout);
        }

        rootView.findViewById(R.id.submitBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (referralCodeET.getText().toString().trim().length() > 0) {
                    referralCode = referralCodeET.getText().toString();
                    hideSoftKeyboard(getActivity());
                    if (ConnectBureau.isNetworkAvailable(getActivity())) {
                        new SubmitReferralCodeAsync().execute();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                    // Check if no view has focus:
                } else {
                    Toast.makeText(mContext, "Please enter refferal code", Toast.LENGTH_SHORT).show();
                }
            }
        });


        rootView.findViewById(R.id.followUsOnInstagramRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/thebureauapp/"));
                startActivity(browserIntent);
            }
        });

        rootView.findViewById(R.id.inviteFriendsRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLinkUrl;

                appLinkUrl = "https://fb.me/434574723598294";

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(appLinkUrl)
                            .setPreviewImageUrl(BureauConstants.PREVIEW_IMAGE_URL)
                            .build();

                    AppInviteDialog appInviteDialog = new AppInviteDialog(getActivity());
                    CallbackManager sCallbackManager = CallbackManager.Factory.create();

                    appInviteDialog.registerCallback(sCallbackManager,
                            new FacebookCallback<AppInviteDialog.Result>() {
                                @Override
                                public void onSuccess(AppInviteDialog.Result result) {
                                    Log.d("Invitation", "Invitation Sent Successfully");
                                }

                                @Override
                                public void onCancel() {
                                }

                                @Override
                                public void onError(FacebookException e) {
                                    Log.d("Invitation", "Error Occured");
                                }
                            });
                    appInviteDialog.show(content);
                }
            }
        });

        SpannableString ss = new SpannableString(getString(R.string.txtGoldStoreTerms));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                showTermsAndConditions();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 37, 55, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView = (TextView) rootView.findViewById(R.id.tv_terms_and_cond);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(getResources().getColor(R.color.blue));

        bp = new BillingProcessor(getActivity(), BureauConstants.base64EncodedPublicKey, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
//                showAlert("onProductPurchased: " + productId);

                if (productId.equalsIgnoreCase(inAppProduct1)) {
                    addGoldAmount = inAppProduct1GoldAMount;
                } else if (productId.equalsIgnoreCase(inAppProduct2)) {
                    addGoldAmount = inAppProduct2GoldAMount;
                } else if (productId.equalsIgnoreCase(inAppProduct3)) {
                    addGoldAmount = inAppProduct3GoldAMount;
                }
                new AddGoldAsync().execute();
                bp.consumePurchase(productId);
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
                showAlert("onBillingError: " + Integer.toString(errorCode));
            }

            @Override
            public void onBillingInitialized() {
//                showToast("onBillingInitialized");
            }

            @Override
            public void onPurchaseHistoryRestored() {
//                showAlert("onPurchaseHistoryRestored");
                for (String sku : bp.listOwnedProducts())
                    Log.d(TAG, "Owned Managed Product: " + sku);
                for (String sku : bp.listOwnedSubscriptions())
                    Log.d(TAG, "Owned Subscription: " + sku);
            }
        });

        rootView.findViewById(R.id.leaseGoldLL1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(getActivity(), inAppProduct1);
            }
        });
        rootView.findViewById(R.id.leaseGoldLL2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(getActivity(), inAppProduct2);
            }
        });
        rootView.findViewById(R.id.leaseGoldLL3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(getActivity(), inAppProduct3);
            }
        });

        return rootView;
    }

    private void showAlert(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d("GoldstoreFragment", "Showing alert dialog: " + message);
        bld.create().show();
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ConversationsRecyclerView conversationsList = (ConversationsRecyclerView) rootView.findViewById(R.id.conversations_list);
        conversationsList.init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setInitialHistoricMessagesToFetch(10);

        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new GetAvailableGold(getActivity());
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void showTermsAndConditions() {
        final Dialog openDialog = new Dialog(mContext);
        openDialog.setContentView(R.layout.customdialog_layout);
        ((TextView) openDialog.findViewById(R.id.tv_custom_dialog_title)).setText("Terms And Conditions");
        TextView dialogTextContent = (TextView) openDialog.findViewById(R.id.dialog_textview);
        dialogTextContent.setText(getTermsAndConditions("termsandconditions.txt"));
        ImageView dialogCloseButton = (ImageView) openDialog.findViewById(R.id.dialog_close_button);
        dialogCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openDialog.dismiss();
            }
        });
        openDialog.show();
    }

    public String getTermsAndConditions(String filename) {
        // Open and read the contents of <filename> into
        // a single string then return it

        InputStream is = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = getApplicationContext().getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == 32459) {
                if (!bp.handleActivityResult(requestCode, resultCode, data))
                    super.onActivityResult(requestCode, resultCode, data);

            } else {
                super.onActivityResult(requestCode, resultCode, data);
                if ("com.facebook.platform.action.request.LIKE_DIALOG".equals(data.getStringExtra("com.facebook.platform.protocol.PROTOCOL_ACTION"))) {
                    // get action results
                    Bundle bundle = data.getExtras().getBundle("com.facebook.platform.protocol.RESULT_ARGS");
                    if (bundle != null) {
//                       textView.setText("Facebook object_is_liked " + bundle.getBoolean("object_is_liked")+"  "+"Facebook completionGesture " + bundle.getString("completionGesture")+"  "+"Facebook like_count " + bundle.getInt("like_count"));
                        Log.e("Bureau", "Facebook object_is_liked " + bundle.getBoolean("object_is_liked")); // liked/unliked
                        Log.e("Bureau", "Facebook didComplete " + bundle.getInt("didComplete"));
                        Log.e("Bureau", "Facebook like_count " + bundle.getInt("like_count")); // object like count
                        Log.e("Bureau", "Facebook like_count_string " + bundle.getString("like_count_string"));
                        Log.e("Bureau", "Facebook social_sentence " + bundle.getString("social_sentence"));
                        Log.e("Bureau", "Facebook completionGesture " + bundle.getString("completionGesture")); // liked/cancel/unliked
                    }
                }
            }
        }
    }


    public class SubmitReferralCodeAsync extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(getActivity(), R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.referralCode, referralCode));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.GOLD_CHECK_REFERRALS;
            url += "?";
            url += paramString;

            Log.e("Bureau", "Refferal url " + url);

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.GOLD_CHECK_REFERRALS, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                String response = jsonObject.getString("response");
//                Log.e("Bureau","Refferal code "+result);

                if (msg.equalsIgnoreCase("Success")) {
                    new GetAvailableGold(mContext);

                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText(android.R.string.ok);

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tvTotalGold.setText(AppData.getString(mContext, BureauConstants.goldAvailable));
                            AppData.saveString(mContext, BureauConstants.referralCodeApplied, "y");
                            ll_referral_code.setVisibility(View.GONE);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else if (msg.equals("Error")) {

                    if (response.equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(mContext, activity);
                    }

                    if (response.equalsIgnoreCase("Invalid referral code")) {
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText(android.R.string.ok);

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class AddGoldAsync extends AsyncTask<String, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(getActivity(), R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(getActivity(), BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.Gold_to_add, addGoldAmount));


            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.ADD_GOLD_URL, parms);

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equalsIgnoreCase("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(getActivity(), getActivity());
                    } else {

                        final Dialog dialog = new Dialog(mContext);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("Ok");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                } else {
                    new GetAvailableGold(getActivity());
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
