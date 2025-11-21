package org.example.controller.errorHandling;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Simple error payload returned by the global exception handler. */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {

    public static ApiError from(HttpStatusCode status, String message, String path) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String errorText = resolved != null ? resolved.getReasonPhrase() : status.toString();
        return new ApiError(Instant.now(), status.value(), errorText, message, path);
    }
}
