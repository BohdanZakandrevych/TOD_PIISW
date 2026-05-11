package com.piisw.tod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MessageSendRequestDto(
        @NotBlank @Size(max = 20000) String content,
        @Positive Long receiverId,
        @Positive Long relatedAdId
) {
}
