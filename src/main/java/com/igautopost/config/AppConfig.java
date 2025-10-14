package com.igautopost.config;

/**
 * Configuration class to manage environment variables and API credentials.
 * All sensitive information should be stored in environment variables.
 */
public class AppConfig {
    
    // OpenAI Configuration
    private final String openaiApiKey;
    private final String openaiApiUrl;
    private final String openaiModel;
    
    // Instagram Configuration
    private final String instagramAccessToken;
    private final String instagramBusinessAccountId;
    private final String instagramApiUrl;
    
    // AWS S3 Configuration
    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String awsRegion;
    private final String awsS3Bucket;
    
    // Scheduling Configuration
    private final int schedulingIntervalMinutes;
    
    private AppConfig(Builder builder) {
        this.openaiApiKey = builder.openaiApiKey;
        this.openaiApiUrl = builder.openaiApiUrl;
        this.openaiModel = builder.openaiModel;
        this.instagramAccessToken = builder.instagramAccessToken;
        this.instagramBusinessAccountId = builder.instagramBusinessAccountId;
        this.instagramApiUrl = builder.instagramApiUrl;
        this.awsAccessKey = builder.awsAccessKey;
        this.awsSecretKey = builder.awsSecretKey;
        this.awsRegion = builder.awsRegion;
        this.awsS3Bucket = builder.awsS3Bucket;
        this.schedulingIntervalMinutes = builder.schedulingIntervalMinutes;
    }
    
    /**
     * Creates AppConfig instance from environment variables.
     */
    public static AppConfig fromEnvironment() {
        return new Builder()
            .openaiApiKey(getEnvOrDefault("OPENAI_API_KEY", ""))
            .openaiApiUrl(getEnvOrDefault("OPENAI_API_URL", "https://api.openai.com/v1"))
            .openaiModel(getEnvOrDefault("OPENAI_MODEL", "gpt-4"))
            .instagramAccessToken(getEnvOrDefault("INSTAGRAM_ACCESS_TOKEN", ""))
            .instagramBusinessAccountId(getEnvOrDefault("INSTAGRAM_BUSINESS_ACCOUNT_ID", ""))
            .instagramApiUrl(getEnvOrDefault("INSTAGRAM_API_URL", "https://graph.facebook.com/v18.0"))
            .awsAccessKey(getEnvOrDefault("AWS_ACCESS_KEY", ""))
            .awsSecretKey(getEnvOrDefault("AWS_SECRET_KEY", ""))
            .awsRegion(getEnvOrDefault("AWS_REGION", "us-east-1"))
            .awsS3Bucket(getEnvOrDefault("AWS_S3_BUCKET", ""))
            .schedulingIntervalMinutes(Integer.parseInt(getEnvOrDefault("SCHEDULING_INTERVAL_MINUTES", "60")))
            .build();
    }
    
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
    
    public void validate() {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is required");
        }
        if (instagramAccessToken == null || instagramAccessToken.isEmpty()) {
            throw new IllegalStateException("INSTAGRAM_ACCESS_TOKEN environment variable is required");
        }
        if (instagramBusinessAccountId == null || instagramBusinessAccountId.isEmpty()) {
            throw new IllegalStateException("INSTAGRAM_BUSINESS_ACCOUNT_ID environment variable is required");
        }
    }
    
    // Getters
    public String getOpenaiApiKey() { return openaiApiKey; }
    public String getOpenaiApiUrl() { return openaiApiUrl; }
    public String getOpenaiModel() { return openaiModel; }
    public String getInstagramAccessToken() { return instagramAccessToken; }
    public String getInstagramBusinessAccountId() { return instagramBusinessAccountId; }
    public String getInstagramApiUrl() { return instagramApiUrl; }
    public String getAwsAccessKey() { return awsAccessKey; }
    public String getAwsSecretKey() { return awsSecretKey; }
    public String getAwsRegion() { return awsRegion; }
    public String getAwsS3Bucket() { return awsS3Bucket; }
    public int getSchedulingIntervalMinutes() { return schedulingIntervalMinutes; }
    
    public static class Builder {
        private String openaiApiKey;
        private String openaiApiUrl;
        private String openaiModel;
        private String instagramAccessToken;
        private String instagramBusinessAccountId;
        private String instagramApiUrl;
        private String awsAccessKey;
        private String awsSecretKey;
        private String awsRegion;
        private String awsS3Bucket;
        private int schedulingIntervalMinutes;
        
        public Builder openaiApiKey(String openaiApiKey) {
            this.openaiApiKey = openaiApiKey;
            return this;
        }
        
        public Builder openaiApiUrl(String openaiApiUrl) {
            this.openaiApiUrl = openaiApiUrl;
            return this;
        }
        
        public Builder openaiModel(String openaiModel) {
            this.openaiModel = openaiModel;
            return this;
        }
        
        public Builder instagramAccessToken(String instagramAccessToken) {
            this.instagramAccessToken = instagramAccessToken;
            return this;
        }
        
        public Builder instagramBusinessAccountId(String instagramBusinessAccountId) {
            this.instagramBusinessAccountId = instagramBusinessAccountId;
            return this;
        }
        
        public Builder instagramApiUrl(String instagramApiUrl) {
            this.instagramApiUrl = instagramApiUrl;
            return this;
        }
        
        public Builder awsAccessKey(String awsAccessKey) {
            this.awsAccessKey = awsAccessKey;
            return this;
        }
        
        public Builder awsSecretKey(String awsSecretKey) {
            this.awsSecretKey = awsSecretKey;
            return this;
        }
        
        public Builder awsRegion(String awsRegion) {
            this.awsRegion = awsRegion;
            return this;
        }
        
        public Builder awsS3Bucket(String awsS3Bucket) {
            this.awsS3Bucket = awsS3Bucket;
            return this;
        }
        
        public Builder schedulingIntervalMinutes(int schedulingIntervalMinutes) {
            this.schedulingIntervalMinutes = schedulingIntervalMinutes;
            return this;
        }
        
        public AppConfig build() {
            return new AppConfig(this);
        }
    }
}
