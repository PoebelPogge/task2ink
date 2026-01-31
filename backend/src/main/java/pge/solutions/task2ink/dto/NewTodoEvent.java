package pge.solutions.task2ink.dto;

import net.fortuna.ical4j.model.component.VToDo;

public record NewTodoEvent(
        VToDo toDo
) { }
