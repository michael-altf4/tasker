package io.github.michael_altf4.tasker.rest.converter;

import io.github.michael_altf4.tasker.rest.resource.TaskResource;
import io.github.michael_altf4.tasker.storage.model.Task;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TaskToTaskResourceConverter implements Converter<Task, TaskResource> {

    @Override
    public TaskResource convert(Task task) {
        TaskResource resource = new TaskResource();
        resource.setId(task.getId());
        resource.setTitle(task.getTitle());
        resource.setDescription(task.getDescription());
        resource.setCompleted(task.isCompleted());
        resource.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        resource.setCreatedAt(task.getCreatedAt());
        return resource;
    }
}