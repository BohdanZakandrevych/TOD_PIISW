package com.piisw.tod.repository;

import com.piisw.tod.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("""
        SELECT t 
        FROM Tag t 
        LEFT JOIN t.ads a
        WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%'))
        GROUP BY t.id
        ORDER BY COUNT(DISTINCT a.id) DESC
        """)
    List<Tag> findByNameStartingWithOrderByUsageCount(@Param("prefix") String prefix);

    @Query("""
        SELECT t 
        FROM Tag t 
        LEFT JOIN t.ads a
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :phrase, '%'))
        GROUP BY t.id
        ORDER BY COUNT(DISTINCT a.id) DESC
        """)
    List<Tag> findByNameContainingOrderByUsageCount(@Param("phrase") String phrase);
}
