package com.bureau.bureauapp.util;

import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;

/**
 * Created by hi on 3/2/2017.
 */

public class LayerUtils {

    private static final String METADATA_KEY_CONVERSATION_TITLE = "conversationName";


    public static String getConversationId(LayerClient client, ParticipantProvider provider, Conversation conversation) {
        String metadataTitle = getConversationMetadataTitle(conversation);
        if (metadataTitle != null) return metadataTitle.trim();

        StringBuilder sb = new StringBuilder();
        String userId = client.getAuthenticatedUserId();
        for (String participantId : conversation.getParticipants()) {
            if (participantId.equals(userId)) continue;
            Participant participant = provider.getParticipant(participantId);
            if (participant == null) continue;
            String initials = conversation.getParticipants().size() > 2 ? getInitials(participant) : participant.getId();
            if (sb.length() > 0) sb.append(", ");
            sb.append(initials);
        }
        return sb.toString().trim();
    }

    public static String getConversationMetadataTitle(Conversation conversation) {
        String metadataTitle = (String) conversation.getMetadata().get(METADATA_KEY_CONVERSATION_TITLE);
        if (metadataTitle != null && !metadataTitle.trim().isEmpty()) return metadataTitle.trim();
        return null;
    }

    public static String getInitials(Participant p) {
        return getInitials(p.getId());
    }

    public static String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        if (fullName.contains(" ")) {
            String[] names = fullName.split(" ");
            int count = 0;
            StringBuilder b = new StringBuilder();
            for (String name : names) {
                String t = name.trim();
                if (t.isEmpty()) continue;
                b.append(("" + t.charAt(0)).toUpperCase());
                if (++count >= 2) break;
            }
            return b.toString();
        } else {
            return ("" + fullName.trim().charAt(0)).toUpperCase();
        }
    }
}
