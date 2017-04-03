package com.bureau.bureauapp.layerclasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.adapters.ConversationsAdapter;
import com.bureau.bureauapp.adapters.ConversationsRecyclerView;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.model.ChatAvatarModel;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.ChatData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.bureau.bureauapp.util.Log;
import com.layer.atlas.util.views.SwipeableItem;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.transform.Result;

public class ConversationsListFragment extends Fragment {

    Context mContext;
    //    public static HashMap<String, ChatAvatarModel> chatAvatarModelHashMap = new HashMap<>();
    ConversationsRecyclerView conversationsList;
    int totalFriendsCount = 0;
    public static int totalConnectedFriendsCount = 0;
    TextView messageListCountTV;
    View addChatView;
    Activity activity;
    /* public ConversationsListFragment() {
        super(R.layout.activity_conversations_list, R.menu.menu_conversations_list, R.string.title_conversations_list, false);
    }
*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_conversations_list, container, false);

        mContext = getParentFragment().getActivity();
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Comfortaa-Regular.ttf");

        if (MyApplication.routeLogin(getActivity())) {
            if (!getActivity().isFinishing()) getActivity().finish();
        }
        conversationsList = (ConversationsRecyclerView) rootView.findViewById(R.id.conversations_list);
        conversationsList.setTypeface(typeface, typeface, typeface, typeface, typeface);

        activity = getActivity();
//        HashSet<String> participantIds = new HashSet<String>(conversation.getParticipants());

        // Atlas methods
        messageListCountTV = (TextView) rootView.findViewById(R.id.messageListCountTV);
        addChatView = rootView.findViewById(R.id.addChatView);

//        initConversationActions();

        new GetChatFriendsList().execute();

        rootView.findViewById(R.id.addChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ConversationListActivity messageListCountTV clicked");

                Intent intent = new Intent(getActivity(), MessagesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("friendsList", "friendsList");
                startActivity(intent);
            }
        });
        messageListCountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ConversationListActivity messageListCountTV clicked");
                Intent intent = new Intent(getActivity(), MessagesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("friendsList", "friendsList");
                startActivity(intent);
            }
        });
        addChatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ConversationListActivity messageListCountTV clicked");
                Intent intent = new Intent(getActivity(), MessagesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("friendsList", "friendsList");
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.e("ConversationsListFrag", "onResume ==>> ");
        initConversationActions();

/*


        final Handler handlerSync = new Handler();
        Timer timerSync = new Timer();
//        timer.cancel();
        TimerTask doAsynchronousTaskSync = new TimerTask() {
            @Override
            public void run() {
                handlerSync.post(new Runnable() {
                    public void run() {
                        try {
                            initConversationActions();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timerSync.schedule(doAsynchronousTaskSync, 20 * 1000, 100000 * 60 * 1000); //execute in every 50000 ms
*/
/*
        if (!AppData.getBoolean(getParentFragment().getActivity(), BureauConstants.RELOAD_FRAGMENT)) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
            android.util.Log.e("ConversationsListFrag", "onResume ==>> AppData.getBoolean");

            AppData.saveBoolean(getParentFragment().getActivity(),BureauConstants.RELOAD_FRAGMENT, true);
        }*/

    }

    private class GetChatFriendsList extends AsyncTask<Void, Void, Result> {

        ProgressDialog pDialog;
        String resultStr;
        String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCanceledOnTouchOutside(false);
//            pDialog.show();
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(getParentFragment().getActivity(), BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            url = BureauConstants.BASE_URL + BureauConstants.GET_CHAT_VIEW;
            url += "?";
            url += paramString;
        }

        @Override
        protected Result doInBackground(Void... params) {

            resultStr = new ConnectBureau().getDataFromUrl(url);

            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
//            pDialog.dismiss();
//            chatAvatarModelHashMap.clear();
            android.util.Log.e("MessageListActivity", "chat list " + resultStr);
            totalFriendsCount = 0;
            try {
                if (resultStr != null && resultStr.length() > 1) {
                    String s = resultStr.substring(0, 1);
                    if (s.equalsIgnoreCase("{")) {
                        JSONObject jsonObject = new JSONObject(resultStr);
                        if (jsonObject.has("msg")) {
                            Toast.makeText(getActivity(), "Fetching friends list " + jsonObject.getString("response"), Toast.LENGTH_SHORT).show();
                            if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                                com.bureau.bureauapp.util.Util.invalidateUserID(mContext, activity);
                            }
                        }
                    } else if (s.equalsIgnoreCase("[")) {
                        JSONArray jsonArray = new JSONArray(resultStr);
                        totalFriendsCount = jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ChatAvatarModel chatAvatarModel = new ChatAvatarModel();
                            JSONObject jsonObjectChild = jsonArray.getJSONObject(i);
                            chatAvatarModel.setUserId(jsonObjectChild.getString("userid"));
                            chatAvatarModel.setFirst_Name(jsonObjectChild.getString("First Name"));
                            chatAvatarModel.setLast_Name(jsonObjectChild.getString("Last Name"));
                            chatAvatarModel.setRelation(jsonObjectChild.getString("relation"));
                            chatAvatarModel.setNotification_id(jsonObjectChild.getString("notification_id"));
                            chatAvatarModel.setDaily_match(jsonObjectChild.getString("daily_match"));
                            chatAvatarModel.setChat_notification(jsonObjectChild.getString("chat_notification"));
                            chatAvatarModel.setCustomer_service(jsonObjectChild.getString("customer_service"));
                            chatAvatarModel.setBlog_release(jsonObjectChild.getString("blog_release"));
                            chatAvatarModel.setSound(jsonObjectChild.getString("sound"));
                            chatAvatarModel.setImg_url(jsonObjectChild.getString("img_url"));
                            chatAvatarModel.setStatus(jsonObjectChild.getString("status"));
                            ChatData.saveString(mContext, jsonObjectChild.getString("userid"), jsonObjectChild.getString("First Name"));
                            ChatData.saveString(mContext, jsonObjectChild.getString("userid") + "I", jsonObjectChild.getString("img_url"));
//                            chatAvatarModelHashMap.put(jsonObjectChild.getString("userid"), chatAvatarModel);
                            android.util.Log.e("MessagesListActivity", "Userid " + jsonObjectChild.getString("userid"));
                        }
//                        android.util.Log.e("MessagesList","hashmap length "+chatAvatarModelHashMap.size());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            messageListCountTV.setText("" + totalFriendsCount);

//            initConversationActions();

        }
    }

    private void initConversationActions() {

        android.util.Log.e("ConversationsListFrag", "inside initConversationActions ==>> ");

        conversationsList.init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setInitialHistoricMessagesToFetch(10);

        conversationsList.setOnConversationClickListener(new ConversationsAdapter.OnConversationClickListener() {
            @Override
            public void onConversationClick(ConversationsAdapter adapter, Conversation conversation) {
                Intent intent = new Intent(getActivity(), MessagesListActivity.class);
                if (Log.isLoggable(Log.VERBOSE)) {
                    Log.v("Launching MessagesListActivity with existing conversation ID: " + conversation.getId());
                }
                intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, conversation.getId());
                startActivity(intent);
            }

            @Override
            public boolean onConversationLongClick(ConversationsAdapter adapter, Conversation conversation) {
                return false;
            }
        });
        conversationsList.setOnConversationSwipeListener(new SwipeableItem.OnSwipeListener<Conversation>() {
            @Override
            public void onSwipe(final Conversation conversation, int direction) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.alert_message_delete_conversation)
                        .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: simply update this one conversation
                                conversationsList.getAdapter().notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                conversation.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                            }
                        })
                        /*.setPositiveButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                conversation.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                            }
                        })*/
                        .show();
            }
        });

    }
}
