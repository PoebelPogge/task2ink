package pge.solutions.task2ink.exceptions;

import lombok.Getter;

public class CalenderConnectionException extends Exception{
    @Getter
    private final String calenderUrl;

    public CalenderConnectionException(String calenderUrl) {
        this.calenderUrl = calenderUrl;
    }
}
