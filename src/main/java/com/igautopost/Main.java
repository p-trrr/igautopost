package com.igautopost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igautopost.config.AppConfig;
import com.igautopost.scheduler.PostScheduler;
import com.igautopost.service.InstagramService;
import com.igautopost.service.OpenAIService;
import com.igautopost.service.PostingService;
import com.igautopost.upload.ImageUploader;
import com.igautopost.upload.S3ImageUploader;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Main application class for Instagram Auto Post.
 * 
 * Usage examples:
 * 1. One-time post from URL:
 *    java -jar igautopost.jar post-url <image-url> <context>
 * 
 * 2. One-time post from local file:
 *    java -jar igautopost.jar post-local <image-path> <context>
 * 
 * 3. Start scheduler:
 *    java -jar igautopost.jar schedule
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            // Load configuration from environment variables
            AppConfig config = AppConfig.fromEnvironment();
            config.validate();
            
            // Initialize dependencies
            OkHttpClient httpClient = createHttpClient();
            ObjectMapper objectMapper = createObjectMapper();
            
            // Initialize services
            OpenAIService openAIService = new OpenAIService(config, httpClient, objectMapper);
            InstagramService instagramService = new InstagramService(config, httpClient, objectMapper);
            ImageUploader imageUploader = new S3ImageUploader(config);
            PostingService postingService = new PostingService(openAIService, instagramService, imageUploader);
            
            // Parse command line arguments
            if (args.length == 0) {
                printUsage();
                System.exit(1);
            }
            
            String command = args[0];
            
            switch (command) {
                case "post-url":
                    if (args.length < 3) {
                        System.err.println("Usage: post-url <image-url> <context>");
                        System.exit(1);
                    }
                    handlePostFromUrl(postingService, args[1], args[2]);
                    break;
                    
                case "post-local":
                    if (args.length < 3) {
                        System.err.println("Usage: post-local <image-path> <context>");
                        System.exit(1);
                    }
                    handlePostFromLocal(postingService, args[1], args[2]);
                    break;
                    
                case "schedule":
                    handleSchedule(config, postingService);
                    break;
                    
                default:
                    System.err.println("Unknown command: " + command);
                    printUsage();
                    System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("Application error", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void handlePostFromUrl(PostingService postingService, String imageUrl, String context) {
        try {
            logger.info("Posting from URL: {}", imageUrl);
            var post = postingService.createAndPublishFromUrl(imageUrl, context);
            System.out.println("Post published successfully!");
            System.out.println("Media ID: " + post.getMediaId());
            System.out.println("Caption: " + post.getCaption());
        } catch (Exception e) {
            logger.error("Failed to post from URL", e);
            throw new RuntimeException("Failed to post from URL", e);
        }
    }
    
    private static void handlePostFromLocal(PostingService postingService, String imagePath, String context) {
        try {
            logger.info("Posting from local file: {}", imagePath);
            var post = postingService.createAndPublishFromLocal(imagePath, context);
            System.out.println("Post published successfully!");
            System.out.println("Media ID: " + post.getMediaId());
            System.out.println("Caption: " + post.getCaption());
            System.out.println("Image URL: " + post.getImageUrl());
        } catch (Exception e) {
            logger.error("Failed to post from local file", e);
            throw new RuntimeException("Failed to post from local file", e);
        }
    }
    
    private static void handleSchedule(AppConfig config, PostingService postingService) {
        logger.info("Starting scheduled posting mode");
        
        PostScheduler scheduler = new PostScheduler(config, postingService);
        
        // Example task: This should be customized based on your needs
        // In production, this would read from a database or queue
        scheduler.start(service -> {
            logger.info("Scheduled task triggered - implement your posting logic here");
            // Example: service.createAndPublishFromUrl(imageUrl, context);
        });
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down scheduler...");
            scheduler.stop();
        }));
        
        System.out.println("Scheduler started. Press Ctrl+C to stop.");
        
        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Application interrupted");
        }
    }
    
    private static OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    private static void printUsage() {
        System.out.println("Instagram Auto Post - Usage:");
        System.out.println();
        System.out.println("  post-url <image-url> <context>");
        System.out.println("    Post an image from a URL with AI-generated caption");
        System.out.println();
        System.out.println("  post-local <image-path> <context>");
        System.out.println("    Upload a local image to S3 and post with AI-generated caption");
        System.out.println();
        System.out.println("  schedule");
        System.out.println("    Start the scheduler for automated posting");
        System.out.println();
    }
}
