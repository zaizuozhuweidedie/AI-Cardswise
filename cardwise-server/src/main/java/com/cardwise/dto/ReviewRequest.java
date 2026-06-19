package com.cardwise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewRequest {
    @NotNull @Min(1) @Max(4)
    private Integer quality;

    public @NotNull @Min(1) @Max(4) Integer getQuality() { return quality; }
    public void setQuality(@NotNull @Min(1) @Max(4) Integer quality) { this.quality = quality; }
}
