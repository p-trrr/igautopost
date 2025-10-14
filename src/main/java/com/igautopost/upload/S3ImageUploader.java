package com.igautopost.upload;

import com.igautopost.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Service for uploading images to AWS S3.
 * Provides publicly accessible URLs for uploaded images.
 */
public class S3ImageUploader implements ImageUploader {
    
    private static final Logger logger = LoggerFactory.getLogger(S3ImageUploader.class);
    
    private final AppConfig config;
    private final S3Client s3Client;
    
    public S3ImageUploader(AppConfig config) {
        this.config = config;
        this.s3Client = createS3Client();
    }
    
    /**
     * Constructor for dependency injection (useful for testing).
     */
    public S3ImageUploader(AppConfig config, S3Client s3Client) {
        this.config = config;
        this.s3Client = s3Client;
    }
    
    private S3Client createS3Client() {
        if (config.getAwsAccessKey() == null || config.getAwsAccessKey().isEmpty()) {
            logger.warn("AWS credentials not configured, S3 upload will not work");
            return null;
        }
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            config.getAwsAccessKey(),
            config.getAwsSecretKey()
        );
        
        return S3Client.builder()
            .region(Region.of(config.getAwsRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
    
    @Override
    public String uploadImage(File imageFile) throws IOException {
        if (s3Client == null) {
            throw new IllegalStateException("S3 client not configured. Set AWS credentials in environment variables.");
        }
        
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new IOException("Image file does not exist: " + imageFile.getAbsolutePath());
        }
        
        String key = generateKey(imageFile.getName());
        String contentType = determineContentType(imageFile.toPath());
        
        logger.info("Uploading image to S3: {} -> s3://{}/{}", imageFile.getName(), config.getAwsS3Bucket(), key);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(config.getAwsS3Bucket())
                .key(key)
                .contentType(contentType)
                .build();
            
            PutObjectResponse response = s3Client.putObject(
                putObjectRequest,
                RequestBody.fromFile(imageFile)
            );
            
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                config.getAwsS3Bucket(),
                config.getAwsRegion(),
                key
            );
            
            logger.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;
            
        } catch (S3Exception e) {
            logger.error("Failed to upload image to S3", e);
            throw new IOException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String uploadImage(String localPath) throws IOException {
        return uploadImage(new File(localPath));
    }
    
    /**
     * Generates a unique key for S3 storage.
     */
    private String generateKey(String originalFilename) {
        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
        }
        return "instagram-posts/" + UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Determines the content type of the image file.
     */
    private String determineContentType(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            // Fallback based on file extension
            String filename = path.getFileName().toString().toLowerCase();
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (filename.endsWith(".png")) {
                return "image/png";
            } else if (filename.endsWith(".gif")) {
                return "image/gif";
            }
            return "application/octet-stream";
        }
        return contentType;
    }
    
    /**
     * Closes the S3 client.
     */
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
