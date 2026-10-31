package com.bigobooks.purchases_service.service;

import com.bigobooks.dto.PurchaseCreateRequest;
import com.bigobooks.dto.PurchaseDetailDto;
import com.bigobooks.dto.PurchaseDto;
import com.bigobooks.dto.PurchaseStatusChange;
import com.bigobooks.entities.purchases.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseService {

    PurchaseDto create(Long warehouseId, PurchaseCreateRequest request);

    PurchaseDto getById(Long id);

    Optional<List<PurchaseDetailDto>> getDetails(Long id);

    Page<PurchaseDto> list(PurchaseStatus status, Long warehouseId, Long supplierId, Long bookId,
                           String invoiceNumber, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable);

    PurchaseDto updateStatus(Long id, PurchaseStatusChange request);

    void delete(Long id);
}