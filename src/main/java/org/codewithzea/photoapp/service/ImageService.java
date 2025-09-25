package org.codewithzea.photoapp.service;

import lombok.extern.slf4j.Slf4j;
import org.codewithzea.photoapp.model.Image;
import org.codewithzea.photoapp.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image saveImage(MultipartFile file, String description) throws IOException {
        try {
            log.info("Starting image save process...");

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty or null");
            }

            // Debug logging
            log.info("File details - Name: {}, Size: {}, ContentType: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Invalid file type. Please upload an image file.");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.contains("..")) {
                throw new IllegalArgumentException("Invalid file name");
            }

            // Create image entity
            Image image = new Image();
            image.setOriginalFilename(originalFilename);
            image.setFilename(generateUniqueFilename(originalFilename));
            image.setDescription(description != null ? description : "");
            image.setSize(file.getSize());
            image.setContentType(contentType);

            // DEBUG: Log the data type and size before setting
            byte[] fileBytes = file.getBytes();
            log.info("File bytes length: {}, Type: {}",
                    fileBytes.length, fileBytes.getClass().getName());

            image.setData(fileBytes); // This should be byte[]

            log.info("Image entity prepared - Data field type: {}",
                    image.getData() != null ? image.getData().getClass().getName() : "null");

            // Save and return
            Image savedImage = imageRepository.save(image);
            log.info("Image saved successfully with ID: {}", savedImage.getId());

            return savedImage;

        } catch (Exception e) {
            log.error("Error saving image: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return "img_" + timestamp + extension;
    }

//    public Image saveImage(MultipartFile file, String description) throws IOException {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("File is empty or null");
//        }
//
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new IllegalArgumentException("Invalid file type. Please upload an image file.");
//        }
//
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename == null || originalFilename.contains("..")) {
//            throw new IllegalArgumentException("Invalid file name");
//        }
//
//        // Create and save image entity with file bytes
//        Image image = new Image();
//        image.setOriginalFilename(originalFilename);
//        image.setFilename(originalFilename); // optional, you can still generate UUID if you want
//        image.setDescription(description != null ? description : "");
//        image.setSize(file.getSize());
//        image.setContentType(contentType);
//        image.setUploadDate(LocalDateTime.now());
//        image.setData(file.getBytes()); // 👈 store image bytes directly in DB
//
//        return imageRepository.save(image);
//    }

    public List<Image> getAllImages() {
        return imageRepository.findAllByOrderByUploadDateDesc();
    }

    public boolean deleteImage(Long id) {
        try {
            if (!imageRepository.existsById(id)) {
                throw new IllegalArgumentException("Image not found with id: " + id);
            }
            imageRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting image: " + e.getMessage());
            return false;
        }
    }

    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + id));
    }

    public long getImageCount() {
        return imageRepository.count();
    }
}
