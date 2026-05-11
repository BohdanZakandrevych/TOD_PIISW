package com.piisw.tod.repository;

import com.piisw.tod.model.Ad;
import com.piisw.tod.model.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repozytorium dla encji Ad (Ogłoszenie).
 */
@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    /**
     * Wyszukiwanie ogłoszeń po statusie.
     */
    List<Ad> findByStatus(AdStatus status);

    /**
     * Wyszukiwanie ogłoszeń po autorze (user_id).
     */
    List<Ad> findByAuthorId(Long authorId);

    /**
     * Wyszukiwanie ogłoszeń po autorze i statusie.
     */
    List<Ad> findByAuthorIdAndStatus(Long authorId, AdStatus status);

    /**
     * Wyszukiwanie ofłoszenia po tajnym tokenie (do podglądu wersji roboczych).
     */
    Optional<Ad> findBySecretPreviewToken(String token);

    /**
     * Wyszukiwanie ogłoszeń opublikowanych.
     */
    List<Ad> findByStatusOrderByCreatedAtDesc(AdStatus status);

    /**
     * Wyszukiwanie ogłoszeń po tytule (LIKE search).
     */
    List<Ad> findByTitleContainingIgnoreCase(String title);

    /**
     * Wyszukiwanie ogłoszeń po descripcji (LIKE search).
     */
    List<Ad> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Wyszukivanie ogłoszeń po tytule lub opisie.
     */
    @Query("""
        SELECT a 
        FROM Ad a 
        WHERE a.status = :status 
        AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
             OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY a.createdAt DESC
        """)
    List<Ad> searchPublishedAds(@Param("searchTerm") String searchTerm, @Param("status") AdStatus status);

    /**
     * Wyszukiwanie ogłoszeń po tagach.
     */
    @Query("""
        SELECT DISTINCT a 
        FROM Ad a 
        JOIN a.tags t 
        WHERE t.id = :tagId AND a.status = :status
        ORDER BY a.createdAt DESC
        """)
    Page<Ad> findByTagIdAndStatus(@Param("tagId") Long tagId, @Param("status") AdStatus status, Pageable pageable);

    /**
     * Wyszukiwanie ogłoszeń utworzonych po danej dacie.
     */
    List<Ad> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    /**
     * Liczba ogłoszeń w danym statusie.
     */
    long countByStatus(AdStatus status);

    /**
     * Liczba ogłoszeń użytkownika.
     */
    long countByAuthorId(Long authorId);
}

