package com.piisw.tod.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Encja reprezentująca wiadomość w systemie poczty wewnętrznej.
 * Wiadomość ma nadawcę, odbiorcę, treść i może być powiązana z ogłoszeniem.
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Data wysłania wiadomości.
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    /**
     * Czy wiadomość została przeczytana.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Nadawca wiadomości.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Odbiorca wiadomości.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * Ogłoszenie, do którego dotyczy ta wiadomość (opcjonalnie).
     * Pozwala powiązać conversację z konkretnym ogłoszeniem.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_ad_id")
    private Ad relatedAd;

    /**
     * Wiadomość, na którą ta wiadomość jest odpowiedzią (opcjonalnie).
     * Pozwala budować wątki konwersacji.
     * Self-referencing relacja (wiele do jednego).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;

    /**
     * Odpowiedzi na tę wiadomość.
     */
    @OneToMany(mappedBy = "parentMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<Message> replies = new java.util.ArrayList<>();
}

