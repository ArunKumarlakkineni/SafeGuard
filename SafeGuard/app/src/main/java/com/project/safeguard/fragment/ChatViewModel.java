package com.project.safeguard.fragment;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.safeguard.fragment.ChatMessage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>(new ArrayList<>());
    private static final String TAG = "ChatFragment";
    private static final String URL = "https://api.vultrinference.com/v1/chat/completions/RAG";
    private static final String AUTHORIZATION = "Bearer Z3EAKE3SMRID5J2JUK6YPZW3S5ZGAEFQOCBA";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String jsonResponse;
    public ChatViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public void sendMessage(String message) {
        addMessage(new ChatMessage(message, true));
        fetchDummyResponse(message);
    }

    private void addMessage(ChatMessage message) {
        List<ChatMessage> currentMessages = new ArrayList<>(chatMessages.getValue());
        Log.d("ChatFragment","msg : "+currentMessages);
        currentMessages.add(message);
        chatMessages.setValue(currentMessages);
        Log.d("ChatFragment","msg : "+chatMessages.getValue().get(chatMessages.getValue().size()-1).getMessage());

    }

    private void fetchDummyResponse(String prompt) {

        // Display the response in the UI
        Log.d(TAG, "fetchDummyResponse: "+prompt);
        makeApiCall(prompt);

    }
    private void makeApiCall(String prompt) {
        OkHttpClient client = new OkHttpClient();

        // Prepare the JSON payload with user-provided prompt
        String json = "{\n" +
                "    \"collection\": \"women_suggesti\",\n" +
                "    \"model\": \"llama2-7b-chat-Q5_K_M\",\n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"" + prompt.replace("\"", "\\\"") + "\"\n" + // escaping quotes in user input
                "        }\n" +
                "    ],\n" +
                "    \"max_tokens\": 256,\n" +
                "    \"seed\": -1,\n" +
                "    \"temperature\": 0.8,\n" +
                "    \"top_k\": 40,\n" +
                "    \"top_p\": 0.9\n" +
                "}";

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", AUTHORIZATION)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        jsonResponse = jsonObject.toString();
                        Log.d("ChatFragment", "Response: " + jsonResponse);

                        // Extract content from JSON response
                        String content = jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        Log.d("ChatFragment", "Content: " + content);

                        // Update UI with the response on the main thread
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if(content.isEmpty()){
                                addMessage(new ChatMessage("Iam having trouble can you please try again later",false));
                            }else{
                                addMessage(new ChatMessage(content, false));
                            }

                        }, 2000);

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse JSON: " + e.getMessage());
                    }
                }
            }

        });
        if(jsonResponse==null){
            addMessage(new ChatMessage("Iam having trouble can you please try again later",false));
        }
    }


}
