package com.piisw.tod.repository;

import com.piisw.tod.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repozytorium dla encji Tag.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Wyszukiwanie tagu po nazwie (case-insensitive).
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * Wyszukiwanie tagów, których nazwa zaczyna się od danego prefiksu.
     * Rezultaty są sortowane po ilości przypisanych ogłoszeń (malejąco - od najczęściej używanych).
     * 
     * @param prefix prefiks nazwy tagu
     * @return lista tagów posortowana po częstości użycia
     */
    @Query("""
        SELECT t 
        FROM Tag t 
        LEFT JOIN t.ads a
        WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%'))
        GROUP BY t.id
        ORDER BY COUNT(DISTINCT a.id) DESC
        """)
    List<Tag> findByNameStartingWithOrderByUsageCount(@Param("prefix") String prefix);

    /**
     * Wyszukiwanie tagów po nazwie zawierającej daną frazę (fuzzy search).
     * Sortowanie po ilości przypisanych ogłoszeń.
     * 
     * @param phrase fraza do wyszukania
     * @return lista tagów
     */
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

