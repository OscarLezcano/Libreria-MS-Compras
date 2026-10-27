package com.bigobooks.purchases_service.exception;

import org.springframework.http.HttpStatus;

import com.bigobooks.util.exception.ApiException;

/**
 * Operacion no permitida en el estado actual del recurso (HTTP 409).
 */
public class ConflictException extends ApiException {

	public ConflictException(String message) {
		super(HttpStatus.CONFLICT, message);
	}
}
