package com.piisw.tod.dto;

import com.piisw.tod.model.AdStatus;
import jakarta.validation.constraints.NotNull;

public record AdStatusUpdateRequestDto(@NotNull AdStatus status) {
}
