package com.bigobooks.purchases_service.service;

import com.bigobooks.dto.PurchaseCreateRequest;
import com.bigobooks.dto.PurchaseDto;
import com.bigobooks.dto.PurchaseStatusChange;
import com.bigobooks.entities.purchases.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PurchaseService {

    PurchaseDto create(PurchaseCreateRequest request);

    PurchaseDto getById(Long id);

    Page<PurchaseDto> list(PurchaseStatus status, Long warehouseId, Long supplierId, Long bookId,
                           String invoiceNumber, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable);

    PurchaseDto updateStatus(Long id, PurchaseStatusChange request);

    void delete(Long id);
}