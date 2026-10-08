package com.bigobooks.purchases_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bigobooks.entities.purchases.Purchase;
import com.bigobooks.entities.purchases.PurchaseDetail;
import com.bigobooks.entities.purchases.PurchaseStatus;
import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.purchases_service.dto.PurchaseCreateRequestDto;
import com.bigobooks.purchases_service.dto.PurchaseDetailRequestDto;
import com.bigobooks.purchases_service.dto.PurchaseDetailResponseDto;
import com.bigobooks.purchases_service.dto.PurchaseResponseDto;
import com.bigobooks.purchases_service.dto.PurchaseStatusUpdateRequestDto;
import com.bigobooks.purchases_service.exception.ConflictException;
import com.bigobooks.purchases_service.exception.NotFoundException;
import com.bigobooks.purchases_service.repository.PurchaseDetailRepository;
import com.bigobooks.purchases_service.repository.PurchaseRepository;
import com.bigobooks.purchases_service.repository.SupplierRepository;
import com.bigobooks.purchases_service.specifications.PurchaseSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

	private final PurchaseRepository purchaseRepository;
	private final PurchaseDetailRepository purchaseDetailRepository;
	private final SupplierRepository supplierRepository;
	private final StockService stockService;

	@Override
	@Transactional
	public PurchaseResponseDto create(PurchaseCreateRequestDto request) {
		Supplier supplier = supplierRepository.findById(request.getSupplierId())
				.orElseThrow(() -> new NotFoundException("No existe el proveedor con id " + request.getSupplierId()));

		Purchase purchase = new Purchase();
		purchase.setWarehouseId(request.getWarehouseId());
		purchase.setSupplier(supplier);
		purchase.setStatus(PurchaseStatus.PENDING);
		purchase.setInvoiceNumber(request.getInvoiceNumber().trim());
		purchase.setPurchaseDetails(new ArrayList<>());

		Purchase saved = purchaseRepository.save(purchase);

		for (PurchaseDetailRequestDto item : request.getDetails()) {
			addDetail(saved, item);
		}

		saved.setTotalPrice(calculateTotalPrice(saved.getPurchaseDetails()));
		Purchase result = purchaseRepository.save(saved);

		log.info("Compra id={} creada: {} detalle(s), proveedor={}, total={}", result.getId(),
				result.getPurchaseDetails().size(), supplier.getId(), result.getTotalPrice());
		return toResponse(result);
	}

	@Override
	@Transactional(readOnly = true)
	public PurchaseResponseDto getById(Long id) {
		Purchase purchase = requireById(id);
		log.info("Consulta de compra id={} (estado={})", purchase.getId(), purchase.getStatus());
		return toResponse(purchase);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PurchaseResponseDto> list(PurchaseStatus status, Long warehouseId, Long supplierId, Long bookId,
			String invoiceNumber, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable) {
		Specification<Purchase> spec = PurchaseSpecifications.build(status, warehouseId, supplierId, bookId,
				invoiceNumber, createdFrom, createdTo);
		Page<PurchaseResponseDto> page = purchaseRepository.findAll(spec, pageable).map(this::toResponse);
		log.info("Listado de compras: pagina {} con {} resultado(s)", page.getNumber(), page.getTotalElements());
		return page;
	}

	@Override
	@Transactional
	public PurchaseResponseDto updateStatus(Long id, PurchaseStatusUpdateRequestDto request) {
		if (request == null || request.getStatus() == null) {
			throw new IllegalArgumentException("status es obligatorio");
		}
		Purchase purchase = requireById(id);
		PurchaseStatus current = purchase.getStatus();
		PurchaseStatus newStatus = request.getStatus();

		if (current == newStatus) {
			throw new ConflictException("La compra ya esta en el estado " + current);
		}
		if (!isAllowedTransition(current, newStatus)) {
			throw new ConflictException("Transicion no permitida: " + current + " -> " + newStatus);
		}

		if (newStatus == PurchaseStatus.RECEIVED) {
			stockService.increaseStock(purchase);
		}

		purchase.setStatus(newStatus);
		Purchase saved = purchaseRepository.save(purchase);
		log.info("Compra id={}: estado {} -> {}", saved.getId(), current, newStatus);
		return toResponse(saved);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Purchase purchase = requireById(id);
		purchase.setDeleted(true);
		purchaseRepository.save(purchase);
		log.info("Compra id={} eliminada (borrado logico, factura={})", purchase.getId(), purchase.getInvoiceNumber());
	}

	private Purchase requireById(Long id) {
		return purchaseRepository.findByIdWithDetails(id)
				.orElseThrow(() -> new NotFoundException("No existe la compra con id " + id));
	}

	private void addDetail(Purchase purchase, PurchaseDetailRequestDto item) {
		PurchaseDetail detail = new PurchaseDetail();
		detail.setPurchase(purchase);
		detail.setBookId(item.getBookId());
		detail.setBookName(item.getBookName().trim());
		detail.setQuantity(item.getQuantity());
		detail.setUnitCost(item.getUnitCost());

		PurchaseDetail savedDetail = purchaseDetailRepository.save(detail);
		purchase.getPurchaseDetails().add(savedDetail);
	}

	private int calculateTotalPrice(List<PurchaseDetail> details) {
		long total = 0L;
		for (PurchaseDetail detail : details) {
			total += (long) detail.getQuantity() * detail.getUnitCost();
		}
		if (total > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("El total de la compra excede el maximo permitido");
		}
		return (int) total;
	}

	private boolean isAllowedTransition(PurchaseStatus from, PurchaseStatus to) {
		return switch (from) {
			case PENDING -> to == PurchaseStatus.RECEIVED || to == PurchaseStatus.CANCELLED;
			default -> false;
		};
	}

	private PurchaseResponseDto toResponse(Purchase purchase) {
		List<PurchaseDetailResponseDto> detailDtos = purchase.getPurchaseDetails() == null
				? List.of()
				: purchase.getPurchaseDetails().stream().map(this::toDetailResponse).toList();

		Long supplierId = purchase.getSupplier() != null ? purchase.getSupplier().getId() : null;
		String supplierName = purchase.getSupplier() != null ? purchase.getSupplier().getName() : null;

		return PurchaseResponseDto.builder()
				.id(purchase.getId())
				.warehouseId(purchase.getWarehouseId())
				.supplierId(supplierId)
				.supplierName(supplierName)
				.status(purchase.getStatus())
				.totalPrice(purchase.getTotalPrice())
				.invoiceNumber(purchase.getInvoiceNumber())
				.details(detailDtos)
				.createdAt(purchase.getCreatedAt())
				.build();
	}

	private PurchaseDetailResponseDto toDetailResponse(PurchaseDetail detail) {
		long lineTotal = (long) detail.getQuantity() * detail.getUnitCost();
		return PurchaseDetailResponseDto.builder()
				.id(detail.getId())
				.bookId(detail.getBookId())
				.bookName(detail.getBookName())
				.quantity(detail.getQuantity())
				.unitCost(detail.getUnitCost())
				.lineTotal(lineTotal)
				.build();
	}
}
