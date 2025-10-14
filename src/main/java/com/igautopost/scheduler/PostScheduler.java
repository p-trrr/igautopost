package com.igautopost.scheduler;

import com.igautopost.config.AppConfig;
import com.igautopost.service.PostingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for automated Instagram posting.
 * Supports scheduling posts at regular intervals.
 */
public class PostScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(PostScheduler.class);
    
    private final AppConfig config;
    private final PostingService postingService;
    private final ScheduledExecutorService scheduler;
    private PostTask postTask;
    
    public PostScheduler(AppConfig config, PostingService postingService) {
        this.config = config;
        this.postingService = postingService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Starts the scheduler with a custom post task.
     * 
     * @param postTask The task to execute on schedule
     */
    public void start(PostTask postTask) {
        this.postTask = postTask;
        
        int intervalMinutes = config.getSchedulingIntervalMinutes();
        logger.info("Starting scheduler with interval: {} minutes", intervalMinutes);
        
        scheduler.scheduleAtFixedRate(
            this::executeTask,
            0,
            intervalMinutes,
            TimeUnit.MINUTES
        );
        
        logger.info("Scheduler started successfully");
    }
    
    /**
     * Stops the scheduler.
     */
    public void stop() {
        logger.info("Stopping scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Scheduler stopped");
    }
    
    private void executeTask() {
        try {
            logger.info("Executing scheduled post task");
            if (postTask != null) {
                postTask.execute(postingService);
            } else {
                logger.warn("No post task configured");
            }
        } catch (Exception e) {
            logger.error("Error executing scheduled task", e);
        }
    }
    
    /**
     * Interface for custom post tasks.
     */
    public interface PostTask {
        void execute(PostingService postingService) throws Exception;
    }
}
