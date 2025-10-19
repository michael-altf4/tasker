package io.github.michael_altf4.tasker.rest.resource;

import jakarta.validation.constraints.NotBlank;

public class UpdateCommentResource {
    @NotBlank
    private String text;

    public @NotBlank String getText() {
        return text;
    }

    public void setText(@NotBlank String text) {
        this.text = text;
    }
}
