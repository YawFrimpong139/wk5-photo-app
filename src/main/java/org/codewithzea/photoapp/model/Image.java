package org.codewithzea.photoapp.model;

// Image.java

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String filename;

    @NotBlank
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private Long size;

    @Column(name = "content_type")
    private String contentType;

    // 👇 Store image bytes directly
    @Lob
    @Column(name = "data", columnDefinition = "BYTEA") // for PostgreSQL
    private byte[] data;

    @PrePersist
    protected void onCreate() {
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
    }

}