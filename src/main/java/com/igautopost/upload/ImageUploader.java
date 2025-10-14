package com.igautopost.upload;

import java.io.File;
import java.io.IOException;

/**
 * Interface for image uploading services.
 */
public interface ImageUploader {
    
    /**
     * Uploads an image file and returns the public URL.
     * 
     * @param imageFile The image file to upload
     * @return The public URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(File imageFile) throws IOException;
    
    /**
     * Uploads an image from a local path and returns the public URL.
     * 
     * @param localPath The local path to the image file
     * @return The public URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(String localPath) throws IOException;
}
