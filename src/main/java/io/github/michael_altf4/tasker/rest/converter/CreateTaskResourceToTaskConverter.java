package io.github.michael_altf4.tasker.rest.converter;

import io.github.michael_altf4.tasker.rest.resource.CreateTaskResource;
import io.github.michael_altf4.tasker.storage.model.Task;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateTaskResourceToTaskConverter implements Converter<CreateTaskResource, Task> {

    @Override
    public Task convert(CreateTaskResource request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setCompleted(false);
        return task;
    }
}