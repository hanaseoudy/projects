package ai;

import org.json.JSONArray;

public final class Conversation {

    private final JSONArray chat;
    private final AIModel aiModel;

    public Conversation(final AIModel aiModel) {
        this.aiModel = aiModel;

        chat = aiModel.getStartup();
    }

    public String respondToMessage(final String prompt) {
        return aiModel.respondToMessage(prompt, this);
    }

    public JSONArray getChat() {
        return chat;
    }
}
