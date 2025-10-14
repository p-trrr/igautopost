package com.igautopost.upload;

import com.igautopost.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for S3ImageUploader using Mockito to mock AWS SDK.
 */
@ExtendWith(MockitoExtension.class)
class S3ImageUploaderTest {
    
    @Mock
    private S3Client s3Client;
    
    private AppConfig config;
    private S3ImageUploader uploader;
    
    @BeforeEach
    void setUp() {
        config = new AppConfig.Builder()
            .awsAccessKey("test-access-key")
            .awsSecretKey("test-secret-key")
            .awsRegion("us-east-1")
            .awsS3Bucket("test-bucket")
            .openaiApiKey("test-key")
            .instagramAccessToken("test-token")
            .instagramBusinessAccountId("test-id")
            .build();
        
        uploader = new S3ImageUploader(config, s3Client);
    }
    
    @Test
    void testUploadImage_Success() throws IOException {
        // Arrange
        File tempFile = Files.createTempFile("test-image", ".jpg").toFile();
        tempFile.deleteOnExit();
        
        PutObjectResponse response = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
            .thenReturn(response);
        
        // Act
        String url = uploader.uploadImage(tempFile);
        
        // Assert
        assertNotNull(url);
        assertTrue(url.contains("https://"));
        assertTrue(url.contains("test-bucket"));
        assertTrue(url.contains("s3"));
        assertTrue(url.contains("us-east-1"));
        assertTrue(url.contains(".jpg"));
        
        // Verify S3 client was called
        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }
    
    @Test
    void testUploadImage_FileNotFound() {
        // Arrange
        File nonExistentFile = new File("/path/to/nonexistent/file.jpg");
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            uploader.uploadImage(nonExistentFile);
        });
        
        // Verify S3 client was not called
        verifyNoInteractions(s3Client);
    }
    
    @Test
    void testUploadImage_S3Exception() throws IOException {
        // Arrange
        File tempFile = Files.createTempFile("test-image", ".jpg").toFile();
        tempFile.deleteOnExit();
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
            .thenThrow(S3Exception.builder().message("Access denied").build());
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            uploader.uploadImage(tempFile);
        });
    }
    
    @Test
    void testUploadImage_WithPath() throws IOException {
        // Arrange
        File tempFile = Files.createTempFile("test-image", ".png").toFile();
        tempFile.deleteOnExit();
        String path = tempFile.getAbsolutePath();
        
        PutObjectResponse response = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
            .thenReturn(response);
        
        // Act
        String url = uploader.uploadImage(path);
        
        // Assert
        assertNotNull(url);
        assertTrue(url.contains(".png"));
    }
}
