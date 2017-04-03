package com.bureau.bureauapp.flavor;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.util.Log;
import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DemoParticipantProvider implements ParticipantProvider {
    private final Context mContext;
    private String mLayerAppIdLastPathSegment;
    private final Queue<ParticipantListener> mParticipantListeners = new ConcurrentLinkedQueue<>();
    private final Map<String, DemoParticipant> mParticipantMap = new HashMap<>();
    private final AtomicBoolean mFetching = new AtomicBoolean(false);
    String resultStr;

    public DemoParticipantProvider(Context context) {
        mContext = context.getApplicationContext();
    }

    public DemoParticipantProvider setLayerAppId(String layerAppId) {
        if (layerAppId.contains("/")) {
            mLayerAppIdLastPathSegment = Uri.parse(layerAppId).getLastPathSegment();
        } else {
            mLayerAppIdLastPathSegment = layerAppId;
        }
        load();
        fetchParticipants();
        return this;
    }


    //==============================================================================================
    // Atlas ParticipantProvider
    //==============================================================================================

    @Override
    public Map<String, Participant> getMatchingParticipants(String filter, Map<String, Participant> result) {
        if (result == null) {
            result = new HashMap<String, Participant>();
        }

        synchronized (mParticipantMap) {
            // With no filter, return all Participants
            if (filter == null) {
                result.putAll(mParticipantMap);
                return result;
            }

            // Filter participants by substring matching first- and last- names
            for (DemoParticipant p : mParticipantMap.values()) {
                boolean matches = false;
                if (p.getName() != null && p.getName().toLowerCase().contains(filter))
                    matches = true;
                if (matches) {
                    result.put(p.getId(), p);
                } else {
                    result.remove(p.getId());
                }
            }
            return result;
        }
    }

    @Override
    public Participant getParticipant(String userId) {
        synchronized (mParticipantMap) {
            DemoParticipant participant = mParticipantMap.get(userId);
            if (participant != null) return participant;
            fetchParticipants();
            return null;
        }
    }

    /**
     * Adds the provided Participants to this ParticipantProvider, saves the participants, and
     * returns the list of added participant IDs.
     */
    private DemoParticipantProvider setParticipants(Collection<DemoParticipant> participants) {
        List<String> newParticipantIds = new ArrayList<>(participants.size());
        synchronized (mParticipantMap) {
            for (DemoParticipant participant : participants) {
                String participantId = participant.getId();
                if (!mParticipantMap.containsKey(participantId))
                    newParticipantIds.add(participantId);
                mParticipantMap.put(participantId, participant);
            }
            save();
        }
        alertParticipantsUpdated(newParticipantIds);
        return this;
    }


    //==============================================================================================
    // Persistence
    //==============================================================================================

    /**
     * Loads additional participants from SharedPreferences
     */
    private boolean load() {
        synchronized (mParticipantMap) {
            String jsonString = mContext.getSharedPreferences("participants", Context.MODE_PRIVATE).getString("json", null);
            if (jsonString == null) return false;

            try {
                for (DemoParticipant participant : participantsFromJson(new JSONArray(jsonString))) {
                    mParticipantMap.put(participant.getId(), participant);
                }
                return true;
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * Saves the current map of participants to SharedPreferences
     */
    private boolean save() {
        synchronized (mParticipantMap) {
            try {
                mContext.getSharedPreferences("participants", Context.MODE_PRIVATE).edit()
                        .putString("json", participantsToJson(mParticipantMap.values()).toString())
                        .commit();
                return true;
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
            }
        }
        return false;
    }


    //==============================================================================================
    // Network operations
    //==============================================================================================
    public void fetchParticipants() {
//        new GetChatFriendsList().execute();

        resultStr = null;

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                List<NameValuePair> parms = new LinkedList<NameValuePair>();
                parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));

                String paramString = URLEncodedUtils.format(parms, "utf-8");
                String url = BureauConstants.BASE_URL + BureauConstants.GET_CHAT_VIEW;
                url += "?";
                url += paramString;

                resultStr = new ConnectBureau().getDataFromUrl(url);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (resultStr != null && resultStr.length() > 1) {
                        String s = resultStr.substring(0, 1);
                        if (s.equalsIgnoreCase("{")) {
                            JSONObject jsonObject = new JSONObject(resultStr);
                            if (jsonObject.has("msg")) {
                                android.util.Log.e("DemoParticipantProvider", "Fetching friends count " + jsonObject.getString("response"));

                            }
                        } else if (s.equalsIgnoreCase("[")) {
                            JSONArray jsonArray = new JSONArray(resultStr);
                            setParticipants(participantsFromJson(jsonArray));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    public class GetChatFriendsList extends AsyncTask<Void, Void, Void> {

        String resultStr;

        @Override
        protected Void doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));

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
            android.util.Log.e("DemoParticipantProvider", "chat list " + resultStr);
            try {
                if (resultStr != null && resultStr.length() > 1) {
                    String s = resultStr.substring(0, 1);
                    if (s.equalsIgnoreCase("{")) {
                        JSONObject jsonObject = new JSONObject(resultStr);
                        if (jsonObject.has("msg")) {
                            android.util.Log.e("DemoParticipantProvider", "Fetching friends count " + jsonObject.getString("response"));

                        }
                    } else if (s.equalsIgnoreCase("[")) {
                        JSONArray jsonArray = new JSONArray(resultStr);
                        setParticipants(participantsFromJson(jsonArray));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    //==============================================================================================
    // Utils
    //==============================================================================================

    private static List<DemoParticipant> participantsFromJson(JSONArray participantArray) throws JSONException {
        List<DemoParticipant> participants = new ArrayList<>(participantArray.length());
        for (int i = 0; i < participantArray.length(); i++) {

            JSONObject participantObject = participantArray.getJSONObject(i);
            DemoParticipant participant = new DemoParticipant();
            participant.setId(participantObject.optString("userid"));
            participant.setName(participantObject.optString("First Name"));
            participant.setAvatarUrl(Uri.parse(participantObject.optString("img_url")));
            participants.add(participant);
        }
        return participants;
    }

    private static JSONArray participantsToJson(Collection<DemoParticipant> participants) throws JSONException {
        JSONArray participantsArray = new JSONArray();
        for (DemoParticipant participant : participants) {
            JSONObject participantObject = new JSONObject();
            participantObject.put("id", participant.getId());
            participantObject.put("name", participant.getName());
            participantsArray.put(participantObject);
        }
        return participantsArray;
    }

    private DemoParticipantProvider registerParticipantListener(ParticipantListener participantListener) {
        if (!mParticipantListeners.contains(participantListener)) {
            mParticipantListeners.add(participantListener);
        }
        return this;
    }

    private DemoParticipantProvider unregisterParticipantListener(ParticipantListener participantListener) {
        mParticipantListeners.remove(participantListener);
        return this;
    }

    private void alertParticipantsUpdated(Collection<String> updatedParticipantIds) {
        for (ParticipantListener listener : mParticipantListeners) {
            listener.onParticipantsUpdated(this, updatedParticipantIds);
        }
    }


    //==============================================================================================
    // Callbacks
    //==============================================================================================

    public interface ParticipantListener {
        void onParticipantsUpdated(DemoParticipantProvider provider, Collection<String> updatedParticipantIds);
    }
}