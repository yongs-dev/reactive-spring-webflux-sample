package com.mark.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoviesInfoClientException extends RuntimeException {
    private String message;
    @Getter
    private Integer statusCode;

    public MoviesInfoClientException(String message, Integer statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
