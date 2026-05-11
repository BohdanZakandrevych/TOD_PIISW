package com.piisw.tod.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Encja reprezentująca dane kontaktowe użytkownika.
 * Użytkownik może mieć wiele danych kontaktowych (np. telefon, email, adres, itp.)
 */
@Entity
@Table(name = "contact_infos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // np. "PHONE", "EMAIL", "ADDRESS"

    @Column(nullable = false)
    private String value; // wartość (numer telefonu, email, adres itp.)

    /**
     * Użytkownik, do którego należą dane kontaktowe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Ogłoszenia, do których przypisane są te dane kontaktowe.
     * Ogłoszeniodawca wybiera, które dane kontaktowe będą widoczne dla konkretnego ogłoszenia.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ad_contact_info",
            joinColumns = @JoinColumn(name = "contact_info_id"),
            inverseJoinColumns = @JoinColumn(name = "ad_id")
    )
    @Builder.Default
    private Set<Ad> ads = new HashSet<>();
}

