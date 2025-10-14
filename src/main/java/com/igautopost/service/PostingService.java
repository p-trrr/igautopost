package com.igautopost.service;

import com.igautopost.model.InstagramPost;
import com.igautopost.upload.ImageUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Orchestrates the entire posting workflow:
 * 1. Optionally upload image to S3
 * 2. Generate caption with OpenAI
 * 3. Publish to Instagram
 */
public class PostingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PostingService.class);
    
    private final OpenAIService openAIService;
    private final InstagramService instagramService;
    private final ImageUploader imageUploader;
    
    public PostingService(OpenAIService openAIService, InstagramService instagramService, ImageUploader imageUploader) {
        this.openAIService = openAIService;
        this.instagramService = instagramService;
        this.imageUploader = imageUploader;
    }
    
    /**
     * Creates and publishes a post from a local image file.
     * 
     * @param localImagePath Path to the local image file
     * @param context Context for caption generation
     * @return The published post
     * @throws IOException if any step fails
     */
    public InstagramPost createAndPublishFromLocal(String localImagePath, String context) throws IOException {
        logger.info("Creating post from local image: {}", localImagePath);
        
        // Upload image to S3
        String imageUrl = imageUploader.uploadImage(localImagePath);
        logger.info("Image uploaded to: {}", imageUrl);
        
        // Generate caption
        String caption = openAIService.generateCaption(context);
        
        // Create and publish post
        InstagramPost post = new InstagramPost(imageUrl, caption);
        return instagramService.publishPost(post);
    }
    
    /**
     * Creates and publishes a post from an existing image URL.
     * 
     * @param imageUrl URL of the image (must be publicly accessible)
     * @param context Context for caption generation
     * @return The published post
     * @throws IOException if any step fails
     */
    public InstagramPost createAndPublishFromUrl(String imageUrl, String context) throws IOException {
        logger.info("Creating post from URL: {}", imageUrl);
        
        // Generate caption
        String caption = openAIService.generateCaption(context);
        
        // Create and publish post
        InstagramPost post = new InstagramPost(imageUrl, caption);
        return instagramService.publishPost(post);
    }
    
    /**
     * Publishes a pre-configured post.
     * 
     * @param post The post to publish
     * @return The published post
     * @throws IOException if publishing fails
     */
    public InstagramPost publishPost(InstagramPost post) throws IOException {
        logger.info("Publishing pre-configured post");
        return instagramService.publishPost(post);
    }
}
