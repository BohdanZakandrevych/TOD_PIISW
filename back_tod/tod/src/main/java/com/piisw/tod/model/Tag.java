package com.piisw.tod.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Encja reprezentująca tag (słowo kluczowe).
 * Tag może być przypisany do wielu ogłoszeń.
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Ogłoszenia, do których przypisany jest ten tag.
     * Relacja wiele do wielu - jeden tag może być przypisany do wielu ogłoszeń.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Ad> ads = new HashSet<>();
}

