package com.igautopost.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igautopost.config.AppConfig;
import com.igautopost.model.OpenAIResponse;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OpenAIService using MockWebServer to avoid real API calls.
 */
class OpenAIServiceTest {
    
    private MockWebServer mockWebServer;
    private OpenAIService openAIService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        objectMapper = new ObjectMapper();
        
        AppConfig config = new AppConfig.Builder()
            .openaiApiKey("test-api-key")
            .openaiApiUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
            .openaiModel("gpt-4")
            .instagramAccessToken("test-ig-token")
            .instagramBusinessAccountId("test-ig-id")
            .build();
        
        OkHttpClient httpClient = new OkHttpClient();
        openAIService = new OpenAIService(config, httpClient, objectMapper);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void testGenerateCaption_Success() throws Exception {
        // Arrange
        String mockResponse = "{" +
            "\"id\": \"chatcmpl-123\"," +
            "\"object\": \"chat.completion\"," +
            "\"created\": 1677652288," +
            "\"model\": \"gpt-4\"," +
            "\"choices\": [{" +
            "\"index\": 0," +
            "\"message\": {" +
            "\"role\": \"assistant\"," +
            "\"content\": \"Beautiful sunset vibes! 🌅 #sunset #nature #photography\"" +
            "}," +
            "\"finish_reason\": \"stop\"" +
            "}]," +
            "\"usage\": {" +
            "\"prompt_tokens\": 20," +
            "\"completion_tokens\": 15," +
            "\"total_tokens\": 35" +
            "}" +
            "}";
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .addHeader("Content-Type", "application/json"));
        
        // Act
        String caption = openAIService.generateCaption("sunset at the beach");
        
        // Assert
        assertNotNull(caption);
        assertTrue(caption.contains("sunset") || caption.contains("Beautiful"));
        
        // Verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/chat/completions", request.getPath());
        assertEquals("POST", request.getMethod());
        assertTrue(request.getHeader("Authorization").contains("Bearer test-api-key"));
    }
    
    @Test
    void testGenerateCaption_ApiError() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(401)
            .setBody("{\"error\": \"Invalid API key\"}"));
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            openAIService.generateCaption("test context");
        });
    }
    
    @Test
    void testGenerateCaption_EmptyResponse() {
        // Arrange
        String mockResponse = "{" +
            "\"id\": \"chatcmpl-123\"," +
            "\"choices\": []" +
            "}";
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .addHeader("Content-Type", "application/json"));
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            openAIService.generateCaption("test context");
        });
    }
}
