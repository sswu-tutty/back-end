package com.example.tutty.service.conversation;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import com.example.tutty.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    @Override
    public List<Conversation> getEarliestConversationsByChatroomAndUser(User user) {
        return conversationRepository.findEarliestConversationsByUser(user);
    }


    @Override
    public List<Conversation> getConversationsByChatroomId(Long chatroomId) {
        return conversationRepository.findByChatroomId(chatroomId);
    }

    @Override
    public String getChatroomContent(Long chatroomId, User user) {
        List<Conversation> conversations = conversationRepository.findByChatroomIdAndUser(chatroomId, user);
        return conversations.stream()
                .map(Conversation::getAnswer)
                .collect(Collectors.joining(" "));
    }
}
