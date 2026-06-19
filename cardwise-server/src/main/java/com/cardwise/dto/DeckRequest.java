package com.cardwise.dto;

import jakarta.validation.constraints.NotBlank;

public class DeckRequest {
    @NotBlank
    private String name;
    private String description;
    private String color;

    public @NotBlank String getName() { return name; }
    public void setName(@NotBlank String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
