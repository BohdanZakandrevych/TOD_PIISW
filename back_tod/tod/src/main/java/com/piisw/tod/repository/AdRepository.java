package com.piisw.tod.repository;

import com.piisw.tod.model.Ad;
import com.piisw.tod.model.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long>, JpaSpecificationExecutor<Ad> {

    @Override
    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    Page<Ad> findAll(Specification<Ad> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    List<Ad> findByStatus(AdStatus status);

    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    List<Ad> findByAuthorId(Long authorId);

    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    List<Ad> findByAuthorIdAndStatus(Long authorId, AdStatus status);

    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    Optional<Ad> findBySecretPreviewToken(String token);

    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    List<Ad> findByStatusOrderByCreatedAtDesc(AdStatus status);

    List<Ad> findByTitleContainingIgnoreCase(String title);

    List<Ad> findByDescriptionContainingIgnoreCase(String description);

    @Query("""
        SELECT a 
        FROM Ad a 
        WHERE a.status = :status 
        AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
              OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY a.createdAt DESC
        """)
    @EntityGraph(attributePaths = {"tags", "contactInfos", "author", "imageUrls"})
    List<Ad> searchPublishedAds(@Param("searchTerm") String searchTerm, @Param("status") AdStatus status);

    @Query("""
        SELECT DISTINCT a 
        FROM Ad a 
        JOIN a.tags t 
        WHERE t.id = :tagId AND a.status = :status
        ORDER BY a.createdAt DESC
        """)
    Page<Ad> findByTagIdAndStatus(@Param("tagId") Long tagId, @Param("status") AdStatus status, Pageable pageable);

    List<Ad> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    long countByStatus(AdStatus status);

    long countByAuthorId(Long authorId);
}
