package com.igautopost.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a post to be published on Instagram.
 */
public class InstagramPost {
    
    private String imageUrl;
    private String caption;
    private String mediaId;
    private PostStatus status;
    
    public InstagramPost() {
        this.status = PostStatus.PENDING;
    }
    
    public InstagramPost(String imageUrl, String caption) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.status = PostStatus.PENDING;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getMediaId() {
        return mediaId;
    }
    
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
    
    public PostStatus getStatus() {
        return status;
    }
    
    public void setStatus(PostStatus status) {
        this.status = status;
    }
    
    public enum PostStatus {
        PENDING,
        CONTAINER_CREATED,
        PUBLISHED,
        FAILED
    }
    
    @Override
    public String toString() {
        return "InstagramPost{" +
                "imageUrl='" + imageUrl + '\'' +
                ", caption='" + caption + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", status=" + status +
                '}';
    }
}
