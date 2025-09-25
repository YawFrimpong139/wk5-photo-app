package org.codewithzea.photoapp.controller;

import org.codewithzea.photoapp.model.Image;
import org.codewithzea.photoapp.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class GalleryController {

    private final ImageService imageService;

    public GalleryController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("images", imageService.getAllImages());
        model.addAttribute("imageCount", imageService.getImageCount());
        return "index";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        if (!model.containsAttribute("success")) {
            model.addAttribute("success", null);
        }
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", null);
        }
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            RedirectAttributes redirectAttributes) {

        try {
            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/upload";
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Please upload a valid image file (JPEG, PNG, GIF, etc.)");
                return "redirect:/upload";
            }

            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                redirectAttributes.addFlashAttribute("error", "File size must be less than 10MB");
                return "redirect:/upload";
            }

            Image savedImage = imageService.saveImage(file, description);
            redirectAttributes.addFlashAttribute("success",
                    "Image '" + savedImage.getOriginalFilename() + "' uploaded successfully!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to upload image: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/upload";
    }

    @PostMapping("/delete/{id}")
    public String deleteImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = imageService.deleteImage(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Image deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete image");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting image: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/image/{id}")
    public String viewImage(@PathVariable Long id, Model model) {
        try {
            Image image = imageService.getImageById(id);
            model.addAttribute("image", image);
            return "image-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }
    }

    /**
     * 👇 New endpoint to stream raw image bytes
     */
    @GetMapping("/images/{id}/data")
    public ResponseEntity<byte[]> getImageData(@PathVariable Long id) {
        try {
            Image image = imageService.getImageById(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .body(image.getData());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
