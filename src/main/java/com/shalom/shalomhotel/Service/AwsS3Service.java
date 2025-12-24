package com.shalom.shalomhotel.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AwsS3Service {
    private static final Logger log = LoggerFactory.getLogger(AwsS3Service.class);

    private final String bucketName = "shalom-hotels-images";

    @Value("${aws.s3.access}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secrete}")
    private String awsS3SecretKey;

    public String uploadFile(MultipartFile photo){
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }

        try{
            // Generate unique filename to avoid conflicts
            String originalFileName = photo.getOriginalFilename();
            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Create AWS credentials using the access and secret key
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsS3AccessKey, awsS3SecretKey);

            // Create an S3 client with config credentials and region
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.US_EAST_1)
                    .build();

            // Get input stream from photo
            InputStream inputStream = photo.getInputStream();

            // Set metadata for the object
            ObjectMetadata metadata = new ObjectMetadata();

            // Use the actual content type from the file
            String contentType = photo.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg"; // Default if not provided
            }
            metadata.setContentType(contentType);

            // CRITICAL FIX: Set content length to avoid buffering in memory
            metadata.setContentLength(photo.getSize());

            // Optional: Add additional metadata
            metadata.addUserMetadata("original-filename", originalFileName != null ? originalFileName : "unknown");

            // Create a put request to upload the image to S3
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    "room-images/" + uniqueFileName, // Organize in folder
                    inputStream,
                    metadata
            );

            // Upload to S3
            s3Client.putObject(putObjectRequest);

            // Generate and return the URL
            String imageUrl = "https://" + bucketName + ".s3.us-east-1.amazonaws.com/room-images/" + uniqueFileName;

            log.info("Image uploaded successfully to S3: {}", imageUrl);
            return imageUrl;

        } catch (IOException e){
            log.error("Error uploading image to S3", e);
            throw new RuntimeException("Unable to upload image to S3 bucket: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in S3 upload", e);
            throw new RuntimeException("Error uploading image: " + e.getMessage());
        }
    }
}