package com.example.tutty.service.quiz;

import com.example.tutty.domain.QuizQuestion;
import com.example.tutty.service.OpenAiService;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizAIService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    public QuizAIService(OpenAiService openAiService, ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.objectMapper = objectMapper;
    }

    public List<QuizQuestion> generateQuizQuestions(Long chatroomId, String content) {
        String prompt = """
    다음 내용을 바탕으로 5개의 퀴즈를 JSON 형식으로 만들어줘. 절대 코드 블록이나 마크다운 형식 없이, 순수한 JSON만 반환해줘:
    [
        {
            "questionText": "질문 내용",
            "option1": "첫 번째 선택지",
            "option2": "두 번째 선택지",
            "option3": "세 번째 선택지",
            "option4": "네 번째 선택지",
            "correctOption": 정답 번호 (1, 2, 3 또는 4)
        },
        ...
    ]

    생성할 퀴즈 내용:
    """ + content;

        // chatroomId를 threadId로 사용
        String threadId = chatroomId.toString();
        String response = openAiService.askQuestion(threadId, prompt).block();
        return parseQuizQuestions(response);
    }

    private List<QuizQuestion> parseQuizQuestions(String response) {
        List<QuizQuestion> quizQuestions = new ArrayList<>();

        try {
            // 원본 AI 응답 출력
            System.out.println("AI raw response: " + response);

            // JSON 응답 파싱
            JsonNode rootNode = objectMapper.readTree(response);

            // quizzes 배열 추출
            JsonNode quizzesArrayNode = rootNode.path("quizzes");

            if (quizzesArrayNode.isArray()) {
                for (JsonNode node : quizzesArrayNode) {
                    QuizQuestion quizQuestion = new QuizQuestion();
                    quizQuestion.setQuestionText(node.path("questionText").asText());
                    quizQuestion.setOption1(node.path("option1").asText());
                    quizQuestion.setOption2(node.path("option2").asText());
                    quizQuestion.setOption3(node.path("option3").asText());
                    quizQuestion.setOption4(node.path("option4").asText());
                    quizQuestion.setCorrectOption(node.path("correctOption").asInt());

                    // 유효성 검사
                    if (quizQuestion.getQuestionText().isEmpty() ||
                            quizQuestion.getOption1().isEmpty() ||
                            quizQuestion.getOption2().isEmpty() ||
                            quizQuestion.getCorrectOption() == 0) {
                        System.err.println("Invalid question data found and skipped: " + quizQuestion);
                        continue;
                    }

                    quizQuestions.add(quizQuestion);
                }
            } else {
                System.err.println("quizzes 배열을 찾을 수 없습니다. AI 응답: " + response);
            }

        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            e.printStackTrace();
        }

        return quizQuestions;
    }
}
