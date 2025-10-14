# Mock Testing Strategy

## Overview

This project uses a comprehensive mock testing strategy to ensure code quality without making real API calls. This approach provides fast, reliable tests that don't incur API costs and can run in CI/CD pipelines without external dependencies.

## Testing Frameworks

### 1. OkHttp MockWebServer

**Purpose**: Mock HTTP API calls to OpenAI and Instagram Graph API

**How it works**:
- Creates a local HTTP server that can be programmed to return specific responses
- Allows testing of various scenarios (success, errors, edge cases)
- Captures requests for verification

**Example**:
```java
@BeforeEach
void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    
    // Configure service to use mock server URL
    AppConfig config = new AppConfig.Builder()
        .openaiApiUrl(mockWebServer.url("/").toString())
        .build();
}

@Test
void testApiCall() throws Exception {
    // Arrange: Queue a mock response
    mockWebServer.enqueue(new MockResponse()
        .setBody("{\"id\": \"test-123\"}")
        .addHeader("Content-Type", "application/json"));
    
    // Act: Make API call
    String result = service.callApi();
    
    // Assert: Verify response
    assertEquals("test-123", result);
    
    // Verify request was made correctly
    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("POST", request.getMethod());
}
```

### 2. Mockito

**Purpose**: Mock service dependencies and verify interactions

**How it works**:
- Creates mock objects that simulate real dependencies
- Allows stubbing of method behavior
- Enables verification of method calls

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class PostingServiceTest {
    @Mock
    private OpenAIService openAIService;
    
    @Mock
    private InstagramService instagramService;
    
    @Test
    void testPostingWorkflow() throws IOException {
        // Arrange: Stub mock behavior
        when(openAIService.generateCaption(any()))
            .thenReturn("Test caption");
        
        // Act: Execute workflow
        service.createPost("image.jpg", "context");
        
        // Assert: Verify interactions
        verify(openAIService).generateCaption("context");
        verify(instagramService).publishPost(any());
    }
}
```

### 3. AWS SDK Mocking

**Purpose**: Mock AWS S3 operations

**How it works**:
- Injects mocked S3Client instead of creating real connection
- Simulates S3 operations without actual uploads
- Tests error scenarios (access denied, bucket not found, etc.)

**Example**:
```java
@Mock
private S3Client s3Client;

@Test
void testImageUpload() throws IOException {
    // Arrange
    when(s3Client.putObject(any(PutObjectRequest.class), any()))
        .thenReturn(PutObjectResponse.builder().build());
    
    S3ImageUploader uploader = new S3ImageUploader(config, s3Client);
    
    // Act
    String url = uploader.uploadImage(testFile);
    
    // Assert
    assertNotNull(url);
    assertTrue(url.contains("s3"));
    verify(s3Client).putObject(any(), any());
}
```

## Test Coverage

### Service Tests

#### OpenAIServiceTest
- ✅ Successful caption generation
- ✅ API authentication errors (401)
- ✅ Empty response handling
- ✅ Request structure validation
- ✅ Response parsing

#### InstagramServiceTest
- ✅ Complete publishing workflow (container + publish)
- ✅ Container creation failures
- ✅ Publishing failures
- ✅ Request parameter validation
- ✅ Error response handling

#### PostingServiceTest
- ✅ End-to-end workflow from local file
- ✅ End-to-end workflow from URL
- ✅ Upload failure handling
- ✅ Caption generation failure handling
- ✅ Publishing failure handling
- ✅ Service interaction verification

### Upload Tests

#### S3ImageUploaderTest
- ✅ Successful image upload
- ✅ File not found error
- ✅ S3 access errors
- ✅ Content type detection
- ✅ URL generation

### Scheduler Tests

#### PostSchedulerTest
- ✅ Task execution at intervals
- ✅ Exception handling in tasks
- ✅ Graceful shutdown
- ✅ Multiple task executions

## Benefits of Mock Testing

### 1. Speed
- Tests run in milliseconds instead of seconds
- No network latency
- No waiting for external services

### 2. Reliability
- Tests don't fail due to network issues
- No flaky tests from API rate limits
- Consistent results every time

### 3. Cost
- No API charges during testing
- Can run unlimited test iterations
- Safe for CI/CD pipelines

### 4. Isolation
- Tests focus on code logic, not external services
- Can test error scenarios that are hard to reproduce
- No risk of affecting production data

### 5. Control
- Test any scenario (success, errors, timeouts)
- Simulate edge cases
- Test error recovery

## Running Tests

### Run all tests:
```bash
mvn test
```

### Run specific test class:
```bash
mvn test -Dtest=OpenAIServiceTest
```

### Run with coverage (if configured):
```bash
mvn clean test jacoco:report
```

### Run tests with detailed output:
```bash
mvn test -X
```

## Test Organization

```
src/test/java/com/igautopost/
├── service/
│   ├── OpenAIServiceTest.java         # OpenAI API mocking
│   ├── InstagramServiceTest.java      # Instagram API mocking
│   └── PostingServiceTest.java        # Service orchestration
├── upload/
│   └── S3ImageUploaderTest.java       # AWS S3 mocking
└── scheduler/
    └── PostSchedulerTest.java         # Scheduler testing
```

## Adding New Tests

### For HTTP API Clients:

1. Use MockWebServer
2. Queue appropriate responses
3. Verify request structure
4. Test error scenarios

### For Service Orchestration:

1. Use Mockito to mock dependencies
2. Stub behavior with `when().thenReturn()`
3. Verify interactions with `verify()`
4. Test error propagation

### For External Clients:

1. Create mock implementation or inject mocked client
2. Simulate success and error scenarios
3. Verify method calls
4. Test exception handling

## Best Practices

1. **Test One Thing**: Each test should verify one specific behavior
2. **Clear Names**: Test names should describe what they test
3. **Arrange-Act-Assert**: Follow AAA pattern for clarity
4. **Independent Tests**: Tests should not depend on each other
5. **Mock Only External Dependencies**: Keep business logic real
6. **Test Error Paths**: Error scenarios are as important as success
7. **Clean Up**: Close resources in @AfterEach
8. **Readable Assertions**: Use descriptive assertion messages

## Continuous Integration

Tests are designed to run in CI/CD pipelines:
- No external dependencies required
- Fast execution (< 30 seconds)
- Deterministic results
- No secrets or credentials needed

## Future Enhancements

- [ ] Add integration tests with TestContainers
- [ ] Add performance tests
- [ ] Add contract tests for API interactions
- [ ] Add mutation testing
- [ ] Increase code coverage to >90%
- [ ] Add property-based testing for edge cases
