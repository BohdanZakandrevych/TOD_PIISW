package com.piisw.tod.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record AdUpdateRequestDto(
        @Size(max = 255) String title,
        @Size(max = 20000) String description,
        List<@Size(max = 2000) String> imageUrls,
        List<@Size(max = 50) String> tagNames,
        List<Long> contactInfoIds
) {
}
