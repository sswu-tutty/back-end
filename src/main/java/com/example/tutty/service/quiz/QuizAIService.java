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

    public List<QuizQuestion> generateQuizQuestions(String content) {
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

        String response = openAiService.askQuestion(prompt).block();
        return parseQuizQuestions(response);
    }


    private List<QuizQuestion> parseQuizQuestions(String response) {
        List<QuizQuestion> quizQuestions = new ArrayList<>();

        try {
            // 원본 AI 응답 출력
            System.out.println("AI raw response: " + response);

            // JSON 응답을 직접 파싱하여 JSON 배열 추출
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choicesNode = rootNode.path("choices").get(0).path("message").path("content");

            if (choicesNode.isTextual()) {
                String jsonContent = choicesNode.asText();
                System.out.println("Extracted JSON content: " + jsonContent);

                // JSON 배열 파싱
                JsonNode quizArrayNode = objectMapper.readTree(jsonContent);
                for (JsonNode node : quizArrayNode) {
                    QuizQuestion quizQuestion = new QuizQuestion();
                    quizQuestion.setQuestionText(node.path("questionText").asText());
                    quizQuestion.setOption1(node.path("option1").asText());
                    quizQuestion.setOption2(node.path("option2").asText());
                    quizQuestion.setOption3(node.path("option3").asText());
                    quizQuestion.setOption4(node.path("option4").asText());
                    quizQuestion.setCorrectOption(node.path("correctOption").asInt());

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
                System.err.println("JSON 형식을 찾을 수 없습니다. AI 응답: " + response);
            }

        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            e.printStackTrace();
        }

        return quizQuestions;
    }

}