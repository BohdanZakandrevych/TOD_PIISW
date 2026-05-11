package com.piisw.tod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageReplyRequestDto(
        @NotBlank @Size(max = 20000) String content
) {
}
