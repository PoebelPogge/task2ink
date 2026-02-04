package pge.solutions.task2ink.dto;

import java.time.OffsetDateTime;

public record ApiToDo(
        String uid,
        String summary,
        String description,
        OffsetDateTime dueDateTime
) { }
