package com.example.tutty.service.ai;

import com.example.tutty.domain.Quiz;
import com.example.tutty.domain.QuizQuestion;
import com.example.tutty.dto.QuestionResultDTO;
import com.example.tutty.dto.quiz.QuizResponseDTO;
import com.example.tutty.dto.quiz.QuizQuestionResponseDTO;
import com.example.tutty.dto.quiz.QuizResultDTO;
import com.example.tutty.repository.QuizRepository;
import com.example.tutty.repository.QuizQuestionRepository;
import com.example.tutty.service.UserService;
import com.example.tutty.service.conversation.ConversationService;
import com.example.tutty.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAIService quizAiService;
    private final ConversationService conversationService;
    private final UserService userService; // UserService 추가

    @Autowired
    public QuizService(QuizRepository quizRepository, QuizQuestionRepository quizQuestionRepository,
                       QuizAIService quizAiService, ConversationService conversationService,
                       UserService userService) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizAiService = quizAiService;
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
        List<QuizQuestion> questions = quizAiService.generateQuizQuestions(fullContent);

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
    @Transactional
    public List<QuestionResultDTO> evaluateQuiz(Long quizId, Map<Long, Integer> userAnswers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        List<QuestionResultDTO> questionResults = new ArrayList<>();
        int correctCount = 0;

        for (QuizQuestion question : questions) {
            Integer userAnswer = userAnswers.get(question.getId());

            // 사용자의 선택을 selectedOption에 설정
            question.setSelectedOption(userAnswer);

            boolean isCorrect = userAnswer != null && userAnswer.equals(question.getCorrectOption());

            if (isCorrect) {
                correctCount++;
            }

            QuestionResultDTO result = new QuestionResultDTO(
                    question.getId(),
                    question.getQuestionText(),
                    userAnswer,
                    isCorrect,
                    question.getCorrectOption()
            );

            questionResults.add(result);
        }

        // Update the correctAnswers in the Quiz entity
        quiz.setHasAttempted(true);
        quiz.setCorrectAnswers(correctCount);
        quizRepository.save(quiz);

        return questionResults;
    }

    public QuizResultDTO getQuizResult(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        List<QuestionResultDTO> questionResults = questions.stream()
                .map(question -> new QuestionResultDTO(
                        question.getId(),
                        question.getQuestionText(),
                        question.getSelectedOption(),
                        question.getCorrectOption().equals(question.getSelectedOption()), // isCorrect
                        question.getCorrectOption()
                ))
                .collect(Collectors.toList());


        int correctCount = (int) questionResults.stream().filter(QuestionResultDTO::isCorrect).count();
        int totalQuestions = questionResults.size();

        return new QuizResultDTO(totalQuestions, correctCount, questionResults, quiz.isHasAttempted());    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return userService.getUserByUserId(userId);
    }
}