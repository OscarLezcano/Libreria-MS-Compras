package com.bigobooks.purchases_service.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bigobooks.entities.purchases.PurchaseStatus;
import com.bigobooks.purchases_service.dto.PurchaseCreateRequestDto;
import com.bigobooks.purchases_service.dto.PurchaseResponseDto;
import com.bigobooks.purchases_service.dto.PurchaseStatusUpdateRequestDto;

public interface PurchaseService {

	PurchaseResponseDto create(PurchaseCreateRequestDto request);

	PurchaseResponseDto getById(Long id);

	Page<PurchaseResponseDto> list(PurchaseStatus status, Long warehouseId, Long supplierId, Long bookId,
			String invoiceNumber, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable);

	PurchaseResponseDto updateStatus(Long id, PurchaseStatusUpdateRequestDto request);

	void delete(Long id);
}
