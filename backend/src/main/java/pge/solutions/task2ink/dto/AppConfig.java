package pge.solutions.task2ink.dto;

import java.util.List;

public record AppConfig(
        int refreshInterval,
        List<CalDavCredential> calendars
) { }
