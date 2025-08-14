package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.ChatAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.ChatMessage;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.List;

public class ChatSupportFragment extends Fragment {
    private RecyclerView rv;
    private EditText etMessage;
    private ImageButton btnSend;
    private DatabaseHelper db;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private SessionManager session;

    @Override public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle b) {
        View v = inf.inflate(R.layout.fragment_chat_support, parent, false);
        rv = v.findViewById(R.id.rvChat);
        etMessage = v.findViewById(R.id.etMessage);
        btnSend = v.findViewById(R.id.btnSend);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        messages = db.getAllChatMessages();
        adapter = new ChatAdapter(messages);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        btnSend.setOnClickListener(view -> sendMessage());

        return v;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        db.addChatMessage("player", text);
        etMessage.setText("");
        messages.clear();
        messages.addAll(db.getAllChatMessages());
        adapter.notifyDataSetChanged();
        rv.scrollToPosition(messages.size() - 1);
    }
}
