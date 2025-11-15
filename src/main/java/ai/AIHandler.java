package ai;

import java.util.LinkedList;
import java.util.List;

public final class AIHandler {

    private static AIHandler instance;
    private final List<AIModel> aiModels;
    private final Conversation conversation;

    public AIHandler() {
        instance = this;

        aiModels = new LinkedList<>();
        aiModels.add(new GeminiFlashHandler());
        // More if needed (m4 dlw2ty 5ales)

        conversation = new Conversation(aiModels.getFirst());
    }

    public String respond(final String message) {
        return conversation.respondToMessage(message);
    }

    public static AIHandler getInstance() {
        return instance;
    }
}
