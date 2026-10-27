package com.bigobooks.purchases_service.exception;

import org.springframework.http.HttpStatus;

import com.bigobooks.util.exception.ApiException;

public class NotFoundException extends ApiException {

	public NotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}
}
