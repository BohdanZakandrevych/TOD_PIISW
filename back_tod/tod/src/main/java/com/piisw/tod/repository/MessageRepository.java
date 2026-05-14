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

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderId(Long senderId);

    List<Message> findByReceiverId(Long receiverId);

    @Query("""
        SELECT m 
        FROM Message m 
        WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) 
            OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)
        ORDER BY m.sentAt ASC
        """)
    List<Message> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("""
        SELECT m 
        FROM Message m 
        WHERE m.relatedAd.id = :adId 
            AND ((m.sender.id = :user1Id AND m.receiver.id = :user2Id) 
                 OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id))
        ORDER BY m.sentAt ASC
        """)
    List<Message> findConversationAboutAd(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("adId") Long adId);

    List<Message> findByRelatedAdId(Long adId);

    List<Message> findByParentMessageId(Long parentMessageId);

    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    List<Message> findBySentAtAfterOrderBySentAtDesc(LocalDateTime date);

    Page<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId, Pageable pageable);

    Page<Message> findBySenderIdOrderBySentAtDesc(Long senderId, Pageable pageable);
}
