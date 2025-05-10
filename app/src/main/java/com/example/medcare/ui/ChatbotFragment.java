package com.example.medcare.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.medcare.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class ChatbotFragment extends Fragment {

    private static final String TAG = "ChatbotFragment";
    private static final String LOCAL_API_URL = "http://192.168.100.81:11436/api/generate";
    private static final String LOCAL_MODEL = "gemma:2b-instruct";

    private EditText userInput;
    private Button sendButton;
    private TextView chatHistory;
    private ProgressBar progressBar;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final String[] defaultResponses = {
            "Je suis là pour vous aider. Que puis-je faire pour vous?",
            "Je comprends votre message. Avez-vous des questions spécifiques?",
            "Je suis à votre écoute. Comment puis-je vous assister aujourd'hui?",
            "Bien sûr, je suis là pour vous. Que puis-je faire pour vous?",
            "Je vous écoute. N'hésitez pas à me poser des questions spécifiques.",
            "Je ferai de mon mieux pour vous aider. Pourriez-vous préciser votre demande?"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        userInput = view.findViewById(R.id.inputEditText);
        sendButton = view.findViewById(R.id.sendButton);
        chatHistory = view.findViewById(R.id.chatHistory);
        progressBar = view.findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Format the welcome message with styled text
        String welcomeMessage = formatBotMessage("Bonjour! Je suis l'assistant médical MedWay à votre service. Comment puis-je vous aider aujourd'hui?");
        chatHistory.setText(Html.fromHtml(welcomeMessage, Html.FROM_HTML_MODE_COMPACT));
        chatHistory.setMovementMethod(LinkMovementMethod.getInstance());

        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString().trim();
            if (!input.isEmpty()) {
                appendToChatHistory(formatUserMessage(input));
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                sendButton.setEnabled(false);
                sendMessageToChatbot(input);
                userInput.setText("");
            }
        });

        return view;
    }

    private String formatUserMessage(String message) {
        return "<p><b><font color='#1E5CB3'>Vous:</font></b><br>" +
                "<span style='background-color:#E8F1FD;padding:8px;border-radius:12px;display:inline-block;'>" +
                message + "</span></p>";
    }

    private String formatBotMessage(String message) {
        return "<p><b><font color='#F23B3B'>Assistant:</font></b><br>" +
                "<span style='background-color:#FFFFFF;padding:8px;border-radius:12px;display:inline-block;border:1px solid #E6E6E6;'>" +
                message + "</span></p>";
    }

    private void appendToChatHistory(String formattedMessage) {
        if (getActivity() == null || chatHistory == null) return;

        requireActivity().runOnUiThread(() -> {
            // Append the new HTML content
            String currentText = chatHistory.getText().toString();
            chatHistory.setText(Html.fromHtml(currentText + formattedMessage, Html.FROM_HTML_MODE_COMPACT));
            chatHistory.setMovementMethod(LinkMovementMethod.getInstance());

            // Scroll to bottom
            View parent = (View) chatHistory.getParent();
            if (parent instanceof ScrollView) {
                ((ScrollView) parent).post(() ->
                        ((ScrollView) parent).fullScroll(View.FOCUS_DOWN));
            }
        });
    }

    private void sendMessageToChatbot(String message) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("model", LOCAL_MODEL);

            // Format simplifié spécifique à Gemma
            jsonRequest.put("prompt", "Tu es un assistant médical bilingue. " +
                    "Réponds dans la langue de l'utilisateur. Question: " + message);

            jsonRequest.put("stream", false);

            // Options optimisées
            JSONObject options = new JSONObject();
            options.put("num_ctx", 512);
            options.put("temperature", 0.7);
            options.put("num_thread", 2);
            jsonRequest.put("options", options);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonRequest.toString()
            );

            Request request = new Request.Builder()
                    .url(LOCAL_API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "API call failed", e);
                    requireActivity().runOnUiThread(() -> handleError());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "API Response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String botReply = jsonResponse.optString("response", "");

                        if (botReply.isEmpty()) {
                            Log.e(TAG, "Received empty response");
                            requireActivity().runOnUiThread(() -> handleError());
                        } else {
                            requireActivity().runOnUiThread(() -> displayBotReply(botReply));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        requireActivity().runOnUiThread(() -> handleError());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            handleError();
        }
    }

    private void handleError() {
        if (getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                displayLocalResponse();
                resetUI();
            });
        }
    }

    private void displayBotReply(String reply) {
        if (getActivity() == null) return;

        String cleanedReply = reply.trim();
        if (!cleanedReply.isEmpty()) {
            appendToChatHistory(formatBotMessage(cleanedReply));
        } else {
            appendToChatHistory(formatBotMessage("[Réponse vide reçue]"));
        }
        resetUI();
    }

    private void displayLocalResponse() {
        String defaultResponse = defaultResponses[new Random().nextInt(defaultResponses.length)];
        appendToChatHistory(formatBotMessage(defaultResponse));

        Toast.makeText(getContext(),
                "Désolé, le service est temporairement indisponible. Utilisation du mode hors ligne.",
                Toast.LENGTH_SHORT).show();
    }

    private void resetUI() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        sendButton.setEnabled(true);
    }
}