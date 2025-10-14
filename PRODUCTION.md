# Production Deployment Guide

## Overview

This guide provides recommendations for deploying the Instagram Auto Post application in a production environment.

## Architecture Recommendations

### Option 1: AWS ECS/Fargate (Recommended)

**Benefits**:
- Fully managed container orchestration
- Auto-scaling based on queue depth
- Built-in integration with AWS services
- Cost-effective for variable workloads

**Architecture**:
```
┌─────────────┐
│   S3 Bucket │
│  (Images)   │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────┐
│     Application Load Balancer   │
└────────────┬────────────────────┘
             │
      ┌──────┴──────┐
      │             │
┌─────▼─────┐ ┌────▼──────┐
│ ECS Task 1│ │ECS Task 2 │
└─────┬─────┘ └────┬──────┘
      │            │
      └──────┬─────┘
             │
    ┌────────▼────────┐
    │   SQS Queue     │
    │ (Post Queue)    │
    └────────┬────────┘
             │
    ┌────────▼────────┐
    │   RDS Database  │
    │ (Post Metadata) │
    └─────────────────┘
```

### Option 2: Kubernetes

**Benefits**:
- Cloud-agnostic deployment
- Advanced orchestration features
- Strong community support

### Option 3: AWS Lambda (Serverless)

**Benefits**:
- Pay only for execution time
- Automatic scaling
- No server management

**Limitations**:
- 15-minute execution limit
- Cold start latency

## Environment Configuration

### Production Environment Variables

```bash
# Application
APP_ENV=production
LOG_LEVEL=INFO

# OpenAI
OPENAI_API_KEY=<from-secrets-manager>
OPENAI_MODEL=gpt-4
OPENAI_MAX_RETRIES=3

# Instagram
INSTAGRAM_ACCESS_TOKEN=<from-secrets-manager>
INSTAGRAM_BUSINESS_ACCOUNT_ID=<account-id>
INSTAGRAM_API_VERSION=v18.0

# AWS
AWS_REGION=us-east-1
AWS_S3_BUCKET=my-instagram-posts
AWS_S3_PREFIX=posts/

# Database
DB_HOST=<rds-endpoint>
DB_PORT=5432
DB_NAME=igautopost
DB_USER=<from-secrets-manager>
DB_PASSWORD=<from-secrets-manager>

# Queue
QUEUE_URL=<sqs-queue-url>
QUEUE_VISIBILITY_TIMEOUT=300
QUEUE_MAX_MESSAGES=10

# Monitoring
DATADOG_API_KEY=<from-secrets-manager>
NEW_RELIC_LICENSE_KEY=<from-secrets-manager>
```

### Secrets Management

**AWS Secrets Manager** (Recommended):
```java
// Retrieve secrets at startup
SecretsManagerClient client = SecretsManagerClient.create();
GetSecretValueResponse response = client.getSecretValue(
    GetSecretValueRequest.builder()
        .secretId("igautopost/production")
        .build()
);
String secretString = response.secretString();
```

**HashiCorp Vault**:
```bash
vault kv get -field=api_key secret/igautopost/openai
```

## Security Best Practices

### 1. Network Security
- Use VPC with private subnets for application
- Use security groups to restrict traffic
- Enable VPC endpoints for AWS services
- Use NAT Gateway for outbound internet access

### 2. IAM Roles
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject"
      ],
      "Resource": "arn:aws:s3:::my-instagram-posts/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "arn:aws:sqs:*:*:instagram-post-queue"
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "arn:aws:secretsmanager:*:*:secret:igautopost/*"
    }
  ]
}
```

### 3. Token Management
- Rotate Instagram access tokens before expiration (60 days)
- Implement automated token refresh
- Store tokens encrypted
- Use short-lived tokens when possible

### 4. Content Validation
- Validate image sizes (max 8MB for Instagram)
- Check image dimensions
- Scan for inappropriate content
- Validate caption length (max 2,200 characters)

## Monitoring & Observability

### Key Metrics to Track

```java
// Example metrics
- posts_published_total (counter)
- post_publish_duration_seconds (histogram)
- api_errors_total (counter by service)
- queue_depth (gauge)
- token_expiry_days (gauge)
```

### Logging Structure

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "service": "igautopost",
  "trace_id": "abc123",
  "user_id": "user-456",
  "event": "post_published",
  "post_id": "post-789",
  "duration_ms": 3450,
  "tags": ["instagram", "success"]
}
```

### Health Checks

```java
@GetMapping("/health")
public HealthStatus health() {
    return HealthStatus.builder()
        .status("UP")
        .openaiReachable(checkOpenAI())
        .instagramReachable(checkInstagram())
        .s3Reachable(checkS3())
        .queueReachable(checkQueue())
        .build();
}
```

## Error Handling

### Retry Strategy

```java
// Exponential backoff
RetryPolicy retryPolicy = RetryPolicy.builder()
    .maxAttempts(3)
    .delay(Duration.ofSeconds(1))
    .maxDelay(Duration.ofSeconds(30))
    .backoffMultiplier(2.0)
    .retryOn(IOException.class, TimeoutException.class)
    .build();
```

### Dead Letter Queue

- Failed posts move to DLQ after max retries
- Monitor DLQ depth
- Manual review and reprocessing
- Alert on DLQ messages

## Database Schema

### Posts Table

```sql
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    image_url TEXT NOT NULL,
    local_path TEXT,
    caption TEXT NOT NULL,
    context TEXT,
    instagram_media_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    scheduled_at TIMESTAMP,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0
);

CREATE INDEX idx_posts_user_status ON posts(user_id, status);
CREATE INDEX idx_posts_scheduled ON posts(scheduled_at) WHERE status = 'SCHEDULED';
```

### Token Management Table

```sql
CREATE TABLE access_tokens (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    access_token TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_tokens_expiry ON access_tokens(expires_at);
```

## Scaling Considerations

### Horizontal Scaling

- Run multiple instances
- Use message queue for work distribution
- Implement distributed locking for scheduled tasks

### Rate Limiting

```java
// Instagram: 200 calls/hour
RateLimiter instagramLimiter = RateLimiter.create(3.33); // per minute

// OpenAI: Based on tier
RateLimiter openaiLimiter = RateLimiter.create(50.0); // per minute
```

### Caching

```java
// Cache OpenAI responses for similar contexts
@Cacheable(value = "captions", key = "#context")
public String generateCaption(String context) {
    // ...
}
```

## Deployment Process

### 1. Build Docker Image

```dockerfile
FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/igautopost-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Push to ECR

```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com
docker build -t igautopost .
docker tag igautopost:latest <account>.dkr.ecr.us-east-1.amazonaws.com/igautopost:latest
docker push <account>.dkr.ecr.us-east-1.amazonaws.com/igautopost:latest
```

### 3. Deploy to ECS

```bash
aws ecs update-service --cluster igautopost-cluster --service igautopost-service --force-new-deployment
```

## Backup & Recovery

### Backup Strategy

- **Database**: Daily automated backups with 30-day retention
- **S3 Images**: Versioning enabled, lifecycle policy to Glacier
- **Configuration**: Store in version control

### Disaster Recovery

- RPO (Recovery Point Objective): 1 hour
- RTO (Recovery Time Objective): 30 minutes
- Multi-region deployment for critical applications

## Cost Optimization

### Estimated Monthly Costs

```
AWS ECS (2 tasks, 0.5 vCPU, 1GB RAM): $30
RDS PostgreSQL (db.t3.micro): $15
S3 Storage (100GB): $2.30
Data Transfer: $10
OpenAI API (1000 requests/day @ gpt-4): $60
Total: ~$117/month
```

### Optimization Tips

- Use spot instances for non-critical tasks
- Implement S3 lifecycle policies
- Use reserved instances for steady workload
- Cache API responses
- Use gpt-3.5-turbo for simpler captions

## Compliance

### GDPR Considerations

- Obtain user consent for data processing
- Provide data export functionality
- Implement data deletion
- Document data retention policies

### Instagram Compliance

- Follow Instagram Platform Terms
- Respect rate limits
- Don't store user data longer than necessary
- Implement proper error handling

## Maintenance

### Regular Tasks

- [ ] Weekly: Review error logs and DLQ
- [ ] Weekly: Check API rate limit usage
- [ ] Monthly: Rotate access tokens
- [ ] Monthly: Review and optimize costs
- [ ] Quarterly: Update dependencies
- [ ] Quarterly: Security audit

### Alerts

```yaml
alerts:
  - name: high_error_rate
    condition: error_rate > 5%
    severity: critical
  
  - name: token_expiring
    condition: token_expiry < 7 days
    severity: warning
  
  - name: queue_depth_high
    condition: queue_depth > 1000
    severity: warning
  
  - name: api_rate_limit
    condition: rate_limit_usage > 80%
    severity: warning
```

## Support & Troubleshooting

### Common Issues

1. **Token Expired**: Refresh Instagram access token
2. **Rate Limited**: Implement exponential backoff
3. **Image Upload Failed**: Check S3 permissions and network
4. **Caption Generation Failed**: Verify OpenAI API key and quota

### Debug Mode

```bash
LOG_LEVEL=DEBUG java -jar igautopost.jar
```

### Useful Commands

```bash
# Check service health
curl https://api.example.com/health

# View logs
aws logs tail /aws/ecs/igautopost --follow

# Check queue depth
aws sqs get-queue-attributes --queue-url <url> --attribute-names ApproximateNumberOfMessages
```
