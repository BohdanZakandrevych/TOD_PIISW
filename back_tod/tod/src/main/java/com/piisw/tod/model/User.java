package com.piisw.tod.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encja reprezentująca użytkownika w systemie.
 * Użytkownik może być jednocześnie kupującym i sprzedającym.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Zahashowane hasło

    /**
     * Lista danych kontaktowych użytkownika.
     * Jeden użytkownik może mieć wiele danych kontaktowych (np. telefon, email dodatkowy, itp.)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContactInfo> contactInfos = new ArrayList<>();

    /**
     * Ogłoszenia opublikowane przez użytkownika.
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Ad> adsCreated = new ArrayList<>();

    /**
     * Wiadomości wysłane przez użytkownika.
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Message> messagesSent = new ArrayList<>();

    /**
     * Wiadomości otrzymane przez użytkownika.
     */
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Message> messagesReceived = new ArrayList<>();
}

