package pge.solutions.task2ink.dto;

import java.util.List;

public record ApiToDoList(
        String listName,
        List<ApiToDo> toDoList
) { }
