package org.codewithzea.photoapp.controller;


import org.codewithzea.photoapp.dto.ImageUploadRequest;
import org.codewithzea.photoapp.model.Image;
import org.codewithzea.photoapp.repository.ImageRepository;
import org.codewithzea.photoapp.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @GetMapping("/")
    public String gallery(Model model) {
        List<Image> images = imageRepository.findAll();
        model.addAttribute("images", images);
        model.addAttribute("imageCount", images.size());
        String lastUpdateTime = images.stream()
                .map(Image::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .map(date -> "Last updated " + formatLastUpdate(date))
                .orElse("No images yet");
        model.addAttribute("lastUpdate", lastUpdateTime);
        return "gallery";
    }

    private String formatLastUpdate(LocalDateTime date) {
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(date, now);
        if (days == 0) {
            return "today";
        } else if (days == 1) {
            return "yesterday";
        } else {
            return days + " days ago";
        }
    }

    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              @RequestParam("description") String description) throws Exception {
        ImageUploadRequest request = new ImageUploadRequest();
        request.setDescription(description);
        imageService.uploadPhoto(file, request);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deletePhoto(@PathVariable Long id) throws Exception {
        imageService.deletePhoto(id);
        return "redirect:/";
    }
}

