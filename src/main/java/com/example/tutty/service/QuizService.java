package com.example.tutty.service;

import com.example.tutty.domain.Quiz;
import com.example.tutty.domain.QuizQuestion;
import com.example.tutty.dto.QuizResponseDTO;
import com.example.tutty.dto.QuizQuestionResponseDTO;
import com.example.tutty.repository.QuizRepository;
import com.example.tutty.repository.QuizQuestionRepository;
import com.example.tutty.service.conversation.ConversationService;
import com.example.tutty.domain.User;
import com.example.tutty.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final AIService aiService;
    private final ConversationService conversationService;
    private final UserService userService; // UserService 추가

    @Autowired
    public QuizService(QuizRepository quizRepository, QuizQuestionRepository quizQuestionRepository,
                       AIService aiService, ConversationService conversationService,
                       UserService userService) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.aiService = aiService;
        this.conversationService = conversationService;
        this.userService = userService;
    }

    public QuizResponseDTO generateQuizForChatroom(Long chatroomId) {
        // 인증된 사용자 가져오기
        User user = getCurrentUser();

        // 생성된 퀴즈와 질문 가져오기
        Quiz quiz = createAndSaveQuiz(chatroomId, user);
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());

        // DTO 변환
        List<QuizQuestionResponseDTO> questionDTOs = questions.stream()
                .map(this::toQuizQuestionResponseDTO)
                .collect(Collectors.toList());

        return new QuizResponseDTO(quiz.getId(), quiz.getTotalQuestions(), questionDTOs);
    }

    private Quiz createAndSaveQuiz(Long chatroomId, User user) {
        String fullContent = conversationService.getChatroomContent(chatroomId);
        List<QuizQuestion> questions = aiService.generateQuizQuestions(fullContent);

        Quiz quiz = new Quiz();
        quiz.setTotalQuestions(questions.size());
        quiz.setCorrectAnswers(0);
        quiz.setChatroomId(chatroomId);
        quiz.setUser(user); // 사용자 설정
        quizRepository.save(quiz);

        questions.forEach(q -> {
            q.setQuiz(quiz);
            quizQuestionRepository.save(q);
        });

        return quiz;
    }

    private QuizQuestionResponseDTO toQuizQuestionResponseDTO(QuizQuestion question) {
        return new QuizQuestionResponseDTO(
                question.getId(),
                question.getQuestionText(),
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4(),
                question.getCorrectOption()
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return userService.getUserByUserId(userId);
    }
}