package com.example.tutty.service.note;

import com.example.tutty.service.OpenAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class NoteSummaryService {

    private final OpenAiService openAiService;

    public NoteSummaryService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    public String summarizeContent(String fullContent) {
        return extractContent(openAiService.askQuestion("다음 대화 내용을 요약해줘: " + fullContent).block());
    }

    public String generateTitle(String summarizedContent) {
        return extractContent(openAiService.askQuestion("다음 요약 내용에 맞는 한 줄 제목을 만들어줘: " + summarizedContent).block());
    }

    private String extractContent(String gptResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gptResponse);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "추출 실패";
        }
    }
}
