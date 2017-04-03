package com.bureau.bureauapp.layerclasses;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.bureau.bureauapp.adapters.ChatMessagesAdapter;
import com.layer.atlas.messagetypes.AtlasCellFactory;
import com.layer.atlas.messagetypes.MessageStyle;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.itemanimators.NoChangeAnimator;
import com.layer.atlas.util.views.SwipeableItem;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.squareup.picasso.Picasso;

public class MessagesRecyclerView extends RecyclerView {
    private ChatMessagesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ItemTouchHelper mSwipeItemTouchHelper;
    private MessageStyle mMessageStyle;

    public MessagesRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(getContext(), attrs, defStyle);
    }

    public MessagesRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessagesRecyclerView(Context context) {
        super(context);
    }

    public MessagesRecyclerView init(LayerClient layerClient, ParticipantProvider participantProvider, Picasso picasso) {
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        setLayoutManager(mLayoutManager);

        // Create an adapter that auto-scrolls if we're already at the bottom
        mAdapter = new ChatMessagesAdapter(getContext(), layerClient, participantProvider, picasso)
                .setRecyclerView(this)
                .setOnMessageAppendListener(new ChatMessagesAdapter.OnMessageAppendListener() {
                    @Override
                    public void onMessageAppend(ChatMessagesAdapter adapter, Message message) {
                        autoScroll();
                    }
                });
        mAdapter.setStyle(mMessageStyle);
        super.setAdapter(mAdapter);

        // Don't flash items when changing content
        setItemAnimator(new NoChangeAnimator());

        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (AtlasCellFactory factory : mAdapter.getCellFactories()) {
                    factory.onScrollStateChanged(newState);
                }
            }
        });

        return this;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new RuntimeException("AtlasMessagesRecyclerView sets its own Adapter");
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

    public MessagesRecyclerView refresh() {
        if (mAdapter != null) mAdapter.refresh();
        return this;
    }

    /**
     * Updates the underlying ChatMessagesAdapter with a Query for Messages in the given
     * Conversation.
     *
     * @param conversation Conversation to display Messages for.
     * @return This PavanAtlasMessagesRecyclerView.
     */
    public MessagesRecyclerView setConversation(Conversation conversation) {
        mAdapter.setQuery(Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.ASCENDING))
                .build()).refresh();
        return this;
    }

    public MessagesRecyclerView setOnMessageSwipeListener(SwipeableItem.OnSwipeListener<Message> listener) {
        if (mSwipeItemTouchHelper != null) {
            mSwipeItemTouchHelper.attachToRecyclerView(null);
        }
        if (listener == null) {
            mSwipeItemTouchHelper = null;
        } else {
            listener.setBaseAdapter((ChatMessagesAdapter) getAdapter());
            mSwipeItemTouchHelper = new ItemTouchHelper(listener);
            mSwipeItemTouchHelper.attachToRecyclerView(this);
        }
        return this;
    }

    /**
     * Convenience pass-through to this list's ChatMessagesAdapter.
     *
     * @see ChatMessagesAdapter#addCellFactories(AtlasCellFactory...)
     */
    public MessagesRecyclerView addCellFactories(AtlasCellFactory... cellFactories) {
        mAdapter.addCellFactories(cellFactories);
        return this;
    }

    public MessagesRecyclerView setTextTypeface(Typeface myTypeface, Typeface otherTypeface) {
        mMessageStyle.setMyTextTypeface(myTypeface);
        mMessageStyle.setOtherTextTypeface(otherTypeface);
        return this;
    }

    /**
     * Convenience pass-through to this list's LinearLayoutManager.
     *
     * @see LinearLayoutManager#findLastVisibleItemPosition()
     */
    private int findLastVisibleItemPosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

    /**
     * Convenience pass-through to this list's ChatMessagesAdapter.
     *
     * @see ChatMessagesAdapter#setFooterView(View)
     */
    public MessagesRecyclerView setFooterView(View footerView) {
        mAdapter.setFooterView(footerView);
        autoScroll();
        return this;
    }

    /**
     * Convenience pass-through to this list's ChatMessagesAdapter.
     *
     * @see ChatMessagesAdapter#getFooterView()
     */
    public View getFooterView() {
        return mAdapter.getFooterView();
    }

    /**
     * Scrolls if the user is at the end
     */
    private void autoScroll() {
        int end = mAdapter.getItemCount() - 1;
        if (end <= 0) return;
        int visible = findLastVisibleItemPosition();
        // -3 because -1 seems too finicky
        if (visible >= (end - 3)) scrollToPosition(end);
    }

    public void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, com.layer.atlas.R.styleable.AtlasMessagesRecyclerView, com.layer.atlas.R.attr.AtlasMessagesRecyclerView, defStyle);
        MessageStyle.Builder messageStyleBuilder = new MessageStyle.Builder();
        messageStyleBuilder.myTextColor(ta.getColor(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_myTextColor, context.getResources().getColor(com.layer.atlas.R.color.atlas_text_black)));
        int myTextStyle = ta.getInt(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_myTextStyle, Typeface.NORMAL);
        messageStyleBuilder.myTextStyle(myTextStyle);
        String myTextTypefaceName = ta.getString(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_myTextTypeface);
        messageStyleBuilder.myTextTypeface(myTextTypefaceName != null ? Typeface.create(myTextTypefaceName, myTextStyle) : null);
        messageStyleBuilder.myTextSize(ta.getDimensionPixelSize(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_myTextSize, context.getResources().getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_text_size_message_item)));

        messageStyleBuilder.otherTextColor(ta.getColor(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_theirTextColor, context.getResources().getColor(com.layer.atlas.R.color.atlas_color_primary_blue)));
        int otherTextStyle = ta.getInt(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_theirTextStyle, Typeface.NORMAL);
        messageStyleBuilder.otherTextStyle(otherTextStyle);
        String otherTextTypefaceName = ta.getString(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_theirTextTypeface);
        messageStyleBuilder.otherTextTypeface(otherTextTypefaceName != null ? Typeface.create(otherTextTypefaceName, otherTextStyle) : null);
        messageStyleBuilder.otherTextSize(ta.getDimensionPixelSize(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_theirTextSize, context.getResources().getDimensionPixelSize(com.layer.atlas.R.dimen.atlas_text_size_message_item)));

        messageStyleBuilder.myBubbleColor(ta.getColor(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_myBubbleColor, context.getResources().getColor(com.layer.atlas.R.color.atlas_color_primary_blue)));
        messageStyleBuilder.otherBubbleColor(ta.getColor(com.layer.atlas.R.styleable.AtlasMessagesRecyclerView_theirBubbleColor, context.getResources().getColor(com.layer.atlas.R.color.atlas_color_primary_gray)));

        ta.recycle();
        this.mMessageStyle = messageStyleBuilder.build();
    }
}