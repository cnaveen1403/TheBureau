package com.bureau.bureauapp.layerclasses;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.RematchActivity;
import com.bureau.bureauapp.model.ChatAvatarModel;
import com.bureau.bureauapp.myapplication.ChatData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.bureau.bureauapp.util.LayerUtils;
import com.layer.atlas.AtlasHistoricMessagesFetchLayout;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.atlas.messagetypes.generic.GenericCellFactory;
import com.layer.atlas.messagetypes.location.LocationCellFactory;
import com.layer.atlas.messagetypes.location.LocationSender;
import com.layer.atlas.messagetypes.singlepartimage.SinglePartImageCellFactory;
import com.layer.atlas.messagetypes.text.TextCellFactory;
import com.layer.atlas.messagetypes.threepartimage.CameraSender;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.atlas.typingindicators.BubbleTypingIndicatorFactory;
import com.layer.atlas.util.Util;
import com.layer.atlas.util.views.SwipeableItem;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerConversationException;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.ConversationOptions;
import com.layer.sdk.messaging.Message;

import java.util.HashMap;
import java.util.List;

public class MessagesListActivity extends BaseActivity {
    private UiState mState;
    private Conversation mConversation;

    Typeface mTypeface;
    private AddressBar mAddressBar;
    private AtlasHistoricMessagesFetchLayout mHistoricFetchLayout;
    private MessagesRecyclerView mMessagesList;
    private AtlasTypingIndicator mTypingIndicator;
    private MessageComposer mMessageComposer;
    HashMap<String, ChatAvatarModel> chatAvatarModelHashMap = new HashMap<>();

    public MessagesListActivity() {
        super(R.layout.activity_messages_list, R.mipmap.ic_launcher, R.string.title_select_conversation, true);
    }

    private void setUiState(UiState state) {
        if (mState == state) return;
        mState = state;
        switch (state) {
            case ADDRESS:
                mAddressBar.setVisibility(View.VISIBLE);
                mAddressBar.setSuggestionsVisibility(View.VISIBLE);
                mHistoricFetchLayout.setVisibility(View.GONE);
                mMessageComposer.setVisibility(View.GONE);
                mAddressBar.setTypeface(mTypeface, mTypeface, mTypeface, mTypeface);
                break;

            case ADDRESS_COMPOSER:
                mAddressBar.setVisibility(View.GONE);
                mAddressBar.setSuggestionsVisibility(View.VISIBLE);
                mHistoricFetchLayout.setVisibility(View.GONE);
                mMessageComposer.setVisibility(View.VISIBLE);
                mAddressBar.setTypeface(mTypeface, mTypeface, mTypeface, mTypeface);
                break;

            case ADDRESS_CONVERSATION_COMPOSER:
                mAddressBar.setVisibility(View.VISIBLE);
                mAddressBar.setSuggestionsVisibility(View.GONE);
                mHistoricFetchLayout.setVisibility(View.VISIBLE);
                mMessageComposer.setVisibility(View.VISIBLE);
                mAddressBar.setTypeface(mTypeface, mTypeface, mTypeface, mTypeface);
                break;

            case CONVERSATION_COMPOSER:
                mAddressBar.setVisibility(View.GONE);
                mAddressBar.setSuggestionsVisibility(View.GONE);
                mHistoricFetchLayout.setVisibility(View.VISIBLE);
                mMessageComposer.setVisibility(View.VISIBLE);
                mAddressBar.setTypeface(mTypeface, mTypeface, mTypeface, mTypeface);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_messages_list);
        mTypeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Comfortaa-Regular.ttf");
        if (MyApplication.routeLogin(this)) {
            if (!isFinishing()) finish();
            return;
        }

        MyApplication.changeLayerInAppNotification(false);

      /*  ((ImageView) findViewById(R.id.backIconIMG)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
//        ((TextView)findViewById(R.id.title)).setText(R.string.title_select_conversation);

        mAddressBar = ((AddressBar) findViewById(R.id.conversation_launcher))
                .init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setOnConversationClickListener(new AddressBar.OnConversationClickListener() {
                    @Override
                    public void onConversationClick(AddressBar addressBar, Conversation conversation) {
                        setConversation(conversation, true);
                        setTitle(true);
                    }
                })
                .setOnParticipantSelectionChangeListener(new AddressBar.OnParticipantSelectionChangeListener() {
                    @Override
                    public void onParticipantSelectionChanged(AddressBar addressBar, final List<String> participantIds) {
                        if (participantIds.isEmpty()) {
                            setConversation(null, false);
                            return;
                        }
                        try {
                            setConversation(MyApplication.getLayerClient().newConversation(new ConversationOptions().distinct(true), participantIds), false);
                            return;
                        } catch (LayerConversationException e) {
                            setConversation(e.getConversation(), false);
                        }
                    }
                })
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (mState == UiState.ADDRESS_CONVERSATION_COMPOSER) {
                            mAddressBar.setSuggestionsVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }
                })
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            setUiState(UiState.CONVERSATION_COMPOSER);
                            setTitle(true);
                            return true;
                        }
                        return false;
                    }
                });

        mHistoricFetchLayout = ((AtlasHistoricMessagesFetchLayout) findViewById(R.id.historic_sync_layout))
                .init(MyApplication.getLayerClient())
                .setHistoricMessagesPerFetch(20);

        mMessagesList = ((MessagesRecyclerView) findViewById(R.id.messages_list))
                .init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setTextTypeface(mTypeface, mTypeface)
                .addCellFactories(
                        new TextCellFactory(),
                        new ThreePartImageCellFactory(this, MyApplication.getLayerClient(), MyApplication.getPicasso()),
//                        new LocationCellFactory(this, MyApplication.getPicasso()),
                        new SinglePartImageCellFactory(this, MyApplication.getLayerClient(), MyApplication.getPicasso()),
                        new GenericCellFactory())
                .setOnMessageSwipeListener(new SwipeableItem.OnSwipeListener<Message>() {
                    @Override
                    public void onSwipe(final Message message, int direction) {
                        new AlertDialog.Builder(MessagesListActivity.this)
                                .setMessage(R.string.alert_message_delete_message)
                                .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO: simply update this one message
                                        mMessagesList.getAdapter().notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                })
                                .setNeutralButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        message.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                                    }
                                })
                                .setPositiveButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        message.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                                    }
                                })
                                .show();
                    }
                });

        mTypingIndicator = new AtlasTypingIndicator(this)
                .init(MyApplication.getLayerClient())
                .setTypingIndicatorFactory(new BubbleTypingIndicatorFactory())
                .setTypingActivityListener(new AtlasTypingIndicator.TypingActivityListener() {
                    @Override
                    public void onTypingActivityChange(AtlasTypingIndicator typingIndicator, boolean active) {
                        mMessagesList.setFooterView(active ? typingIndicator : null);
                    }
                });

        mMessageComposer = ((MessageComposer) findViewById(R.id.message_composer))
                .init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider())
                .setTextSender(new TextSender())
                .setTypeface(mTypeface)
                .addAttachmentSenders(
                        new BureauCameraSender(R.string.attachment_menu_camera, R.drawable.ic_photo_camera_white_24dp, this),
                        new BureauGallerySender(R.string.attachment_menu_gallery, R.drawable.ic_photo_white_24dp, this))
                .setOnMessageEditTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            setUiState(UiState.CONVERSATION_COMPOSER);
                            setTitle(true);
                        }
                    }
                });

        // Get or create Conversation from Intent extras
        Conversation conversation = null;
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY)) {
                Uri conversationId = intent.getParcelableExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY);
                conversation = MyApplication.getLayerClient().getConversation(conversationId);

            } else if (intent.hasExtra("participantIds")) {
                String[] participantIds = intent.getStringArrayExtra("participantIds");
                try {
                    conversation = MyApplication.getLayerClient().newConversation(new ConversationOptions().distinct(true), participantIds);
                } catch (LayerConversationException e) {
                    conversation = e.getConversation();
                }
            }
        }
        setConversation(conversation, conversation != null);
//        ((TextView)findViewById(R.id.title)).setText(Util.getConversationTitle(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation));

        setTitle(mConversation != null);
    }


    @Override
    protected void onResume() {
        // Clear any notifications for this conversation
        PushNotificationReceiver.getNotifications(this).clear(mConversation);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Update the notification position to the latest seen
        PushNotificationReceiver.getNotifications(this).clear(mConversation);
        MyApplication.changeLayerInAppNotification(true);
        super.onPause();
    }

    public void setTitle(boolean useConversation) {
        if (!useConversation) {
//            setTitle(R.string.title_conversations_list);
            mTitle.setText(R.string.title_conversations_list);
            mProfileImage.setVisibility(View.GONE);
        } else {
//            setTitle(Util.getConversationTitle(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation));
            mTitle.setText(Util.getConversationTitle(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation));
            mProfileImage.setVisibility(View.VISIBLE);
//            Glide.with(getApplicationContext()).load(AppData.getString(getApplicationContext(),Util.getConversationTitle(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation)+"I")).placeholder(R.drawable.user).error(R.drawable.user).into(mProfileImage);
//            Glide.with(getApplicationContext()).load(MyApplication.getParticipantProvider().getParticipant(Util.getConversationTitle(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation)).getAvatarUrl()).placeholder(R.drawable.user).error(R.drawable.user).into(mProfileImage);
            Glide.with(getApplicationContext()).load(ChatData.getString(getApplicationContext(), LayerUtils.getConversationId(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation) + "I")).placeholder(R.drawable.user).error(R.drawable.user).into(mProfileImage);

            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    goToUserPage(Util.getConversationTitle(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation));
                    goToUserPage(LayerUtils.getConversationId(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation));
                }
            });
            mProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUserPage(LayerUtils.getConversationId(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), mConversation));
                }
            });

        }
    }

    private void goToUserPage(String value) {
        Intent intent = new Intent(MessagesListActivity.this, RematchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("fromActivity", "Mssage_activity");
        intent.putExtra("userId", value);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setConversation(Conversation conversation, boolean hideLauncher) {
        mConversation = conversation;
        mHistoricFetchLayout.setConversation(conversation);
        mMessagesList.setConversation(conversation);
        mTypingIndicator.setConversation(conversation);
        mMessageComposer.setConversation(conversation);

        // UI state
        if (conversation == null) {
            setUiState(UiState.ADDRESS);
            return;
        }

        if (hideLauncher) {
            setUiState(UiState.CONVERSATION_COMPOSER);
            return;
        }

        if (conversation.getHistoricSyncStatus() == Conversation.HistoricSyncStatus.INVALID) {
            // New "temporary" conversation
            setUiState(UiState.CONVERSATION_COMPOSER);
        } else {
            setUiState(UiState.CONVERSATION_COMPOSER);
        }
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_details:
                if (mConversation == null) return true;
                Intent intent = new Intent(this, ConversationSettingsActivity.class);
                intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, mConversation.getId());
                startActivity(intent);
                return true;

            case R.id.action_sendlogs:
                LayerClient.sendLogs(MyApplication.getLayerClient(), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mMessageComposer.onActivityResult(this, requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mMessageComposer.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private enum UiState {
        ADDRESS,
        ADDRESS_COMPOSER,
        ADDRESS_CONVERSATION_COMPOSER,
        CONVERSATION_COMPOSER
    }

}