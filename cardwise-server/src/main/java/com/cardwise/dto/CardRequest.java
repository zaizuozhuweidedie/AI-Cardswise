package com.cardwise.dto;

import jakarta.validation.constraints.NotBlank;

public class CardRequest {
    @NotBlank
    private String front;
    @NotBlank
    private String back;
    private String tags;

    public @NotBlank String getFront() { return front; }
    public void setFront(@NotBlank String front) { this.front = front; }
    public @NotBlank String getBack() { return back; }
    public void setBack(@NotBlank String back) { this.back = back; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
