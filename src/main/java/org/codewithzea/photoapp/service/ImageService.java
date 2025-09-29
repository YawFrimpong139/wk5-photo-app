package org.codewithzea.photoapp.service;



import org.codewithzea.photoapp.config.S3Config;
import org.codewithzea.photoapp.dto.ImageUploadRequest;
import org.codewithzea.photoapp.model.Image;
import org.codewithzea.photoapp.repository.ImageRepository;
import org.codewithzea.photoapp.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Config s3Config;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ImageRepository imageRepository;

    public void uploadPhoto(MultipartFile file, ImageUploadRequest request) throws Exception {
        String objectKey = "photos/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Upload file
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(s3Config.getBucket())
                        .key(objectKey)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        // Generate presigned URL
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(3))
                .getObjectRequest(getObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        // Save metadata
        Image photo = Image.builder()
                .objectKey(objectKey)
                .description(request.getDescription())
                .presignedUrl(presignedUrl)
                .build();

        imageRepository.save(photo);
    }


    public void deletePhoto(Long id) throws Exception {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Delete from S3
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(image.getObjectKey())
                .build();

        s3Client.deleteObject(deleteObjectRequest);

        // Delete from database
        imageRepository.delete(image);
    }
}
