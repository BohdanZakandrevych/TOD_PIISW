package com.piisw.tod.dto;

import com.piisw.tod.model.AdStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdResponseDto(
        Long id,
        String title,
        String description,
        AdStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long authorId,
        String authorEmail,
        String secretPreviewToken,
        List<String> imageUrls,
        List<TagDto> tags,
        List<ContactInfoDto> contactInfos
) {
}
