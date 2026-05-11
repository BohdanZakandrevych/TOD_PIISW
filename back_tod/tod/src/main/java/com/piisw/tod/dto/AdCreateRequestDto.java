package com.piisw.tod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdCreateRequestDto(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String description,
        List<@Size(max = 2000) String> imageUrls,
        List<@NotBlank @Size(max = 50) String> tagNames,
        List<Long> contactInfoIds
) {
}
