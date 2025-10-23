package io.github.michael_altf4.tasker.rest.resource;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResource {

    private Long id;
    private String text;
    private LocalDateTime createdAt;

}
