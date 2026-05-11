package com.piisw.tod.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Encja reprezentująca ogłoszenie w systemie.
 * Ogłoszenie ma tytuł, opis, galerię zdjęć, status oraz różne relacje.
 */
@Entity
@Table(name = "ads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Status ogłoszenia (DRAFT, PUBLISHED, ARCHIVED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AdStatus status = AdStatus.DRAFT;

    /**
     * Data utworzenia ogłoszenia.
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Data ostatniej modyfikacji.
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Tajny token do podglądu ogłoszenia w wersji roboczej (DRAFT).
     * UUID generowane dla wersji roboczych.
     */
    @Column(unique = true)
    private String secretPreviewToken;

    /**
     * Galeria zdjęć - lista URLi do zdjęć.
     * Przechowywana jako ElementCollection.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ad_gallery", joinColumns = @JoinColumn(name = "ad_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    /**
     * Autor ogłoszenia (użytkownik, który je utworzył).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Tagi przypisane do ogłoszenia.
     * Relacja wiele do wielu - jedno ogłoszenie może mieć wiele tagów.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ad_tags",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    /**
     * Dane kontaktowe przypisane do ogłoszenia.
     * Ogłoszeniodawca wybiera, które dane kontaktowe będą widoczne dla tego ogłoszenia.
     * Relacja wiele do wielu - jedno ogłoszenie może mieć wiele danych kontaktowych.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ad_contact_info",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_info_id")
    )
    @Builder.Default
    private Set<ContactInfo> contactInfos = new HashSet<>();

    /**
     * Wiadomości powiązane z tym ogłoszeniem.
     * Pozwala śledzić rozmowy dotyczące konkretnego ogłoszenia.
     */
    @OneToMany(mappedBy = "relatedAd", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
}

