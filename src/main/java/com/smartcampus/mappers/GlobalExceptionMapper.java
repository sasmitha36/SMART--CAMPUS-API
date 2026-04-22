package com.smartcampus.mappers;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.smartcampus.model.ErrorResponse;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            int status = webApplicationException.getResponse().getStatus();
            String message = exception.getMessage() == null ? "Request failed." : exception.getMessage();
            ErrorResponse error = new ErrorResponse("request_failed", message, status);
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }

        if (exception instanceof IllegalArgumentException illegalArgumentException) {
            ErrorResponse error = new ErrorResponse("bad_request", illegalArgumentException.getMessage(), 400);
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }

        LOGGER.log(Level.SEVERE, "Unhandled exception in Smart Campus API.", exception);
        ErrorResponse error = new ErrorResponse(
                "internal_server_error",
                "An unexpected server error occurred. Please contact the API administrator.",
                500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
