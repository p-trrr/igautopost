# Quick Start Guide

## Prerequisites
- Java 11+
- Maven 3.6+
- OpenAI API Key
- Instagram Business Account + Access Token

## Installation

1. **Clone and Build**
   ```bash
   git clone https://github.com/p-trrr/igautopost.git
   cd igautopost
   mvn clean package
   ```

2. **Configure Environment**
   ```bash
   cp .env.example .env
   # Edit .env with your credentials
   export $(cat .env | xargs)
   ```

3. **Test the Application**
   ```bash
   # Show help
   java -jar target/igautopost-1.0.0-SNAPSHOT.jar
   
   # Post from URL
   java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-url \
     "https://example.com/image.jpg" \
     "Beautiful sunset at the beach"
   ```

## Quick Commands

### Post from Image URL
```bash
java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-url \
  "https://your-image-url.jpg" \
  "Your image description or context"
```

### Post from Local File (requires AWS S3)
```bash
java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-local \
  "/path/to/image.jpg" \
  "Your image description or context"
```

### Start Scheduler
```bash
java -jar target/igautopost-1.0.0-SNAPSHOT.jar schedule
```

## Environment Variables

### Required
```bash
OPENAI_API_KEY=sk-...
INSTAGRAM_ACCESS_TOKEN=YOUR_TOKEN
INSTAGRAM_BUSINESS_ACCOUNT_ID=YOUR_ID
```

### Optional (for S3)
```bash
AWS_ACCESS_KEY=YOUR_KEY
AWS_SECRET_KEY=YOUR_SECRET
AWS_REGION=us-east-1
AWS_S3_BUCKET=your-bucket
```

## API Setup

### Instagram Access Token
1. Go to [Facebook Developers](https://developers.facebook.com)
2. Create a new app
3. Add Instagram Graph API
4. Generate token with permissions:
   - `instagram_basic`
   - `instagram_content_publish`
5. Convert to long-lived token (60 days)

### OpenAI API Key
1. Visit [OpenAI Platform](https://platform.openai.com)
2. Create API key
3. Set usage limits

### AWS S3 (Optional)
1. Create S3 bucket
2. Set public read policy
3. Create IAM user with PutObject permission
4. Generate access keys

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=OpenAIServiceTest

# Build without tests
mvn package -DskipTests
```

## Common Issues

**"OPENAI_API_KEY environment variable is required"**
- Set environment variables before running
- Use: `export $(cat .env | xargs)`

**"Failed to create media container: 400"**
- Check image URL is publicly accessible
- Verify image format (JPEG/PNG) and size (< 8MB)

**"S3 client not configured"**
- AWS credentials needed for local uploads
- Use `post-url` for already hosted images

## Project Structure

```
igautopost/
├── src/main/java/com/igautopost/
│   ├── Main.java                  # Entry point
│   ├── config/                    # Configuration
│   ├── model/                     # Data models
│   ├── service/                   # Business logic
│   ├── scheduler/                 # Scheduling
│   └── upload/                    # Image upload
├── src/test/java/                 # Tests
├── README.md                      # Full documentation
├── TESTING.md                     # Testing strategy
├── PRODUCTION.md                  # Deployment guide
└── pom.xml                        # Maven config
```

## Next Steps

1. ✅ Test with sample images
2. ✅ Set up production environment (see PRODUCTION.md)
3. ✅ Configure monitoring and alerts
4. ✅ Implement database for post queue
5. ✅ Set up automated token refresh
6. ✅ Add content moderation

## Resources

- [Full Documentation](README.md)
- [Testing Guide](TESTING.md)
- [Production Guide](PRODUCTION.md)
- [Instagram API Docs](https://developers.facebook.com/docs/instagram-api)
- [OpenAI API Docs](https://platform.openai.com/docs)

## Support

- Issues: GitHub Issues
- Documentation: See README.md
- API Help: Check service documentation
