package ai;

import org.json.JSONArray;

public interface AIModel {

    JSONArray getStartup();

    String respondToMessage(final String prompt, final Conversation conversation);

}
