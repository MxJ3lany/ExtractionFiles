package com.applozic.mobicomkit.uiwidgets.uikit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applozic.mobicomkit.api.conversation.ApplozicConversation;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.uikit.adapters.AlConversationAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashish on 28/05/18.
 */

public class AlConversationFragment extends Fragment {

    RecyclerView recyclerView;
    List<Message> conversationList;
    AlConversationAdapter conversationAdapter;

    public AlConversationFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.al_conversation_fragment, null);
        recyclerView = view.findViewById(R.id.conversationRecyclerView);

        conversationAdapter = new AlConversationAdapter(getActivity(), conversationList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(conversationAdapter);

        ApplozicConversation.getLatestMessageList(getContext(), null, false, new MessageListHandler() {
            @Override
            public void onResult(List<Message> messageList, ApplozicException e) {
                conversationList.addAll(messageList);
                conversationAdapter.notifyDataSetChanged();
            }
        });

        recyclerView.addOnScrollListener(new AlScrollListener() {
            @Override
            public void onScrollUp() {
            }

            @Override
            public void onScrollDown() {
            }

            @Override
            public void onLoadMore() {
                conversationAdapter.showLoading(true);
                conversationAdapter.notifyDataSetChanged();
                ApplozicConversation.getLatestMessageList(getContext(), null, true, new MessageListHandler() {
                    @Override
                    public void onResult(List<Message> messageList, ApplozicException e) {
                        conversationList.addAll(messageList);
                        conversationAdapter.showLoading(false);
                        conversationAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        return view;
    }

    public void addMessage(Message message) {
        ApplozicConversation.addLatestMessage(message, conversationList);
        conversationAdapter.notifyDataSetChanged();
    }

    public void removeMessage(String userId, Integer groupId) {
        ApplozicConversation.removeLatestMessage(userId, groupId, conversationList);
        conversationAdapter.notifyDataSetChanged();
    }

    public void notifyAdapter() {
        if (conversationAdapter != null) {
            conversationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
