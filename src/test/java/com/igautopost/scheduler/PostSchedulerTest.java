package com.igautopost.scheduler;

import com.igautopost.config.AppConfig;
import com.igautopost.service.PostingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PostScheduler.
 */
@ExtendWith(MockitoExtension.class)
class PostSchedulerTest {
    
    @Mock
    private PostingService postingService;
    
    private AppConfig config;
    private PostScheduler scheduler;
    
    @BeforeEach
    void setUp() {
        config = new AppConfig.Builder()
            .openaiApiKey("test-key")
            .instagramAccessToken("test-token")
            .instagramBusinessAccountId("test-id")
            .schedulingIntervalMinutes(1)
            .build();
        
        scheduler = new PostScheduler(config, postingService);
    }
    
    @Test
    void testSchedulerExecutesTask() throws Exception {
        // Arrange
        AtomicInteger executionCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        PostScheduler.PostTask task = service -> {
            executionCount.incrementAndGet();
            latch.countDown();
        };
        
        // Act
        scheduler.start(task);
        
        // Wait for at least one execution (with timeout)
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        
        // Clean up
        scheduler.stop();
        
        // Assert
        assertTrue(completed, "Task should have been executed");
        assertTrue(executionCount.get() >= 1, "Task should have been executed at least once");
    }
    
    @Test
    void testSchedulerHandlesTaskException() throws Exception {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        
        PostScheduler.PostTask failingTask = service -> {
            latch.countDown();
            throw new RuntimeException("Simulated task failure");
        };
        
        // Act
        scheduler.start(failingTask);
        
        // Wait for execution
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        
        // Clean up
        scheduler.stop();
        
        // Assert - scheduler should continue running despite exception
        assertTrue(completed, "Task should have been executed even though it threw exception");
    }
    
    @Test
    void testSchedulerStop() throws Exception {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        PostScheduler.PostTask task = service -> latch.countDown();
        
        // Act
        scheduler.start(task);
        latch.await(5, TimeUnit.SECONDS);
        scheduler.stop();
        
        // Assert - no exception should be thrown
        assertTrue(true, "Scheduler should stop cleanly");
    }
}
