package com.bigobooks.purchases_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bigobooks.purchases_service.dto.SupplierRequestDto;
import com.bigobooks.purchases_service.dto.SupplierResponseDto;

public interface SupplierService {

	SupplierResponseDto create(SupplierRequestDto request);

	SupplierResponseDto update(Long id, SupplierRequestDto request);

	SupplierResponseDto getById(Long id);

	Page<SupplierResponseDto> list(Pageable pageable);

	void delete(Long id);
}
