package com.bigobooks.purchases_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bigobooks.dto.SupplierDto;
import com.bigobooks.dto.SupplierCreateRequest;
import com.bigobooks.dto.SupplierUpdateRequest;

public interface SupplierService {

	SupplierDto create(SupplierCreateRequest request);

	SupplierDto update(Long id, SupplierUpdateRequest request);

	SupplierDto getById(Long id);

	Page<SupplierDto> list(Pageable pageable);

	void delete(Long id);
}
