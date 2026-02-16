package com.marina.demo.service;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final OllamaChatModel chatModel;

    public AIService(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Integer calculateMatchScore(String cvText, String jobDescription) {
        try {
            String prompt = "Compare this CV: " + cvText + 
                          " with Job Description: " + jobDescription + 
                          ". Return ONLY a single number between 0 and 100 representing the match percentage. Do not include any text, symbols or % sign.";
            
            String response = chatModel.call(prompt).trim();
            String cleaned = response.replaceAll("[^0-9]", "");
            
            if (cleaned.isEmpty()) return 0;
            
            int score = Integer.parseInt(cleaned);
            return (score > 100) ? 100 : score;
            
        } catch (Exception e) {
            System.err.println("AI Service Error: " + e.getMessage());
            return 0;
        }
    }
}