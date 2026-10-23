package com.bigobooks.purchases_service.service;

import com.bigobooks.dto.BookResponse;
import com.bigobooks.dto.PurchaseCreateRequest;
import com.bigobooks.dto.PurchaseDto;
import com.bigobooks.dto.PurchaseItemCreate;
import com.bigobooks.dto.PurchaseStatusChange;
import com.bigobooks.entities.purchases.Purchase;
import com.bigobooks.entities.purchases.PurchaseDetail;
import com.bigobooks.entities.purchases.PurchaseStatus;
import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.purchases_service.client.BookClient;
import com.bigobooks.purchases_service.exception.ConflictException;
import com.bigobooks.purchases_service.exception.NotFoundException;
import com.bigobooks.purchases_service.mappers.PurchaseMapper;
import com.bigobooks.purchases_service.repository.PurchaseDetailRepository;
import com.bigobooks.purchases_service.repository.PurchaseRepository;
import com.bigobooks.purchases_service.repository.SupplierRepository;
import com.bigobooks.purchases_service.specifications.PurchaseSpecifications;
import com.bigobooks.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PurchaseServiceImpl extends BaseService<Purchase, PurchaseRepository> implements PurchaseService {

    private final PurchaseDetailRepository purchaseDetailRepository;
    private final SupplierRepository supplierRepository;
    private final StockService stockService;
    private final PurchaseMapper purchaseMapper;
    private final BookClient bookClient;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
            PurchaseDetailRepository purchaseDetailRepository,
            SupplierRepository supplierRepository,
            StockService stockService,
            PurchaseMapper purchaseMapper,
            BookClient bookClient) {
        super(purchaseRepository);
        this.purchaseDetailRepository = purchaseDetailRepository;
        this.supplierRepository = supplierRepository;
        this.stockService = stockService;
        this.purchaseMapper = purchaseMapper;
        this.bookClient = bookClient;
    }

    @Override
    @Transactional
    public PurchaseDto create(PurchaseCreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new NotFoundException("No existe el proveedor con id " + request.getSupplierId()));

        Purchase purchase = new Purchase();
        if (request.getWarehouseId() != null) {
            purchase.setWarehouseId(request.getWarehouseId());
        }
        purchase.setSupplier(supplier);
        purchase.setStatus(PurchaseStatus.PENDING);
        if (request.getInvoiceNumber() != null) {
            purchase.setInvoiceNumber(request.getInvoiceNumber().trim());
        }
        purchase.setPurchaseDetails(new ArrayList<>());

        Purchase saved = save(purchase);

        for (PurchaseItemCreate item : request.getItems()) {
            addDetail(saved, item);
        }

        saved.setTotalPrice(calculateTotalPrice(saved.getPurchaseDetails()));
        Purchase result = save(saved);

        log.info("Compra id={} creada: {} detalle(s), proveedor={}, total={}", result.getId(),
                result.getPurchaseDetails().size(), supplier.getId(), result.getTotalPrice());
        return purchaseMapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "purchases", key = "#id")
    public PurchaseDto getById(Long id) {
        log.info("[CACHE MISS / DB HIT] Buscando compra con ID: {} en base de datos", id);
        Purchase purchase = requireById(id);
        log.info("Consulta de compra id={} (estado={})", purchase.getId(), purchase.getStatus());
        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseDto> list(PurchaseStatus status, Long warehouseId, Long supplierId, Long bookId,
            String invoiceNumber, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable) {
        // Sin @Cacheable: consultas masivas y paginadas van directo a la base de datos
        Specification<Purchase> spec = PurchaseSpecifications.build(status, warehouseId, supplierId, bookId,
                invoiceNumber, createdFrom, createdTo);
        Page<PurchaseDto> page = getRepository().findAll(spec, pageable).map(purchaseMapper::toResponse);
        log.info("Listado de compras: pagina {} con {} resultado(s)", page.getNumber(), page.getTotalElements());
        return page;
    }

    @Override
    @Transactional
    @CacheEvict(value = "purchases", key = "#id")
    public PurchaseDto updateStatus(Long id, PurchaseStatusChange request) {
        log.info("[CACHE EVICT] Desalojando compra ID: {} de Redis por cambio de estado", id);
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("status es obligatorio");
        }
        Purchase purchase = requireById(id);
        PurchaseStatus current = purchase.getStatus();

        // Conversión segura de OpenAPI enum al entity enum
        PurchaseStatus newStatus = PurchaseStatus.valueOf(request.getStatus().name());

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
        Purchase saved = save(purchase);
        log.info("Compra id={}: estado {} -> {}", saved.getId(), current, newStatus);
        return purchaseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "purchases", key = "#id")
    public void delete(Long id) {
        log.info("[CACHE EVICT] Desalojando compra ID: {} de Redis por borrado", id);
        Purchase purchase = requireById(id);
        purchase.setDeleted(true);
        save(purchase);
        log.info("Compra id={} eliminada (borrado logico, factura={})", purchase.getId(), purchase.getInvoiceNumber());
    }

    private Purchase requireById(Long id) {
        return getRepository().findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("No existe la compra con id " + id));
    }

    private void addDetail(Purchase purchase, PurchaseItemCreate item) {
        PurchaseDetail detail = new PurchaseDetail();
        detail.setPurchase(purchase);
        detail.setBookId(item.getBookId());
        detail.setQuantity(item.getQuantity());
        detail.setUnitCost(item.getUnitCost());
        try {
            BookResponse response = bookClient.getBookById(item.getBookId());
            if (response != null && response.getData() != null) {
                detail.setBookName(response.getData().getTitle());
            }
        } catch (Exception e) {
            // Al fallar la conexión con stock-service, asigna este texto de respaldo:
            log.warn("No se pudo conectar a stock-service, asignando nombre simulado: {}", e.getMessage());
            detail.setBookName("Libro de Prueba (ID " + item.getBookId() + ")");
        }

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
}