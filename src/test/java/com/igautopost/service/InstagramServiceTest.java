package com.igautopost.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igautopost.config.AppConfig;
import com.igautopost.model.InstagramPost;
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
 * Tests for InstagramService using MockWebServer to avoid real API calls.
 */
class InstagramServiceTest {
    
    private MockWebServer mockWebServer;
    private InstagramService instagramService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        objectMapper = new ObjectMapper();
        
        AppConfig config = new AppConfig.Builder()
            .openaiApiKey("test-api-key")
            .instagramAccessToken("test-ig-token")
            .instagramBusinessAccountId("123456789")
            .instagramApiUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
            .build();
        
        OkHttpClient httpClient = new OkHttpClient();
        instagramService = new InstagramService(config, httpClient, objectMapper);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void testPublishPost_Success() throws Exception {
        // Arrange
        String createContainerResponse = "{\"id\": \"container-123\"}";
        String publishResponse = "{\"id\": \"post-456\"}";
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(createContainerResponse)
            .addHeader("Content-Type", "application/json"));
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(publishResponse)
            .addHeader("Content-Type", "application/json"));
        
        InstagramPost post = new InstagramPost(
            "https://example.com/image.jpg",
            "Test caption #test"
        );
        
        // Act
        InstagramPost result = instagramService.publishPost(post);
        
        // Assert
        assertNotNull(result);
        assertEquals("post-456", result.getMediaId());
        assertEquals(InstagramPost.PostStatus.PUBLISHED, result.getStatus());
        
        // Verify requests
        RecordedRequest createRequest = mockWebServer.takeRequest();
        assertTrue(createRequest.getPath().contains("/123456789/media"));
        assertTrue(createRequest.getPath().contains("image_url="));
        assertTrue(createRequest.getPath().contains("caption="));
        
        RecordedRequest publishRequest = mockWebServer.takeRequest();
        assertTrue(publishRequest.getPath().contains("/123456789/media_publish"));
        assertTrue(publishRequest.getPath().contains("creation_id=container-123"));
    }
    
    @Test
    void testPublishPost_CreateContainerFails() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setBody("{\"error\": \"Invalid image URL\"}"));
        
        InstagramPost post = new InstagramPost(
            "invalid-url",
            "Test caption"
        );
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            instagramService.publishPost(post);
        });
    }
    
    @Test
    void testPublishPost_PublishFails() throws InterruptedException {
        // Arrange
        String createContainerResponse = "{\"id\": \"container-123\"}";
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(createContainerResponse)
            .addHeader("Content-Type", "application/json"));
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setBody("{\"error\": \"Media not ready\"}"));
        
        InstagramPost post = new InstagramPost(
            "https://example.com/image.jpg",
            "Test caption"
        );
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            instagramService.publishPost(post);
        });
    }
}
