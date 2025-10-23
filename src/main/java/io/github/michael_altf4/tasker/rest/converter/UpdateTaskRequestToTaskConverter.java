package io.github.michael_altf4.tasker.rest.converter;


import io.github.michael_altf4.tasker.rest.resource.UpdateTaskResource;
import io.github.michael_altf4.tasker.storage.model.Task;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UpdateTaskRequestToTaskConverter implements Converter<UpdateTaskResource, Task> {

    @Override
    public Task convert(UpdateTaskResource request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }
        task.setPriority(request.getPriority());
        return task;
    }
}