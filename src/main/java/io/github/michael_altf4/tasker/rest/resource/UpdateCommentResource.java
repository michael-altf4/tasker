package io.github.michael_altf4.tasker.rest.resource;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCommentResource {
    @NotBlank
    private String text;

}
