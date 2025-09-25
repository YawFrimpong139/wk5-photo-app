package org.codewithzea.photoapp.repository;


import org.codewithzea.photoapp.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByOrderByUploadDateDesc();

    @Query("SELECT i FROM Image i ORDER BY i.uploadDate DESC")
    List<Image> findRecentImages();
}