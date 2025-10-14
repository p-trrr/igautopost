package com.igautopost.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igautopost.config.AppConfig;
import com.igautopost.model.OpenAIRequest;
import com.igautopost.model.OpenAIResponse;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Service for generating Instagram captions using OpenAI API.
 */
public class OpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final AppConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OpenAIService(AppConfig config, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Generates an Instagram caption based on the provided context or description.
     * 
     * @param context Context or description for the caption
     * @return Generated caption
     * @throws IOException if API call fails
     */
    public String generateCaption(String context) throws IOException {
        logger.info("Generating caption for context: {}", context);
        
        String prompt = buildPrompt(context);
        OpenAIRequest request = new OpenAIRequest(
            config.getOpenaiModel(),
            Arrays.asList(
                new OpenAIRequest.Message("system", "You are a creative social media expert who writes engaging Instagram captions."),
                new OpenAIRequest.Message("user", prompt)
            )
        );
        request.setMaxTokens(150);
        request.setTemperature(0.7);
        
        String requestBody = objectMapper.writeValueAsString(request);
        
        Request httpRequest = new Request.Builder()
            .url(config.getOpenaiApiUrl() + "/chat/completions")
            .addHeader("Authorization", "Bearer " + config.getOpenaiApiKey())
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(requestBody, JSON))
            .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("OpenAI API error: {} - {}", response.code(), errorBody);
                throw new IOException("OpenAI API request failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            OpenAIResponse openAIResponse = objectMapper.readValue(responseBody, OpenAIResponse.class);
            
            if (openAIResponse.getChoices() == null || openAIResponse.getChoices().isEmpty()) {
                throw new IOException("No caption generated from OpenAI");
            }
            
            String caption = openAIResponse.getChoices().get(0).getMessage().getContent().trim();
            logger.info("Generated caption: {}", caption);
            return caption;
        }
    }
    
    private String buildPrompt(String context) {
        return String.format(
            "Create an engaging Instagram caption for a post about: %s. " +
            "Make it creative, authentic, and include 3-5 relevant hashtags. " +
            "Keep it under 150 characters.",
            context
        );
    }
}
