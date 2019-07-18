package com.github.adrian83.robome.common.web;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.util.http.Cors;
import com.google.inject.Inject;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;

public class ExceptionHandler {

	private ObjectMapper objectMapper;

	@Inject
	public ExceptionHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public HttpResponse handleException(Throwable ex) {
		
		System.out.println("EXCEPTION: " + ex.getMessage());
		if(ex instanceof CompletionException) {
			return handleException(ex.getCause());
		} else if (ex instanceof ValidationException) {
			return response400(((ValidationException) ex).getErrors());
		} else if (ex instanceof InvalidSignInDataException) {
			return response400(List.of(new ValidationError("email", "login.invalid", "Invalida email or password")));
		}
		return HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR);
	}

	protected HttpResponse response400(List<ValidationError> validationErrors) {
		return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST)
				.withEntity(ContentTypes.APPLICATION_JSON, toBytes(validationErrors))
				.addHeaders(headers(Cors.allowHeaders("Content-Type"), Cors.origin("*"), Cors.methods("POST")));
	}

	protected String toBytes(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	protected List<HttpHeader> headers(HttpHeader... headers) {
		return Arrays.asList(headers);
	}

}