package pge.solutions.task2ink.dto;

import net.fortuna.ical4j.model.component.VToDo;

import java.util.List;

public record TodoList(
        String name,
        List<VToDo> todos
) { }
