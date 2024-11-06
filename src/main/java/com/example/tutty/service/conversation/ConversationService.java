package com.example.tutty.service.conversation;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;

import java.util.List;

public interface ConversationService {
    Conversation saveConversation(Conversation conversation);

    List<Conversation> getConversationsByUser(User user);

    // chatroomId와 사용자에 따른 Conversation 목록 반환
    List<Conversation> getConversationsByChatroomIdAndUser(Long chatroomId, User user);

    List<Conversation> getConversationsByChatroomId(Long chatroomId);
    List<Conversation> getEarliestConversationsByChatroom();

    String getChatroomContent(Long chatroomId);
}
