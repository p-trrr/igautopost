# Instagram Auto Post

A Java application that automatically publishes Instagram posts using AI-generated captions. This application integrates with OpenAI API for caption generation and Instagram Graph API for posting to Instagram Business accounts.

## Features

- ✨ **AI-Powered Captions**: Generates engaging Instagram captions using OpenAI's GPT models
- 📸 **Instagram Integration**: Posts to Instagram Business accounts via Graph API
- ☁️ **S3 Image Upload**: Uploads local images to AWS S3 for public access
- ⏰ **Scheduling**: Built-in scheduler for automated posting at regular intervals
- 🔒 **Secure Configuration**: All credentials stored in environment variables
- 🧪 **Mock Testing**: Comprehensive test suite with mock APIs (no real API calls during tests)
- 🔧 **Production-Ready**: Includes error handling, logging, and production recommendations

## Architecture

```
┌─────────────────┐
│   Main App      │
└────────┬────────┘
         │
    ┌────┴────┐
    │ PostingService │
    └─┬───┬───┬──┘
      │   │   │
┌─────┴─┐ │ ┌─┴──────┐
│OpenAI │ │ │Instagram│
│Service│ │ │Service  │
└───────┘ │ └─────────┘
    ┌─────┴────┐
    │S3Uploader│
    └──────────┘
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- OpenAI API key
- Instagram Business Account with Graph API access
- AWS account (optional, for S3 image uploads)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/p-trrr/igautopost.git
cd igautopost
```

### 2. Build the Project

```bash
mvn clean package
```

This creates an executable JAR file: `target/igautopost-1.0.0-SNAPSHOT.jar`

### 3. Configure Environment Variables

Copy the example environment file and configure your credentials:

```bash
cp .env.example .env
# Edit .env with your actual credentials
```

Required environment variables:

```bash
# OpenAI API
OPENAI_API_KEY=sk-...                           # Required
OPENAI_MODEL=gpt-4                              # Optional (default: gpt-4)

# Instagram Graph API
INSTAGRAM_ACCESS_TOKEN=YOUR_ACCESS_TOKEN        # Required
INSTAGRAM_BUSINESS_ACCOUNT_ID=YOUR_ACCOUNT_ID   # Required

# AWS S3 (optional, only for local image uploads)
AWS_ACCESS_KEY=YOUR_AWS_ACCESS_KEY
AWS_SECRET_KEY=YOUR_AWS_SECRET_KEY
AWS_REGION=us-east-1
AWS_S3_BUCKET=your-bucket-name

# Scheduling
SCHEDULING_INTERVAL_MINUTES=60                  # Optional (default: 60)
```

### 4. Set Environment Variables

**Linux/Mac:**
```bash
export $(cat .env | xargs)
```

**Windows (PowerShell):**
```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '(.+?)=(.+)') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}
```

## Usage

The application supports three modes of operation:

### 1. Post from URL (One-time)

Post an image from a publicly accessible URL:

```bash
java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-url \
  "https://example.com/image.jpg" \
  "Beautiful sunset at the beach"
```

### 2. Post from Local File (One-time)

Upload a local image to S3 and post to Instagram:

```bash
java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-local \
  "/path/to/image.jpg" \
  "My awesome travel photo"
```

### 3. Scheduled Posting

Start the scheduler for automated posting:

```bash
java -jar target/igautopost-1.0.0-SNAPSHOT.jar schedule
```

> **Note**: In schedule mode, you need to implement your posting logic in the `Main.handleSchedule()` method. This could read from a database, queue, or file system.

## API Setup

### Getting Instagram Access Token

1. Create a Facebook App at [developers.facebook.com](https://developers.facebook.com)
2. Add Instagram Graph API product
3. Generate a User Access Token with `instagram_basic`, `instagram_content_publish` permissions
4. Convert to Long-Lived Access Token (valid for 60 days)
5. Get your Instagram Business Account ID

Detailed guide: [Instagram Graph API Documentation](https://developers.facebook.com/docs/instagram-api/getting-started)

### Getting OpenAI API Key

1. Sign up at [platform.openai.com](https://platform.openai.com)
2. Navigate to API Keys section
3. Create a new API key
4. Set usage limits as needed

### Setting up AWS S3 (Optional)

1. Create an S3 bucket in AWS Console
2. Configure bucket policy for public read access
3. Create IAM user with `s3:PutObject` permission
4. Generate access key and secret key

Example S3 bucket policy for public read access:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::your-bucket-name/*"
    }
  ]
}
```

## Development

### Project Structure

```
src/
├── main/java/com/igautopost/
│   ├── Main.java                    # Application entry point
│   ├── config/
│   │   └── AppConfig.java           # Configuration management
│   ├── model/
│   │   ├── InstagramPost.java       # Post data model
│   │   ├── OpenAIRequest.java       # OpenAI request model
│   │   └── OpenAIResponse.java      # OpenAI response model
│   ├── service/
│   │   ├── OpenAIService.java       # OpenAI API integration
│   │   ├── InstagramService.java    # Instagram API integration
│   │   └── PostingService.java      # Orchestration service
│   ├── scheduler/
│   │   └── PostScheduler.java       # Scheduling logic
│   └── upload/
│       ├── ImageUploader.java       # Upload interface
│       └── S3ImageUploader.java     # S3 implementation
└── test/java/com/igautopost/        # Unit tests
```

### Running Tests

```bash
mvn test
```

### Building

```bash
mvn clean package
```

## Mock Testing Strategy

This project uses a comprehensive mock testing strategy to avoid making real API calls during tests:

### 1. MockWebServer for HTTP APIs

For testing OpenAI and Instagram services, we use OkHttp's `MockWebServer`:

```java
MockWebServer mockWebServer = new MockWebServer();
mockWebServer.enqueue(new MockResponse()
    .setBody("{\"id\": \"test-123\"}")
    .addHeader("Content-Type", "application/json"));
```

**Benefits:**
- Tests run without network access
- Full control over responses (success, errors, edge cases)
- Fast execution
- No API costs during testing

### 2. Mockito for Dependencies

For testing service orchestration, we use Mockito to mock dependencies:

```java
@Mock
private OpenAIService openAIService;

when(openAIService.generateCaption(any())).thenReturn("Test caption");
```

**Benefits:**
- Isolated unit tests
- Easy to test error scenarios
- Verifiable interactions between components

### 3. AWS SDK Mocking

For S3 uploads, we inject a mocked `S3Client`:

```java
@Mock
private S3Client s3Client;

S3ImageUploader uploader = new S3ImageUploader(config, s3Client);
```

### Test Coverage

- ✅ OpenAI caption generation (success, errors, empty responses)
- ✅ Instagram posting workflow (container creation, publishing, errors)
- ✅ S3 image uploads (success, file not found, S3 errors)
- ✅ Posting service orchestration (full workflow, partial failures)
- ✅ Scheduler execution (task execution, error handling, shutdown)

## Production Recommendations

### 1. Security

- ✅ **Implemented**: Environment variables for credentials
- 🔒 **Recommend**: Use AWS Secrets Manager or HashiCorp Vault for production
- 🔒 **Recommend**: Rotate Instagram tokens before expiration (60 days for long-lived tokens)
- 🔒 **Recommend**: Use IAM roles instead of access keys when running on AWS (EC2, ECS, Lambda)
- 🔒 **Recommend**: Enable HTTPS-only communication
- 🔒 **Recommend**: Implement rate limiting to prevent API abuse

### 2. Reliability

- 🔧 **Recommend**: Add retry logic with exponential backoff for API calls
- 🔧 **Recommend**: Implement circuit breaker pattern for external services
- 🔧 **Recommend**: Add health checks and monitoring endpoints
- 🔧 **Recommend**: Use persistent queue (SQS, RabbitMQ) for post scheduling
- 🔧 **Recommend**: Store post metadata in database (PostgreSQL, DynamoDB)

### 3. Scalability

- 📈 **Recommend**: Deploy on containerized platform (ECS, Kubernetes)
- 📈 **Recommend**: Use multiple worker instances with distributed scheduling
- 📈 **Recommend**: Implement job queue for parallel processing
- 📈 **Recommend**: Add caching layer (Redis) for API responses
- 📈 **Recommend**: Use CDN for image delivery

### 4. Monitoring & Observability

- 📊 **Recommend**: Integrate with APM tools (New Relic, Datadog, CloudWatch)
- 📊 **Recommend**: Add structured logging with correlation IDs
- 📊 **Recommend**: Track metrics: posts published, API errors, latency
- 📊 **Recommend**: Set up alerts for failures and rate limit warnings
- 📊 **Recommend**: Use distributed tracing (Jaeger, X-Ray)

### 5. Cost Optimization

- 💰 **Recommend**: Use OpenAI's cheaper models (gpt-3.5-turbo) if quality acceptable
- 💰 **Recommend**: Implement caption caching for similar contexts
- 💰 **Recommend**: Use S3 lifecycle policies to archive old images
- 💰 **Recommend**: Monitor API usage and set billing alerts
- 💰 **Recommend**: Batch operations where possible

### 6. Enhanced Features

- ⭐ **Recommend**: Add image preprocessing (resize, compress, watermark)
- ⭐ **Recommend**: Support video uploads (Instagram supports MP4)
- ⭐ **Recommend**: Implement carousel posts (multiple images)
- ⭐ **Recommend**: Add hashtag suggestion based on image analysis
- ⭐ **Recommend**: Schedule posts for optimal engagement times
- ⭐ **Recommend**: Support Instagram Stories
- ⭐ **Recommend**: Add A/B testing for captions
- ⭐ **Recommend**: Implement content moderation before posting

### 7. Error Handling

- 🐛 **Recommend**: Add dead letter queue for failed posts
- 🐛 **Recommend**: Implement graceful degradation
- 🐛 **Recommend**: Add detailed error logging with context
- 🐛 **Recommend**: Create alerting for critical errors
- 🐛 **Recommend**: Implement automatic token refresh

### 8. Deployment Architecture

**Recommended Production Setup:**

```
┌─────────────────────────────────────────────┐
│              Load Balancer                  │
└─────────────┬───────────────────────────────┘
              │
     ┌────────┴──────────┐
     │                   │
┌────▼────┐         ┌────▼────┐
│Worker 1 │         │Worker 2 │
└────┬────┘         └────┬────┘
     │                   │
     └────────┬──────────┘
              │
    ┌─────────▼────────────┐
    │   Message Queue      │
    │   (SQS/RabbitMQ)     │
    └─────────┬────────────┘
              │
    ┌─────────▼────────────┐
    │   Database           │
    │   (RDS/DynamoDB)     │
    └──────────────────────┘
```

### 9. Compliance & Content Policy

- 📋 **Recommend**: Validate content against Instagram's Community Guidelines
- 📋 **Recommend**: Implement content approval workflow
- 📋 **Recommend**: Store audit logs for compliance
- 📋 **Recommend**: Add GDPR-compliant data handling
- 📋 **Recommend**: Implement user consent mechanisms

## Troubleshooting

### Common Issues

**1. "OPENAI_API_KEY environment variable is required"**
- Ensure environment variables are set before running the application
- Check that `.env` file is properly sourced

**2. "Failed to create media container: 400"**
- Verify image URL is publicly accessible
- Check image meets Instagram requirements (JPEG/PNG, max 8MB)
- Ensure Instagram Business Account is properly configured

**3. "S3 client not configured"**
- AWS credentials must be set for local image uploads
- Use `post-url` command if you already have hosted images

**4. "Media not ready" error during publishing**
- Instagram needs time to process the media container
- The app waits 3 seconds by default; increase if needed

## API Rate Limits

- **Instagram Graph API**: 200 calls per hour per user
- **OpenAI API**: Depends on your plan (e.g., 3,500 requests/min for GPT-4)
- **AWS S3**: No rate limit for standard operations

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is provided as-is for educational and personal use.

## Disclaimer

- This application is for Instagram Business accounts only
- Ensure compliance with Instagram's Terms of Service and API policies
- Test thoroughly before using in production
- The author is not responsible for any API costs or policy violations

## Support

For issues and questions:
- Open an issue on GitHub
- Check Instagram Graph API documentation
- Review OpenAI API documentation

## Changelog

### Version 1.0.0
- Initial release
- OpenAI integration for caption generation
- Instagram Graph API integration for posting
- S3 support for local image uploads
- Scheduling capability
- Comprehensive test suite
- Production recommendations

---

Built with ☕ and Java