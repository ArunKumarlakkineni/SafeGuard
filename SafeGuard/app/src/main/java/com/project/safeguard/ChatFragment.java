package com.project.safeguard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.project.safeguard.fragment.ChatAdapter;
import com.project.safeguard.fragment.ChatMessage;
import com.project.safeguard.fragment.ChatViewModel;

import java.util.List;

public class ChatFragment extends Fragment {
    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        ListView chatListView = view.findViewById(R.id.chat_list_view);
        EditText chatInput = view.findViewById(R.id.chat_input);
        Button sendButton = view.findViewById(R.id.send_button);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Initialize the adapter with the chat messages
        chatViewModel.getChatMessages().observe(getViewLifecycleOwner(), new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> messages) {
                if (chatAdapter == null) {
                    chatAdapter = new ChatAdapter(getContext(), messages);
                    chatListView.setAdapter(chatAdapter);
                } else {
                    // Update the adapter with new messages
                    chatAdapter.updateMessages(messages);
                }
                Log.d("ChatFragment", "Messages observed: " + messages.size());
            }
        });


        // Set up the send button to send messages
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String message = chatInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    chatViewModel.sendMessage(message);
                    chatInput.setText("");
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
