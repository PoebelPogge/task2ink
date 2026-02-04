package pge.solutions.task2ink.dto;

import java.time.OffsetDateTime;

public record PrintableToDo(
        String id,
        String listName,
        String summary,
        String description,
        OffsetDateTime dueDateTime
) { }
