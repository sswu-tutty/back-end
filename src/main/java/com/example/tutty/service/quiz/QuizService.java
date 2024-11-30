package com.example.tutty.service.quiz;

import com.example.tutty.domain.Quiz;
import com.example.tutty.domain.QuizQuestion;
import com.example.tutty.dto.quiz.QuestionResultDTO;
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

import java.nio.file.AccessDeniedException;
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

        // 첫 번째 질문 텍스트 가져오기
        String firstQuestionText = questions.isEmpty() ? null : questions.get(0).getQuestionText();

        return new QuizResponseDTO(
                quiz.getId(),
                quiz.getTotalQuestions(),
                quiz.getCorrectAnswers(),
                firstQuestionText,
                quiz.isHasAttempted(),
                quiz.getLiked()
        );
    }


    private Quiz createAndSaveQuiz(Long chatroomId, User user) {
        // 채팅방의 전체 대화 내용을 가져옴
        String fullContent = conversationService.getChatroomContent(chatroomId);

        // chatroomId와 대화 내용을 기반으로 퀴즈 생성
        List<QuizQuestion> questions = quizAiService.generateQuizQuestions(chatroomId, fullContent);

        // 퀴즈 생성 및 저장
        Quiz quiz = new Quiz();
        quiz.setTotalQuestions(questions.size());
        quiz.setCorrectAnswers(0);
        quiz.setChatroomId(chatroomId);
        quiz.setUser(user); // 사용자 설정
        quizRepository.save(quiz);

        // 각 퀴즈 질문 저장
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

        // 현재 사용자가 퀴즈의 소유자인지 확인
        User currentUser = getCurrentUser();
        if (!quiz.getUser().equals(currentUser)) {
            throw new SecurityException("Unauthorized access to this quiz.");
        }

        // 나머지 로직
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        List<QuestionResultDTO> questionResults = new ArrayList<>();
        int correctCount = 0;

        for (QuizQuestion question : questions) {
            Integer userAnswer = userAnswers.get(question.getId());
            question.setSelectedOption(userAnswer);
            boolean isCorrect = userAnswer != null && userAnswer.equals(question.getCorrectOption());

            if (isCorrect) {
                correctCount++;
            }

            QuestionResultDTO result = new QuestionResultDTO(
                    question.getId(),
                    question.getQuestionText(),
                    question.getOption1(),
                    question.getOption2(),
                    question.getOption3(),
                    question.getOption4(),
                    userAnswer,
                    isCorrect,
                    question.getCorrectOption()
            );

            questionResults.add(result);
        }

        quiz.setHasAttempted(true);
        quiz.setCorrectAnswers(correctCount);
        quizRepository.save(quiz);

        return questionResults;
    }

    public QuizResultDTO getQuizResult(Long quizId) throws AccessDeniedException {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        User currentUser = getCurrentUser();
        if (!quiz.getUser().equals(currentUser)) {
            throw new AccessDeniedException("Unauthorized access to this quiz.");
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        List<QuestionResultDTO> questionResults = questions.stream()
                .map(question -> new QuestionResultDTO(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOption1(),
                        question.getOption2(),
                        question.getOption3(),
                        question.getOption4(),
                        question.getSelectedOption(),
                        question.getCorrectOption().equals(question.getSelectedOption()),
                        question.getCorrectOption()
                ))
                .collect(Collectors.toList());

        int correctCount = (int) questionResults.stream().filter(QuestionResultDTO::isCorrect).count();
        int totalQuestions = questionResults.size();

        return new QuizResultDTO(totalQuestions, correctCount, questionResults, quiz.isHasAttempted());
    }

    public List<QuizResponseDTO> getAllQuizzesByUser() {
        User user = getCurrentUser();
        List<Quiz> quizzes = quizRepository.findByUser(user);

        return quizzes.stream()
                .map(quiz -> {
                    List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());
                    String firstQuestionText = questions.isEmpty() ? null : questions.get(0).getQuestionText();

                    return new QuizResponseDTO(
                            quiz.getId(),
                            quiz.getTotalQuestions(),
                            quiz.getCorrectAnswers(),
                            firstQuestionText,
                            quiz.isHasAttempted(),
                            quiz.getLiked()
                    );
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public QuizResponseDTO toggleQuizBookmark(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // Ensure the current user is the owner of the quiz
        User currentUser = getCurrentUser();
        if (!quiz.getUser().equals(currentUser)) {
            throw new SecurityException("Unauthorized access to this quiz.");
        }

        // Toggle the liked status
        quiz.setLiked(!quiz.getLiked());
        quizRepository.save(quiz);

        // Prepare the response
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());
        String firstQuestionText = questions.isEmpty() ? null : questions.get(0).getQuestionText();

        return new QuizResponseDTO(
                quiz.getId(),
                quiz.getTotalQuestions(),
                quiz.getCorrectAnswers(),
                firstQuestionText,
                quiz.isHasAttempted(),
                quiz.getLiked()
        );
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // 현재 사용자가 퀴즈 소유자인지 확인
        User currentUser = getCurrentUser();
        if (!quiz.getUser().equals(currentUser)) {
            throw new SecurityException("Unauthorized access to this quiz.");
        }

        // 퀴즈 삭제
        quizRepository.delete(quiz);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return userService.getUserByUserId(userId);
    }
}