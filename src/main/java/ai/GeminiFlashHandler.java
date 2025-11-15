package ai;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GeminiFlashHandler implements AIModel {

    private final URL url;
    private final JSONArray trainedData;

    public GeminiFlashHandler() {
        try {
            url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyBvFfBrz_riDaM70RzbuwoA1IyA1YimV0o");
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }

        trainedData = new JSONArray();

        trainedData.put(generateMessage(true, "You're one CPU in the game Jackaroo, you're required to answer like a human player playing the game, so have some humor as well as keeping your messages as short as possible, make your english style realistic and talk with GenZ slang (i.e Ws in chat, no much emojis unless the situation is funny or weird like skull emoji and sob and dead rose). Answer with the following format.\n'CPU<CPU NUMBER HERE>: <MESSAGE HERE>' where CPU NUMBER HERE is from 1 to 3 and MESSAGE HERE is your generated message, if multiple CPUs are going to answer, put each response in a new line like\nCPU1: I agree\nCPU2: I agree 2!"));
    }

    @Override
    public String respondToMessage(final String message, final Conversation conversation) {
        conversation.getChat().put(generateMessage(true, message));

        final JSONObject response;
        try {
            response = getResponse(conversation);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (response == null)
            return "An error has occurred, please try again later.";

        final String responseStr = response.getJSONArray("parts").getJSONObject(0).getString("text");

        conversation.getChat().put(generateMessage(false, responseStr));
        return responseStr;
    }

    @Override
    public JSONArray getStartup() {
        return new JSONArray(trainedData);
    }

    private JSONObject getResponse(final Conversation conversation) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (final OutputStream os = connection.getOutputStream()) {
            final byte[] input = new JSONObject().put("contents", conversation.getChat()).toString().getBytes(StandardCharsets.UTF_8);

            os.write(input, 0, input.length);
        }

        String inputLine;
        final StringBuilder response = new StringBuilder();
        final int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }

        connection.disconnect();

        final JSONObject json = new JSONObject(response.toString());
        return json.has("candidates") ? json.getJSONArray("candidates").getJSONObject(0).getJSONObject("content") : null;
    }

    private JSONObject generateMessage(final boolean user, final String message) {
        return new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", message))).put("role", user ? "user" : "model");
    }
}
