package com.igautopost.service;

import com.igautopost.model.InstagramPost;
import com.igautopost.upload.ImageUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for PostingService using Mockito to mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
class PostingServiceTest {
    
    @Mock
    private OpenAIService openAIService;
    
    @Mock
    private InstagramService instagramService;
    
    @Mock
    private ImageUploader imageUploader;
    
    private PostingService postingService;
    
    @BeforeEach
    void setUp() {
        postingService = new PostingService(openAIService, instagramService, imageUploader);
    }
    
    @Test
    void testCreateAndPublishFromLocal_Success() throws IOException {
        // Arrange
        String localPath = "/path/to/image.jpg";
        String context = "sunset at the beach";
        String uploadedUrl = "https://s3.amazonaws.com/bucket/image.jpg";
        String generatedCaption = "Beautiful sunset! #sunset #beach";
        
        when(imageUploader.uploadImage(localPath)).thenReturn(uploadedUrl);
        when(openAIService.generateCaption(context)).thenReturn(generatedCaption);
        when(instagramService.publishPost(any(InstagramPost.class))).thenAnswer(invocation -> {
            InstagramPost post = invocation.getArgument(0);
            post.setMediaId("post-123");
            post.setStatus(InstagramPost.PostStatus.PUBLISHED);
            return post;
        });
        
        // Act
        InstagramPost result = postingService.createAndPublishFromLocal(localPath, context);
        
        // Assert
        assertNotNull(result);
        assertEquals(uploadedUrl, result.getImageUrl());
        assertEquals(generatedCaption, result.getCaption());
        assertEquals("post-123", result.getMediaId());
        assertEquals(InstagramPost.PostStatus.PUBLISHED, result.getStatus());
        
        // Verify interactions
        verify(imageUploader).uploadImage(localPath);
        verify(openAIService).generateCaption(context);
        verify(instagramService).publishPost(any(InstagramPost.class));
    }
    
    @Test
    void testCreateAndPublishFromUrl_Success() throws IOException {
        // Arrange
        String imageUrl = "https://example.com/image.jpg";
        String context = "mountain hiking";
        String generatedCaption = "Adventure awaits! #hiking #mountains";
        
        when(openAIService.generateCaption(context)).thenReturn(generatedCaption);
        when(instagramService.publishPost(any(InstagramPost.class))).thenAnswer(invocation -> {
            InstagramPost post = invocation.getArgument(0);
            post.setMediaId("post-456");
            post.setStatus(InstagramPost.PostStatus.PUBLISHED);
            return post;
        });
        
        // Act
        InstagramPost result = postingService.createAndPublishFromUrl(imageUrl, context);
        
        // Assert
        assertNotNull(result);
        assertEquals(imageUrl, result.getImageUrl());
        assertEquals(generatedCaption, result.getCaption());
        assertEquals("post-456", result.getMediaId());
        assertEquals(InstagramPost.PostStatus.PUBLISHED, result.getStatus());
        
        // Verify interactions
        verify(openAIService).generateCaption(context);
        verify(instagramService).publishPost(any(InstagramPost.class));
        verifyNoInteractions(imageUploader);
    }
    
    @Test
    void testCreateAndPublishFromLocal_UploadFails() throws IOException {
        // Arrange
        String localPath = "/path/to/image.jpg";
        String context = "test";
        
        when(imageUploader.uploadImage(localPath)).thenThrow(new IOException("S3 upload failed"));
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            postingService.createAndPublishFromLocal(localPath, context);
        });
        
        // Verify that subsequent steps were not called
        verify(imageUploader).uploadImage(localPath);
        verifyNoInteractions(openAIService);
        verifyNoInteractions(instagramService);
    }
    
    @Test
    void testCreateAndPublishFromUrl_CaptionGenerationFails() throws IOException {
        // Arrange
        String imageUrl = "https://example.com/image.jpg";
        String context = "test";
        
        when(openAIService.generateCaption(context)).thenThrow(new IOException("OpenAI API error"));
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            postingService.createAndPublishFromUrl(imageUrl, context);
        });
        
        // Verify that subsequent steps were not called
        verify(openAIService).generateCaption(context);
        verifyNoInteractions(instagramService);
    }
}
