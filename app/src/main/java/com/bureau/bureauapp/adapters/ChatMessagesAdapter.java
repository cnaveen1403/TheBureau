package com.bureau.bureauapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.layer.atlas.AtlasAvatar;
import com.layer.atlas.adapters.AtlasBaseAdapter;
import com.layer.atlas.messagetypes.AtlasCellFactory;
import com.layer.atlas.messagetypes.MessageStyle;
import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.Util;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Actor;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.ListViewController;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder> implements AtlasBaseAdapter<Message>, RecyclerViewController.Callback {
    private final static int VIEW_TYPE_FOOTER = 0;

    protected final LayerClient mLayerClient;
    protected final ParticipantProvider mParticipantProvider;
    protected final Picasso mPicasso;
    private final RecyclerViewController<Message> mQueryController;
    protected final LayoutInflater mLayoutInflater;
    protected final Handler mUiThreadHandler;
    protected OnMessageAppendListener mAppendListener;
    protected final DisplayMetrics mDisplayMetrics;

    // Cells
    protected int mViewTypeCount = VIEW_TYPE_FOOTER;
    protected final Set<AtlasCellFactory> mCellFactories = new LinkedHashSet<AtlasCellFactory>();
    protected final Map<Integer, CellType> mCellTypesByViewType = new HashMap<Integer, CellType>();
    protected final Map<AtlasCellFactory, Integer> mMyViewTypesByCell = new HashMap<AtlasCellFactory, Integer>();
    protected final Map<AtlasCellFactory, Integer> mTheirViewTypesByCell = new HashMap<AtlasCellFactory, Integer>();

    // Dates and Clustering
    private final Map<Uri, Cluster> mClusterCache = new HashMap<Uri, Cluster>();
    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;

    // Read and delivery receipts
    private Map<Message.RecipientStatus, MessagePosition> mReceiptMap = new HashMap<Message.RecipientStatus, MessagePosition>();

    private View mFooterView;
    private int mFooterPosition = 0;

    //Stye
    private MessageStyle mMessageStyle;

    private RecyclerView mRecyclerView;

    public ChatMessagesAdapter(Context context, LayerClient layerClient, ParticipantProvider participantProvider, Picasso picasso) {
        mLayerClient = layerClient;
        mParticipantProvider = participantProvider;
        mPicasso = picasso;
        mLayoutInflater = LayoutInflater.from(context);
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        mDisplayMetrics = context.getResources().getDisplayMetrics();

        mQueryController = layerClient.newRecyclerViewController(null, null, this);
        mQueryController.setPreProcessCallback(new ListViewController.PreProcessCallback<Message>() {
            @Override
            public void onCache(ListViewController listViewController, Message message) {
                for (AtlasCellFactory factory : mCellFactories) {
                    if (factory.isBindable(message)) {
                        factory.getParsedContent(mLayerClient, mParticipantProvider, message);
                        break;
                    }
                }
            }
        });

        setHasStableIds(false);
    }

    /**
     * Sets this ChatMessagesAdapter's Message Query.
     *
     * @param query Query drive this ChatMessagesAdapter.
     * @return This ChatMessagesAdapter.
     */
    public ChatMessagesAdapter setQuery(Query<Message> query) {
        mQueryController.setQuery(query);
        return this;
    }

    /**
     * Refreshes this adapter by re-running the underlying Query.
     */
    public void refresh() {
        mQueryController.execute();
    }

    public ChatMessagesAdapter setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        return this;
    }

    public void setStyle(MessageStyle messageStyle) {
        this.mMessageStyle = messageStyle;
    }

    public void setFooterView(View footerView) {
        boolean isNull = footerView == null;
        boolean wasNull = mFooterView == null;
        mFooterView = footerView;

        if (wasNull && !isNull) {
            // Insert
            notifyItemInserted(mFooterPosition);
        } else if (!wasNull && isNull) {
            // Delete
            notifyItemRemoved(mFooterPosition);
        } else if (!wasNull && !isNull) {
            // Change
            notifyItemChanged(mFooterPosition);
        }
    }

    public View getFooterView() {
        return mFooterView;
    }


    //==============================================================================================
    // Listeners
    //==============================================================================================

    /**
     * Sets the OnAppendListener for this AtlasQueryAdapter.  The listener will be called when items
     * are appended to the end of this adapter.  This is useful for implementing a scroll-to-bottom
     * feature.
     *
     * @param listener The OnAppendListener to notify about appended items.
     * @return This AtlasQueryAdapter.
     */
    public ChatMessagesAdapter setOnMessageAppendListener(OnMessageAppendListener listener) {
        mAppendListener = listener;
        return this;
    }


    //==============================================================================================
    // Adapter and Cells
    //==============================================================================================

    /**
     * Registers one or more CellFactories for the ChatMessagesAdapter to manage.  CellFactories
     * know which Messages they can render, and handle View caching, creation, and binding.
     *
     * @param cellFactories Cells to register.
     * @return This ChatMessagesAdapter.
     */
    public ChatMessagesAdapter addCellFactories(AtlasCellFactory... cellFactories) {
        for (AtlasCellFactory CellFactory : cellFactories) {
            CellFactory.setStyle(mMessageStyle);
            mCellFactories.add(CellFactory);

            mViewTypeCount++;
            CellType me = new CellType(true, CellFactory);
            mCellTypesByViewType.put(mViewTypeCount, me);
            mMyViewTypesByCell.put(CellFactory, mViewTypeCount);

            mViewTypeCount++;
            CellType notMe = new CellType(false, CellFactory);
            mCellTypesByViewType.put(mViewTypeCount, notMe);
            mTheirViewTypesByCell.put(CellFactory, mViewTypeCount);
        }
        return this;
    }

    public Set<AtlasCellFactory> getCellFactories() {
        return mCellFactories;
    }

    @Override
    public int getItemViewType(int position) {
        if (mFooterView != null && position == mFooterPosition) return VIEW_TYPE_FOOTER;
        Message message = getItem(position);
        boolean isMe = mLayerClient.getAuthenticatedUserId().equals(message.getSender().getUserId());
        for (AtlasCellFactory factory : mCellFactories) {
            if (!factory.isBindable(message)) continue;
            return isMe ? mMyViewTypesByCell.get(factory) : mTheirViewTypesByCell.get(factory);
        }
        return -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            return new ViewHolder(mLayoutInflater.inflate(ViewHolder.RESOURCE_ID_FOOTER, parent, false));
        }

        CellType cellType = mCellTypesByViewType.get(viewType);
        int rootResId = cellType.mMe ? CellViewHolder.RESOURCE_ID_ME : CellViewHolder.RESOURCE_ID_THEM;
        CellViewHolder rootViewHolder = new CellViewHolder(mLayoutInflater.inflate(rootResId, parent, false), mParticipantProvider, mPicasso);
        rootViewHolder.mCellHolder = cellType.mCellFactory.createCellHolder(rootViewHolder.mCell, cellType.mMe, mLayoutInflater);
        rootViewHolder.mCellHolderSpecs = new AtlasCellFactory.CellHolderSpecs();
        return rootViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mQueryController.updateBoundPosition(position);
        if (mFooterView != null && position == mFooterPosition) {
            // Footer
            bindFooter(viewHolder);
        } else {
            // Cell
            bindCellViewHolder((CellViewHolder) viewHolder, position);
        }
    }

    public void bindFooter(ViewHolder viewHolder) {
        viewHolder.mRoot.removeAllViews();
        if (mFooterView.getParent() != null) {
            ((ViewGroup) mFooterView.getParent()).removeView(mFooterView);
        }
        viewHolder.mRoot.addView(mFooterView);
    }

    public void bindCellViewHolder(CellViewHolder viewHolder, int position) {
        Message message = getItem(position);
        viewHolder.mMessage = message;
        CellType cellType = mCellTypesByViewType.get(viewHolder.getItemViewType());
        boolean oneOnOne = message.getConversation().getParticipants().size() == 2;

        // Clustering and dates
        Cluster cluster = getClustering(message, position);
        if (cluster.mClusterWithPrevious == null) {
            // No previous message, so no gap
            viewHolder.mClusterSpaceGap.setVisibility(View.GONE);
            viewHolder.mTimeGroup.setVisibility(View.GONE);
        } else if (cluster.mDateBoundaryWithPrevious || cluster.mClusterWithPrevious == ClusterType.MORE_THAN_HOUR) {
            // Crossed into a new day, or > 1hr lull in conversation
            Date receivedAt = message.getReceivedAt();
            if (receivedAt == null) receivedAt = new Date();
            String timeBarDayText = Util.formatTimeDay(viewHolder.mCell.getContext(), receivedAt);
            viewHolder.mTimeGroupDay.setText(timeBarDayText);
            String timeBarTimeText = mTimeFormat.format(receivedAt.getTime());
            viewHolder.mTimeGroupTime.setText(" " + timeBarTimeText);
            viewHolder.mTimeGroup.setVisibility(View.VISIBLE);
            viewHolder.mClusterSpaceGap.setVisibility(View.GONE);
        } else if (cluster.mClusterWithPrevious == ClusterType.LESS_THAN_MINUTE) {
            // Same sender with < 1m gap
            viewHolder.mClusterSpaceGap.setVisibility(View.GONE);
            viewHolder.mTimeGroup.setVisibility(View.GONE);
        } else if (cluster.mClusterWithPrevious == ClusterType.NEW_SENDER || cluster.mClusterWithPrevious == ClusterType.LESS_THAN_HOUR) {
            // New sender or > 1m gap
            viewHolder.mClusterSpaceGap.setVisibility(View.VISIBLE);
            viewHolder.mTimeGroup.setVisibility(View.GONE);
        }

        // Sender-dependent elements
        if (cellType.mMe) {
            // Read and delivery receipts
            MessagePosition read = mReceiptMap.get(Message.RecipientStatus.READ);
            MessagePosition delivered = mReceiptMap.get(Message.RecipientStatus.DELIVERED);

            if (read != null && message == read.mMessage) {
                viewHolder.mReceipt.setVisibility(View.VISIBLE);
                viewHolder.mReceipt.setText(com.layer.atlas.R.string.atlas_message_item_read);
            } else if (delivered != null && message == delivered.mMessage) {
                viewHolder.mReceipt.setVisibility(View.VISIBLE);
                viewHolder.mReceipt.setText(com.layer.atlas.R.string.atlas_message_item_delivered);
            } else {
                viewHolder.mReceipt.setVisibility(View.GONE);
            }

            // Unsent and sent
            if (!message.isSent()) {
                viewHolder.mCell.setAlpha(0.5f);
            } else {
                viewHolder.mCell.setAlpha(1.0f);
            }
        } else {
            message.markAsRead();
            // Sender name, only for first message in cluster
            if (!oneOnOne && (cluster.mClusterWithPrevious == null || cluster.mClusterWithPrevious == ClusterType.NEW_SENDER)) {
                Actor sender = message.getSender();
                if (sender.getName() != null) {
                    viewHolder.mUserName.setText(sender.getName());
                } else {
                    Participant participant = mParticipantProvider.getParticipant(sender.getUserId());
                    viewHolder.mUserName.setText(participant != null ? participant.getName() : viewHolder.itemView.getResources().getString(com.layer.atlas.R.string.atlas_message_item_unknown_user));
                }
                viewHolder.mUserName.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mUserName.setVisibility(View.GONE);
            }

            // Avatars
            if (oneOnOne) {
                // Not in one-on-one conversations
                viewHolder.mAvatar.setVisibility(View.GONE);
            } else if (cluster.mClusterWithNext == null || cluster.mClusterWithNext != ClusterType.LESS_THAN_MINUTE) {
                // Last message in cluster
                viewHolder.mAvatar.setVisibility(View.VISIBLE);
                viewHolder.mAvatar.setParticipants(message.getSender().getUserId());
            } else {
                // Invisible for clustered messages to preserve proper spacing
                viewHolder.mAvatar.setVisibility(View.INVISIBLE);
            }
        }

        // CellHolder
        AtlasCellFactory.CellHolder cellHolder = viewHolder.mCellHolder;
        cellHolder.setMessage(message);

        // Cell dimensions
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.mCell.getLayoutParams();
        int maxWidth = mRecyclerView.getWidth() - viewHolder.mRoot.getPaddingLeft() - viewHolder.mRoot.getPaddingRight() - params.leftMargin - params.rightMargin;
        if (!oneOnOne && !cellType.mMe) {
            // Subtract off avatar width if needed
            ViewGroup.MarginLayoutParams avatarParams = (ViewGroup.MarginLayoutParams) viewHolder.mAvatar.getLayoutParams();
            maxWidth -= avatarParams.width + avatarParams.rightMargin + avatarParams.leftMargin;
        }
        // TODO: subtract spacing rather than multiply by 0.8 to handle screen sizes more cleanly
        int maxHeight = (int) Math.round(0.8 * mRecyclerView.getHeight());

        viewHolder.mCellHolderSpecs.isMe = cellType.mMe;
        viewHolder.mCellHolderSpecs.position = position;
        viewHolder.mCellHolderSpecs.maxWidth = maxWidth;
        viewHolder.mCellHolderSpecs.maxHeight = maxHeight;
        cellType.mCellFactory.bindCellHolder(cellHolder, cellType.mCellFactory.getParsedContent(mLayerClient, mParticipantProvider, message), message, viewHolder.mCellHolderSpecs);
    }

    @Override
    public int getItemCount() {
        return mQueryController.getItemCount() + ((mFooterView == null) ? 0 : 1);
    }

    @Override
    public Integer getPosition(Message message) {
        return mQueryController.getPosition(message);
    }

    @Override
    public Integer getPosition(Message message, int lastPosition) {
        return mQueryController.getPosition(message, lastPosition);
    }

    @Override
    public Message getItem(int position) {
        if (mFooterView != null && position == mFooterPosition) return null;
        return mQueryController.getItem(position);
    }

    @Override
    public Message getItem(RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof CellViewHolder)) return null;
        return ((CellViewHolder) viewHolder).mMessage;
    }


    //==============================================================================================
    // Clustering
    //==============================================================================================

    // TODO: optimize by limiting search to positions in- and around- visible range
    private Cluster getClustering(Message message, int position) {
        Cluster result = mClusterCache.get(message.getId());
        if (result == null) {
            result = new Cluster();
            mClusterCache.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.mDateBoundaryWithPrevious = isDateBoundary(previousMessage.getReceivedAt(), message.getReceivedAt());
            result.mClusterWithPrevious = ClusterType.fromMessages(previousMessage, message);

            Cluster previousCluster = mClusterCache.get(previousMessage.getId());
            if (previousCluster == null) {
                previousCluster = new Cluster();
                mClusterCache.put(previousMessage.getId(), previousCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousCluster.mClusterWithNext != result.mClusterWithPrevious) ||
                        (previousCluster.mDateBoundaryWithNext != result.mDateBoundaryWithPrevious)) {
                    requestUpdate(previousMessage, previousPosition);
                }
            }
            previousCluster.mClusterWithNext = result.mClusterWithPrevious;
            previousCluster.mDateBoundaryWithNext = result.mDateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.mDateBoundaryWithNext = isDateBoundary(message.getReceivedAt(), nextMessage.getReceivedAt());
            result.mClusterWithNext = ClusterType.fromMessages(message, nextMessage);

            Cluster nextCluster = mClusterCache.get(nextMessage.getId());
            if (nextCluster == null) {
                nextCluster = new Cluster();
                mClusterCache.put(nextMessage.getId(), nextCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextCluster.mClusterWithPrevious != result.mClusterWithNext) ||
                        (nextCluster.mDateBoundaryWithPrevious != result.mDateBoundaryWithNext)) {
                    requestUpdate(nextMessage, nextPosition);
                }
            }
            nextCluster.mClusterWithPrevious = result.mClusterWithNext;
            nextCluster.mDateBoundaryWithPrevious = result.mDateBoundaryWithNext;
        }

        return result;
    }

    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay() != d2.getDay());
    }

    private void requestUpdate(final Message message, final int lastPosition) {
        mUiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(getPosition(message, lastPosition));
            }
        });
    }


    //==============================================================================================
    // Read and delivery receipts
    //==============================================================================================

    // TODO: find clever way to prevent worst-case O(n) -- perhaps based on visible positions?
    private void updateReceipts() {
        final String userId = mLayerClient.getAuthenticatedUserId();
        Map<Message.RecipientStatus, MessagePosition> receiptMap = new HashMap<Message.RecipientStatus, MessagePosition>();
        for (int position = getItemCount(); position >= 0; position--) {
            Message message = getItem(position);
            if (message == null) continue;

            // Only display receipts for our own messages
            if (!userId.equals(message.getSender().getUserId())) continue;

            for (Map.Entry<String, Message.RecipientStatus> entry : message.getRecipientStatus().entrySet()) {
                // Only show receipts for other members
                if (entry.getKey().equals(userId)) continue;

                // Only the latest entry for this RecipientStatus matters
                if (receiptMap.containsKey(entry.getValue())) continue;

                // Found the latest entry for this RecipientStatus
                receiptMap.put(entry.getValue(), new MessagePosition(message, position));
            }
            if (receiptMap.containsKey(Message.RecipientStatus.READ) && receiptMap.containsKey(Message.RecipientStatus.DELIVERED)) {
                break;
            }
        }

        // Refresh previously-marked messages
        Set<MessagePosition> previousReceiptsToRefresh = new HashSet<MessagePosition>();
        for (Message.RecipientStatus status : Message.RecipientStatus.values()) {
            MessagePosition current = (mReceiptMap == null) ? null : mReceiptMap.get(status);
            MessagePosition next = receiptMap.get(status);
            if (current != null && next != null && current.mMessage != next.mMessage) {
                previousReceiptsToRefresh.add(current);
                previousReceiptsToRefresh.add(next);
            }
            if (current != null && next == null) {
                previousReceiptsToRefresh.add(current);
            }
            if (current == null && next != null) {
                previousReceiptsToRefresh.add(next);
            }
        }
        for (MessagePosition message : previousReceiptsToRefresh) {
            notifyItemChanged(getPosition(message.mMessage, message.mPosition));
        }

        mReceiptMap = receiptMap;
    }


    //==============================================================================================
    // UI update callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        mFooterPosition = mQueryController.getItemCount();
        updateReceipts();
        notifyDataSetChanged();
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        updateReceipts();
        notifyItemChanged(position);
    }

    @Override
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
        updateReceipts();
        notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        mFooterPosition++;
        updateReceipts();
        notifyItemInserted(position);
        if (mAppendListener != null && (position + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(position));
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        mFooterPosition += itemCount;
        updateReceipts();
        notifyItemRangeInserted(positionStart, itemCount);
        int positionEnd = positionStart + itemCount;
        if (mAppendListener != null && (positionEnd + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(positionEnd));
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        mFooterPosition--;
        updateReceipts();
        notifyItemRemoved(position);
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
        mFooterPosition -= itemCount;
        updateReceipts();
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
        updateReceipts();
        notifyItemMoved(fromPosition, toPosition);
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final static int RESOURCE_ID_FOOTER = com.layer.atlas.R.layout.atlas_message_item_footer;

        // View cache
        protected ViewGroup mRoot;

        public ViewHolder(View itemView) {
            super(itemView);
            mRoot = (ViewGroup) itemView.findViewById(com.layer.atlas.R.id.swipeable);
        }
    }

    static class CellViewHolder extends ViewHolder {
        public final static int RESOURCE_ID_ME = com.layer.atlas.R.layout.atlas_message_item_me;
        public final static int RESOURCE_ID_THEM = com.layer.atlas.R.layout.atlas_message_item_them;

        protected Message mMessage;

        // View cache
        protected TextView mUserName;
        protected View mTimeGroup;
        protected TextView mTimeGroupDay;
        protected TextView mTimeGroupTime;
        protected Space mClusterSpaceGap;
        protected AtlasAvatar mAvatar;
        protected ViewGroup mCell;
        protected TextView mReceipt;

        // Cell
        protected AtlasCellFactory.CellHolder mCellHolder;
        protected AtlasCellFactory.CellHolderSpecs mCellHolderSpecs;

        public CellViewHolder(View itemView, ParticipantProvider participantProvider, Picasso picasso) {
            super(itemView);
            mUserName = (TextView) itemView.findViewById(com.layer.atlas.R.id.sender);
            mTimeGroup = itemView.findViewById(com.layer.atlas.R.id.time_group);
            mTimeGroupDay = (TextView) itemView.findViewById(com.layer.atlas.R.id.time_group_day);
            mTimeGroupTime = (TextView) itemView.findViewById(com.layer.atlas.R.id.time_group_time);
            mClusterSpaceGap = (Space) itemView.findViewById(com.layer.atlas.R.id.cluster_space);
            mCell = (ViewGroup) itemView.findViewById(com.layer.atlas.R.id.cell);
            mReceipt = (TextView) itemView.findViewById(com.layer.atlas.R.id.receipt);

            mAvatar = ((AtlasAvatar) itemView.findViewById(com.layer.atlas.R.id.avatar));
            if (mAvatar != null) mAvatar.init(participantProvider, picasso);
        }
    }

    private enum ClusterType {
        NEW_SENDER,
        LESS_THAN_MINUTE,
        LESS_THAN_HOUR,
        MORE_THAN_HOUR;

        private static final long MILLIS_MINUTE = 60 * 1000;
        private static final long MILLIS_HOUR = 60 * MILLIS_MINUTE;

        public static ClusterType fromMessages(Message older, Message newer) {
            // Different users?
            if (!older.getSender().equals(newer.getSender())) return NEW_SENDER;

            // Time clustering for same user?
            Date oldReceivedAt = older.getReceivedAt();
            Date newReceivedAt = newer.getReceivedAt();
            if (oldReceivedAt == null || newReceivedAt == null) return LESS_THAN_MINUTE;
            long delta = Math.abs(newReceivedAt.getTime() - oldReceivedAt.getTime());
            if (delta <= MILLIS_MINUTE) return LESS_THAN_MINUTE;
            if (delta <= MILLIS_HOUR) return LESS_THAN_HOUR;
            return MORE_THAN_HOUR;
        }
    }

    private static class Cluster {
        public boolean mDateBoundaryWithPrevious;
        public ClusterType mClusterWithPrevious;

        public boolean mDateBoundaryWithNext;
        public ClusterType mClusterWithNext;
    }

    private static class MessagePosition {
        public Message mMessage;
        public int mPosition;

        public MessagePosition(Message message, int position) {
            mMessage = message;
            mPosition = position;
        }
    }

    private static class CellType {
        protected final boolean mMe;
        protected final AtlasCellFactory mCellFactory;

        public CellType(boolean me, AtlasCellFactory CellFactory) {
            mMe = me;
            mCellFactory = CellFactory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CellType cellType = (CellType) o;

            if (mMe != cellType.mMe) return false;
            return mCellFactory.equals(cellType.mCellFactory);

        }

        @Override
        public int hashCode() {
            int result = (mMe ? 1 : 0);
            result = 31 * result + mCellFactory.hashCode();
            return result;
        }
    }

    /**
     * Listens for inserts to the end of an AtlasQueryAdapter.
     */
    public interface OnMessageAppendListener {
        /**
         * Alerts the listener to inserts at the end of an AtlasQueryAdapter.  If a batch of items
         * were appended, only the last one will be alerted here.
         *
         * @param adapter The AtlasQueryAdapter which had an item appended.
         * @param message The item appended to the AtlasQueryAdapter.
         */
        void onMessageAppend(ChatMessagesAdapter adapter, Message message);
    }
}