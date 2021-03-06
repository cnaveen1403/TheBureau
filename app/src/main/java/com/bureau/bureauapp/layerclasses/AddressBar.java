package com.bureau.bureauapp.layerclasses;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.RematchActivity;
import com.bureau.bureauapp.myapplication.ChatData;
import com.layer.atlas.AtlasAvatar;
import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.AvatarStyle;
import com.layer.atlas.util.EditTextUtil;
import com.layer.atlas.util.Util;
import com.layer.atlas.util.views.EmptyDelEditText;
import com.layer.atlas.util.views.FlowLayout;
import com.layer.atlas.util.views.MaxHeightScrollView;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.CompoundPredicate;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.sdk.query.SortDescriptor;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddressBar extends LinearLayout {
    private LayerClient mLayerClient;
    private ParticipantProvider mParticipantProvider;
    private Picasso mPicasso;

    private OnConversationClickListener mOnConversationClickListener;
    private OnParticipantSelectionChangeListener mOnParticipantSelectionChangeListener;

    private FlowLayout mSelectedParticipantLayout;
    private EmptyDelEditText mFilter;
    private RecyclerView mParticipantList;
    private AvailableConversationAdapter mAvailableConversationAdapter;
    private final Set<String> mSelectedParticipantIds = new LinkedHashSet<String>();

    private boolean mShowConversations;

    // styles
    private int mInputTextSize;
    private int mInputTextColor;
    private Typeface mInputTextTypeface;
    private int mInputTextStyle;
    private int mInputUnderlineColor;
    private int mInputCursorColor;
    private int mListTextSize;
    private int mListTextColor;
    private Typeface mListTextTypeface;
    private int mListTextStyle;
    private Typeface mChipTypeface;
    private int mChipStyle;
    private AvatarStyle mAvatarStyle;

    public AddressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseStyle(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(com.layer.atlas.R.layout.atlas_address_bar, this, true);
        mSelectedParticipantLayout = (FlowLayout) findViewById(com.layer.atlas.R.id.selected_participant_group);
        mFilter = (EmptyDelEditText) findViewById(com.layer.atlas.R.id.filter);
        mFilter.setVisibility(GONE);
//        mSelectedParticipantLayout.setStretchChild(mFilter);
        mParticipantList = (RecyclerView) findViewById(com.layer.atlas.R.id.participant_list);
//        ((MaxHeightScrollView) findViewById(com.layer.atlas.R.id.selected_participant_scroll)).setMaximumHeight(getResources().getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_selected_participant_group_max_height));
        setOrientation(VERTICAL);
    }

    public AddressBar init(LayerClient layerClient, ParticipantProvider participantProvider, Picasso picasso) {
        mLayerClient = layerClient;
        mParticipantProvider = participantProvider;
        mPicasso = picasso;

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mParticipantList.setLayoutManager(manager);
        mAvailableConversationAdapter = new AvailableConversationAdapter(mLayerClient, mParticipantProvider, mPicasso);

        applyStyle();

        mParticipantList.setAdapter(mAvailableConversationAdapter);

        // Hitting backspace with an empty search string deletes the last selected participant
        mFilter.setOnEmptyDelListener(new EmptyDelEditText.OnEmptyDelListener() {
            @Override
            public boolean onEmptyDel(EmptyDelEditText editText) {
                removeLastSelectedParticipant();
                return true;
            }
        });

        // Refresh available participants and conversations with every search string change
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable e) {
                refresh();
            }
        });
        return this;
    }

    public AddressBar addTextChangedListener(TextWatcher textWatcher) {
        mFilter.addTextChangedListener(textWatcher);
        return this;
    }

    public AddressBar removeTextChangedListener(TextWatcher textWatcher) {
        mFilter.removeTextChangedListener(textWatcher);
        return this;
    }

    public AddressBar setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        mFilter.setOnEditorActionListener(listener);
        return this;
    }

    public AddressBar setOnConversationClickListener(OnConversationClickListener onConversationClickListener) {
        mOnConversationClickListener = onConversationClickListener;
        return this;
    }

    public AddressBar setOnParticipantSelectionChangeListener(OnParticipantSelectionChangeListener onParticipantSelectionChangeListener) {
        mOnParticipantSelectionChangeListener = onParticipantSelectionChangeListener;
        return this;
    }

    public AddressBar setSuggestionsVisibility(int visibility) {
        mParticipantList.setVisibility(visibility);
        return this;
    }

    public AddressBar setTypeface(Typeface inputTextTypeface, Typeface listTextTypeface,
                                  Typeface avatarTextTypeface, Typeface chipTypeface) {
        this.mInputTextTypeface = inputTextTypeface;
        this.mListTextTypeface = listTextTypeface;
        this.mChipTypeface = chipTypeface;
        this.mAvatarStyle.setAvatarTextTypeface(avatarTextTypeface);
        applyTypeface();
        return this;
    }

    public Set<String> getSelectedParticipantIds() {
        return new LinkedHashSet<String>(mSelectedParticipantIds);
    }

    public AddressBar refresh() {
        if (mAvailableConversationAdapter == null) return this;
        mAvailableConversationAdapter.refresh(getSearchFilter(), mSelectedParticipantIds);
        return this;
    }

    public AddressBar setShowConversations(boolean showConversations) {
        this.mShowConversations = showConversations;
        return this;
    }

    public AddressBar setSelectedParticipants(Set<String> selectedParticipants) {
        mSelectedParticipantIds.addAll(selectedParticipants);
        return this;
    }

    public void requestFilterFocus() {
        mFilter.requestFocus();
    }

    private boolean selectParticipant(String participantId) {
        if (mSelectedParticipantIds.contains(participantId)) return true;
        if (mSelectedParticipantIds.size() >= 1) return false;
        mSelectedParticipantIds.add(participantId);
        ParticipantChip chip = new ParticipantChip(getContext(), mParticipantProvider, participantId, mPicasso);
        mSelectedParticipantLayout.addView(chip, mSelectedParticipantLayout.getChildCount() - 1);
        mFilter.setText(null);
        refresh();
        if (mOnParticipantSelectionChangeListener != null) {
            mOnParticipantSelectionChangeListener.onParticipantSelectionChanged(this, new ArrayList<String>(mSelectedParticipantIds));
        }
        return true;
    }

    private void unselectParticipant(ParticipantChip chip) {
        if (!mSelectedParticipantIds.contains(chip.mParticipantId)) return;
        mSelectedParticipantIds.remove(chip.mParticipantId);
        mSelectedParticipantLayout.removeView(chip);
        refresh();
        if (mOnParticipantSelectionChangeListener != null) {
            mOnParticipantSelectionChangeListener.onParticipantSelectionChanged(this, new ArrayList<String>(mSelectedParticipantIds));
        }
    }

    private String getSearchFilter() {
        String s = mFilter.getText().toString();
        return s.trim().isEmpty() ? null : s;
    }

    private String removeLastSelectedParticipant() {
        ParticipantChip lastChip = null;
        for (int i = 0; i < mSelectedParticipantLayout.getChildCount(); i++) {
            View v = mSelectedParticipantLayout.getChildAt(i);
            if (v instanceof ParticipantChip) lastChip = (ParticipantChip) v;
        }
        if (lastChip == null) return null;
        unselectParticipant(lastChip);
        return lastChip.mParticipantId;
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, com.layer.atlas.R.styleable.AtlasAddressBar, com.layer.atlas.R.attr.AtlasAddressBar, defStyle);
        Resources resources = context.getResources();
        this.mInputTextSize = ta.getDimensionPixelSize(com.layer.atlas.R.styleable.AtlasAddressBar_inputTextSize, resources.getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_text_size_input));
        this.mInputTextColor = ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_inputTextColor, resources.getColor(com.layer.atlas.R.color.atlas_text_black));
        this.mInputTextStyle = ta.getInt(com.layer.atlas.R.styleable.AtlasAddressBar_inputTextStyle, Typeface.NORMAL);
        String inputTextTypefaceName = ta.getString(com.layer.atlas.R.styleable.AtlasAddressBar_inputTextTypeface);
        this.mInputTextTypeface = inputTextTypefaceName != null ? Typeface.create(inputTextTypefaceName, mInputTextStyle) : null;
        this.mInputCursorColor = ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_inputCursorColor, resources.getColor(com.layer.atlas.R.color.atlas_color_primary_blue));
        this.mInputUnderlineColor = ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_inputUnderlineColor, resources.getColor(com.layer.atlas.R.color.atlas_color_primary_blue));

        this.mListTextSize = ta.getDimensionPixelSize(com.layer.atlas.R.styleable.AtlasAddressBar_listTextSize, resources.getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_text_size_secondary_item));
        this.mListTextColor = ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_listTextColor, resources.getColor(com.layer.atlas.R.color.atlas_text_black));
        this.mListTextStyle = ta.getInt(com.layer.atlas.R.styleable.AtlasAddressBar_listTextStyle, Typeface.NORMAL);
        String listTextTypefaceName = ta.getString(com.layer.atlas.R.styleable.AtlasAddressBar_listTextTypeface);
        this.mListTextTypeface = listTextTypefaceName != null ? Typeface.create(listTextTypefaceName, mInputTextStyle) : null;

        this.mChipStyle = ta.getInt(com.layer.atlas.R.styleable.AtlasAddressBar_chipStyle, Typeface.NORMAL);
        String chipTypefaceName = ta.getString(com.layer.atlas.R.styleable.AtlasAddressBar_chipTypeface);
        this.mChipTypeface = chipTypefaceName != null ? Typeface.create(chipTypefaceName, mChipStyle) : null;

        AvatarStyle.Builder avatarStyleBuilder = new AvatarStyle.Builder();
        avatarStyleBuilder.avatarBackgroundColor(ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_avatarBackgroundColor, resources.getColor(com.layer.atlas.R.color.atlas_avatar_background)));
        avatarStyleBuilder.avatarTextColor(ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_avatarTextColor, resources.getColor(com.layer.atlas.R.color.atlas_avatar_text)));
        avatarStyleBuilder.avatarBorderColor(ta.getColor(com.layer.atlas.R.styleable.AtlasAddressBar_avatarBorderColor, resources.getColor(com.layer.atlas.R.color.atlas_avatar_border)));
        int avatarTextStyle = ta.getInt(com.layer.atlas.R.styleable.AtlasAddressBar_avatarTextStyle, Typeface.NORMAL);
        String avatarTextTypefaceName = ta.getString(com.layer.atlas.R.styleable.AtlasAddressBar_avatarTextTypeface);
        avatarStyleBuilder.avatarTextTypeface(inputTextTypefaceName != null ? Typeface.create(avatarTextTypefaceName, avatarTextStyle) : null);
        this.mAvatarStyle = avatarStyleBuilder.build();

        ta.recycle();
    }

    private void applyStyle() {
        mFilter.setTextColor(mInputTextColor);
        mFilter.setTextSize(TypedValue.COMPLEX_UNIT_PX, mInputTextSize);
        EditTextUtil.setCursorDrawableColor(mFilter, mInputCursorColor);
        EditTextUtil.setUnderlineColor(mFilter, mInputUnderlineColor);
        applyTypeface();
    }

    private void applyTypeface() {
        mFilter.setTypeface(mInputTextTypeface, mInputTextStyle);
        mAvailableConversationAdapter.notifyDataSetChanged();
    }

    /**
     * Automatically refresh on resume
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) return;
        refresh();
    }

    /**
     * Save selected participants
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (mSelectedParticipantIds.isEmpty()) return superState;
        SavedState savedState = new SavedState(superState);
        savedState.mSelectedParticipantIds = new ArrayList<String>(mSelectedParticipantIds);
        return savedState;
    }

    /**
     * Restore selected participants
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.mSelectedParticipantIds != null) {
            mSelectedParticipantIds.clear();
            for (String participantId : savedState.mSelectedParticipantIds) {
                selectParticipant(participantId);
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        List<String> mSelectedParticipantIds;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringList(mSelectedParticipantIds);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            mSelectedParticipantIds = in.createStringArrayList();
        }
    }

    /**
     * ParticipantChip implements the View used to populate the selected participant FlowLayout.
     */
    private class ParticipantChip extends LinearLayout {
        private String mParticipantId;

        private AtlasAvatar mAvatar;
        private TextView mName;
        private ImageView mRemove;

        public ParticipantChip(Context context, ParticipantProvider participantProvider, String participantId, Picasso picasso) {
            super(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            Resources r = getContext().getResources();
            mParticipantId = participantId;

            // Inflate and cache views
            inflater.inflate(com.layer.atlas.R.layout.atlas_participant_chip, this, true);
            mAvatar = (AtlasAvatar) findViewById(com.layer.atlas.R.id.avatar);
            mName = (TextView) findViewById(com.layer.atlas.R.id.name);
            mRemove = (ImageView) findViewById(com.layer.atlas.R.id.remove);

            // Set Style
            mName.setTypeface(mChipTypeface);

            // Set layout
            int height = r.getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_chip_height);
            int margin = r.getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_chip_margin);
            FlowLayout.LayoutParams p = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
            p.setMargins(margin, margin, margin, margin);
            setLayoutParams(p);
            setOrientation(HORIZONTAL);
            setBackgroundDrawable(r.getDrawable(com.layer.atlas.R.drawable.atlas_participant_chip_background));

            // Initialize participant data

//            Toast.makeText(context,participant.getName(),Toast.LENGTH_SHORT).show();
//            ChatAvatarModel chatAvatarModel = ConversationsListFragment.chatAvatarModelHashMap.get(participant.getName());
//            BaseActivity.actionBar.setTitle(ChatData.getString(context,participant.getName()));
            final Participant participant = participantProvider.getParticipant(participantId);
            mName.setText(participant.getName());
            BaseActivity.mTitle.setText(participant.getName());
            BaseActivity.mProfileImage.setVisibility(VISIBLE);
            String image = ChatData.getString(getContext(), participant.getId() + "I");
            image = image.replace("\\", "");
            image = image.replace("\"", "");
            Glide.with(getContext()).load(image).placeholder(R.drawable.user).error(R.drawable.user).into(BaseActivity.mProfileImage);

//            Glide.with(context).load(ChatData.getString()).placeholder(R.drawable.user).error(R.drawable.user).into(BaseActivity.mProfileImage);

            BaseActivity.mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUserPage(participant.getId());
                }
            });
            BaseActivity.mProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUserPage(participant.getId());
                }
            });

            mAvatar.init(participantProvider, picasso)
                    .setStyle(mAvatarStyle)
                    .setParticipants(participantId);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    unselectParticipant(ParticipantChip.this);
                }
            });
        }

        private void goToUserPage(String value) {
//            Toast.makeText(getContext(), ""+value, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), RematchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("fromActivity", "Mssage_activity");
            intent.putExtra("userId", value);
            getContext().startActivity(intent);
        }
    }

    private enum Type {
        PARTICIPANT,
        CONVERSATION
    }

    /**
     * AvailableConversationAdapter provides items for individual Participants and existing
     * Conversations.  Items are filtered by a participant filter string and by a set of selected
     * Participants.
     */
    private class AvailableConversationAdapter extends RecyclerView.Adapter<AvailableConversationAdapter.ViewHolder> implements RecyclerViewController.Callback {
        protected final LayerClient mLayerClient;
        protected final ParticipantProvider mParticipantProvider;
        protected final Picasso mPicasso;
        private final RecyclerViewController<Conversation> mQueryController;

        private final ArrayList<String> mParticipantIds = new ArrayList<String>();
        private final ArrayList<Participant> mParticipants = new ArrayList<Participant>();
        private final Map<String, Participant> mParticipantMap = new HashMap<String, Participant>();

        public AvailableConversationAdapter(LayerClient client, ParticipantProvider participantProvider, Picasso picasso) {
            this(client, participantProvider, picasso, null);
        }

        public AvailableConversationAdapter(LayerClient client, ParticipantProvider participantProvider, Picasso picasso, Collection<String> updateAttributes) {
            mQueryController = client.newRecyclerViewController(null, updateAttributes, this);
            mLayerClient = client;
            mParticipantProvider = participantProvider;
            mPicasso = picasso;
            setHasStableIds(false);
        }

        /**
         * Refreshes this adapter by re-querying the ParticipantProvider and filtering Conversations
         * to return only those Conversations with the given set of selected Participants.
         */
        public void refresh(String filter, Set<String> selectedParticipantIds) {
            // Apply text search filter to available participants
            String userId = mLayerClient.getAuthenticatedUserId();
            synchronized (mParticipantIds) {
                mParticipantProvider.getMatchingParticipants(filter, mParticipantMap);
                mParticipants.clear();
                for (Map.Entry<String, Participant> entry : mParticipantMap.entrySet()) {
                    // Don't show participants we've already selected
                    if (selectedParticipantIds.contains(entry.getKey())) continue;
                    if (entry.getKey().equals(userId)) continue;
                    mParticipants.add(entry.getValue());
                }
                Collections.sort(mParticipants);

                mParticipantIds.clear();
                for (Participant p : mParticipants) {
                    mParticipantIds.add(p.getId());
                }
            }
            // TODO: compute add/remove/move and notify those instead of complete notify
            notifyDataSetChanged();

            if (mShowConversations) {
                queryConversations(selectedParticipantIds);
            }
        }

        private void queryConversations(Set<String> selectedParticipantIds) {
            // Filter down to only those conversations including the selected participants, hiding one-on-one conversations
            Query.Builder<Conversation> builder = Query.builder(Conversation.class)
                    .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_SENT_AT, SortDescriptor.Order.DESCENDING));
            if (selectedParticipantIds.isEmpty()) {
                builder.predicate(new Predicate(Conversation.Property.PARTICIPANT_COUNT, Predicate.Operator.GREATER_THAN, 2));
            } else {
                builder.predicate(new CompoundPredicate(CompoundPredicate.Type.AND,
                        new Predicate(Conversation.Property.PARTICIPANT_COUNT, Predicate.Operator.GREATER_THAN, 2),
                        new Predicate(Conversation.Property.PARTICIPANTS, Predicate.Operator.IN, selectedParticipantIds)));
            }
            mQueryController.setQuery(builder.build()).execute();
        }

        //==============================================================================================
        // Adapter
        //==============================================================================================

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder viewHolder = new ViewHolder(parent);
           /* viewHolder.mAvatar
                    .init(mParticipantProvider, mPicasso)
                    .setStyle(mAvatarStyle);*/
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            synchronized (mParticipantIds) {
                switch (getType(position)) {
                    case PARTICIPANT: {
                        position = adapterPositionToParticipantPosition(position);
                        Participant participant = mParticipants.get(position);
//                        if (ChatData.containsKey(getContext(), participant.getName())) {
                        if (participant.getName() != null && !participant.getName().equalsIgnoreCase("")) {
                            String participantId = mParticipantIds.get(position);
                            Log.e("PavanAtlasAddressBar", "Userid " + participant.getName());
                            viewHolder.mTitle.setText(participant.getName());
                            String image = ChatData.getString(getContext(), participant.getId() + "I");
                            image = image.replace("\\", "");
                            image = image.replace("\"", "");
                            Glide.with(getContext()).load(image).placeholder(R.drawable.user).error(R.drawable.user).into(viewHolder.userImage);
                            viewHolder.itemView.setTag(participantId);
                            viewHolder.itemView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    selectParticipant((String) v.getTag());
                                }
                            });
//                            viewHolder.mAvatar.setParticipants(participantId);
                        } else {
                            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) viewHolder.mainLL.getLayoutParams();
                            param.height = 0;
                            param.width = 0;
                            viewHolder.mainLL.setVisibility(View.GONE);
                            viewHolder.mainLL.setLayoutParams(param);
                        }
                    }
                    break;

                    case CONVERSATION: {
                        position = adapterPositionToConversationPosition(position);
                        Conversation conversation = mQueryController.getItem(position);
                        String userId = mLayerClient.getAuthenticatedUserId();
                        List<String> names = new ArrayList<String>();
                        Set<String> ids = new LinkedHashSet<String>();
                        for (String participantId : conversation.getParticipants()) {
                            if (participantId.equals(userId)) continue;
                            ids.add(participantId);
                            Participant p = mParticipantProvider.getParticipant(participantId);
                            if (p == null) continue;
                            names.add(p.getName());
                        }
                        viewHolder.mTitle.setText(TextUtils.join(", ", names));
                        viewHolder.itemView.setTag(conversation);
                        viewHolder.itemView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mOnConversationClickListener == null) return;
                                mOnConversationClickListener.onConversationClick(AddressBar.this, (Conversation) v.getTag());
                            }
                        });
//                        viewHolder.mAvatar.setParticipants(ids);
                    }
                    break;
                }
            }
        }

        // first are participants; then are conversations
        Type getType(int position) {
            synchronized (mParticipantIds) {
                return (position < mParticipantIds.size()) ? Type.PARTICIPANT : Type.CONVERSATION;
            }
        }

        int adapterPositionToParticipantPosition(int position) {
            return position;
        }

        int adapterPositionToConversationPosition(int position) {
            synchronized (mParticipantIds) {
                return position - mParticipantIds.size();
            }
        }

        int conversationPositionToAdapterPosition(int position) {
            synchronized (mParticipantIds) {
                return position + mParticipantIds.size();
            }
        }

        @Override
        public int getItemCount() {
            synchronized (mParticipantIds) {
                return mQueryController.getItemCount() + mParticipantIds.size();
            }
        }

        //==============================================================================================
        // Conversation UI update callbacks
        //==============================================================================================

        @Override
        public void onQueryDataSetChanged(RecyclerViewController controller) {
            notifyDataSetChanged();
        }

        @Override
        public void onQueryItemChanged(RecyclerViewController controller, int position) {
            notifyItemChanged(conversationPositionToAdapterPosition(position));
        }

        @Override
        public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
            notifyItemRangeChanged(conversationPositionToAdapterPosition(positionStart), itemCount);
        }

        @Override
        public void onQueryItemInserted(RecyclerViewController controller, int position) {
            notifyItemInserted(conversationPositionToAdapterPosition(position));
        }

        @Override
        public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
            notifyItemRangeInserted(conversationPositionToAdapterPosition(positionStart), itemCount);
        }

        @Override
        public void onQueryItemRemoved(RecyclerViewController controller, int position) {
            notifyItemRemoved(conversationPositionToAdapterPosition(position));
        }

        @Override
        public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
            notifyItemRangeRemoved(conversationPositionToAdapterPosition(positionStart), itemCount);
        }

        @Override
        public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
            notifyItemMoved(conversationPositionToAdapterPosition(fromPosition), conversationPositionToAdapterPosition(toPosition));
        }

        //==============================================================================================
        // Inner classes
        //==============================================================================================

        protected class ViewHolder extends RecyclerView.ViewHolder {
            //            private AtlasAvatar mAvatar;
            private TextView mTitle;
            private LinearLayout mainLL;
            ImageView userImage;

            public ViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_item, parent, false));
//                mAvatar = (AtlasAvatar) itemView.findViewById(com.layer.atlas.R.id.avatar);
                mTitle = (TextView) itemView.findViewById(R.id.item_title_TV);
                userImage = (ImageView) itemView.findViewById(R.id.profile_image);
                mainLL = (LinearLayout) itemView.findViewById(R.id.mainLL);
                mTitle.setTextColor(mListTextColor);
                mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mListTextSize);
                mTitle.setTypeface(mListTextTypeface, mListTextStyle);
            }
        }
    }

    public interface OnConversationClickListener {
        void onConversationClick(AddressBar conversationLauncher, Conversation conversation);
    }

    public interface OnParticipantSelectionChangeListener {
        void onParticipantSelectionChanged(AddressBar conversationLauncher, List<String> participantIds);
    }
}