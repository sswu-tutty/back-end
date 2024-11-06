package com.example.tutty.service.conversation;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import com.example.tutty.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationServiceImpl(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    @Transactional
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    @Override
    public List<Conversation> getConversationsByUser(User user) {
        return conversationRepository.findByUser(user);
    }

    @Override
    public List<Conversation> getConversationsByChatroomIdAndUser(Long chatroomId, User user) {
        return conversationRepository.findByChatroomIdAndUser(chatroomId, user);
    }

    public List<Conversation> getConversationsByChatroomId(Long chatroomId) {
        return conversationRepository.findByChatroomId(chatroomId);
    }

}
