package com.devtrack.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiResumeAnalyzerService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.6-flash}")
    private String model;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiResumeAnalyzerService(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();

        this.objectMapper = objectMapper;
    }

    public String analyzeResume(String resumeText, String jobDescription) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured."
            );
        }

        String prompt = """
                You are an expert AI-powered Resume and ATS Analyzer.

                Analyze the candidate resume against the provided job description.

                Give a practical, recruiter-style analysis.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Return the analysis with these sections:

                1. Overall ATS Match Score (0-100)
                2. Matching Skills
                3. Missing Skills and Keywords
                4. Important Job Description Keywords
                5. Resume Strengths
                6. Resume Weaknesses
                7. ATS Formatting Problems
                8. Experience Relevance
                9. Specific Improvement Suggestions
                10. Three rewritten resume bullet points
                11. Final Recommendation

                Be specific and do not invent experience, skills,
                projects, certifications or achievements that are not
                present in the resume.
                """.formatted(resumeText, jobDescription);

        try {

            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of(
                                    "parts", new Object[]{
                                            Map.of("text", prompt)
                                    }
                            )
                    },
                    "generationConfig", Map.of(
                            "temperature", 0.2,
                            "maxOutputTokens", 2500
                    )
            );

            System.out.println("========== GEMINI REQUEST ==========");
            System.out.println("Model: " + model);
            System.out.println("Resume characters: " + resumeText.length());
            System.out.println("JD characters: " + jobDescription.length());
            System.out.println("====================================");

            String response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException(
                        "Gemini returned an empty response."
                );
            }

            System.out.println("========== GEMINI RESPONSE ==========");
            System.out.println("Response received successfully.");
            System.out.println("=====================================");

            JsonNode root = objectMapper.readTree(response);

            JsonNode textNode = root
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new IllegalStateException(
                        "Could not extract analysis from Gemini response."
                );
            }

            return textNode.asText();

        } catch (RestClientResponseException e) {

            System.out.println("========== GEMINI API ERROR ==========");
            System.out.println("HTTP Status: " + e.getStatusCode());
            System.out.println("Model: " + model);
            System.out.println("Response Body:");
            System.out.println(e.getResponseBodyAsString());
            System.out.println("======================================");

            throw new IllegalStateException(
                    "Gemini API error " + e.getStatusCode()
                            + ": " + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {

            System.out.println("========== GEMINI ERROR ==========");
            System.out.println("Model: " + model);
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("==================================");

            throw new IllegalStateException(
                    "Gemini resume analysis failed: " + e.getMessage(),
                    e
            );
        }
    }
}