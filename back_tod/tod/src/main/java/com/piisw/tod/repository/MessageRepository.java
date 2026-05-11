package com.piisw.tod.repository;

import com.piisw.tod.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium dla encji Message (Wiadomość).
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Wyszukiwanie wiadomości wysłanych przez danego użytkownika.
     */
    List<Message> findBySenderId(Long senderId);

    /**
     * Wyszukiwanie wiadomości otrzymanych przez danego użytkownika.
     */
    List<Message> findByReceiverId(Long receiverId);

    /**
     * Wyszukiwanie konwersacji między dwoma użytkownikami.
     * Zwraca wiadomości wysłane między senderId a receiverId (w obu kierunkach).
     */
    @Query("""
        SELECT m 
        FROM Message m 
        WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) 
           OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)
        ORDER BY m.sentAt ASC
        """)
    List<Message> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * Wyszukiwanie konwersacji dotyczącej konkretnego ogłoszenia.
     */
    @Query("""
        SELECT m 
        FROM Message m 
        WHERE m.relatedAd.id = :adId 
           AND ((m.sender.id = :user1Id AND m.receiver.id = :user2Id) 
                OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id))
        ORDER BY m.sentAt ASC
        """)
    List<Message> findConversationAboutAd(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("adId") Long adId);

    /**
     * Wyszukiwanie wiadomości dotyczących jednego ogłoszenia.
     */
    List<Message> findByRelatedAdId(Long adId);

    /**
     * Wyszukiwanie wiadomości, które są odpowiedziami na inną wiadomość.
     */
    List<Message> findByParentMessageId(Long parentMessageId);

    /**
     * Wyszukiwanie nieprzeczytanych wiadomości dla konkretnego użytkownika.
     */
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    /**
     * Liczba nieprzeczytanych wiadomości.
     */
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    /**
     * Wyszukiwanie wiadomości wysłanych po danej dacie.
     */
    List<Message> findBySentAtAfterOrderBySentAtDesc(LocalDateTime date);

    /**
     * Stronicowana lista wiadomości otrzymanych przez użytkownika.
     */
    Page<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId, Pageable pageable);

    /**
     * Stronicowana lista wiadomości wysłanych przez użytkownika.
     */
    Page<Message> findBySenderIdOrderBySentAtDesc(Long senderId, Pageable pageable);
}

