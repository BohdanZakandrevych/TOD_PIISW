package com.piisw.tod.dto;

import java.time.LocalDateTime;

public record MessageDto(
        Long id,
        String content,
        LocalDateTime sentAt,
        Boolean isRead,
        Long senderId,
        String senderEmail,
        Long receiverId,
        String receiverEmail,
        Long relatedAdId,
        Long parentMessageId
) {
}
