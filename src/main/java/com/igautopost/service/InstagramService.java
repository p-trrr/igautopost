package com.igautopost.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igautopost.config.AppConfig;
import com.igautopost.model.InstagramPost;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service for publishing posts to Instagram using the Instagram Graph API.
 * Supports Instagram Business accounts only.
 */
public class InstagramService {
    
    private static final Logger logger = LoggerFactory.getLogger(InstagramService.class);
    
    private final AppConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public InstagramService(AppConfig config, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publishes a post to Instagram in two steps:
     * 1. Create a media container
     * 2. Publish the media container
     * 
     * @param post The post to publish
     * @return The published post with updated status
     * @throws IOException if API calls fail
     */
    public InstagramPost publishPost(InstagramPost post) throws IOException {
        logger.info("Publishing post to Instagram: {}", post);
        
        // Step 1: Create media container
        String containerId = createMediaContainer(post.getImageUrl(), post.getCaption());
        post.setMediaId(containerId);
        post.setStatus(InstagramPost.PostStatus.CONTAINER_CREATED);
        logger.info("Media container created: {}", containerId);
        
        // Wait a few seconds for Instagram to process the media
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for media processing", e);
        }
        
        // Step 2: Publish the media container
        String publishedMediaId = publishMediaContainer(containerId);
        post.setMediaId(publishedMediaId);
        post.setStatus(InstagramPost.PostStatus.PUBLISHED);
        logger.info("Post published successfully: {}", publishedMediaId);
        
        return post;
    }
    
    /**
     * Creates a media container for the image and caption.
     */
    private String createMediaContainer(String imageUrl, String caption) throws IOException {
        String url = String.format("%s/%s/media",
            config.getInstagramApiUrl(),
            config.getInstagramBusinessAccountId()
        );
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("image_url", imageUrl);
        urlBuilder.addQueryParameter("caption", caption);
        urlBuilder.addQueryParameter("access_token", config.getInstagramAccessToken());
        
        Request request = new Request.Builder()
            .url(urlBuilder.build())
            .post(RequestBody.create("", null))
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("Failed to create media container: {} - {}", response.code(), errorBody);
                throw new IOException("Failed to create media container: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (!jsonNode.has("id")) {
                throw new IOException("No container ID in response: " + responseBody);
            }
            
            return jsonNode.get("id").asText();
        }
    }
    
    /**
     * Publishes a media container.
     */
    private String publishMediaContainer(String containerId) throws IOException {
        String url = String.format("%s/%s/media_publish",
            config.getInstagramApiUrl(),
            config.getInstagramBusinessAccountId()
        );
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("creation_id", containerId);
        urlBuilder.addQueryParameter("access_token", config.getInstagramAccessToken());
        
        Request request = new Request.Builder()
            .url(urlBuilder.build())
            .post(RequestBody.create("", null))
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("Failed to publish media: {} - {}", response.code(), errorBody);
                throw new IOException("Failed to publish media: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (!jsonNode.has("id")) {
                throw new IOException("No media ID in response: " + responseBody);
            }
            
            return jsonNode.get("id").asText();
        }
    }
}
