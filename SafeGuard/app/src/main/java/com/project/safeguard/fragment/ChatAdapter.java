package com.project.safeguard.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.project.safeguard.R;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.inflater = LayoutInflater.from(context);
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage message = (ChatMessage) getItem(position);

        // View recycling and binding views
        if (convertView == null) {
            holder = new ViewHolder();
            // Inflate the appropriate layout based on whether the message is from the user or bot
            if (message.isUser()) {
                convertView = inflater.inflate(R.layout.item_chat_user, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_chat_bot, parent, false);
            }
            holder.messageText = convertView.findViewById(R.id.message_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set the message text
        holder.messageText.setText(message.getMessage());

        return convertView;
    }

    // ViewHolder pattern for efficient view recycling
    private static class ViewHolder {
        TextView messageText;
    }

    // Add a method to update the adapter when the data changes
    public void updateMessages(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }
}
