package com.bigobooks.purchases_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bigobooks.entities.purchases.Purchase;
import com.bigobooks.entities.purchases.PurchaseDetail;
import com.bigobooks.entities.stock.Stock;
import com.bigobooks.purchases_service.repository.StockRepository;
import com.bigobooks.entities.stock.Warehouse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Incrementa el stock de cada detalle al recibir la compra (RECEIVED).
     * Si no existe registro de stock para el libro en el almacen, se crea
     * automaticamente.
     */
    @Transactional
    public void increaseStock(Purchase purchase) {
        Long warehouseId = purchase.getWarehouseId();
        if (warehouseId == null) {
            throw new IllegalArgumentException("La compra debe indicar un almacen antes de ingresar el stock");
        }
        if (purchase.getPurchaseDetails() == null || purchase.getPurchaseDetails().isEmpty()) {
            throw new IllegalArgumentException("La compra no tiene detalles para ingresar al stock");
        }

        for (PurchaseDetail detail : purchase.getPurchaseDetails()) {
            Stock stock = getOrCreateStockFor(detail.getBookId(), warehouseId);
            stockRepository.increaseQuantity(stock.getId(), detail.getQuantity());
            log.debug("Stock libro {} almacen {}: +{}", detail.getBookId(), warehouseId, detail.getQuantity());
        }
        log.info("Stock incrementado para la compra {} en el almacen {}", purchase.getId(), warehouseId);
    }

    private Stock getOrCreateStockFor(Long bookId, Long warehouseId) {
        return stockRepository.findByBookIdAndWarehouse_Id(bookId, warehouseId)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setBookId(bookId);
                    newStock.setQuantity(0);
                    // Obtiene una referencia proxy de Warehouse con el ID sin hacer SELECT extra
                    Warehouse warehouseRef = entityManager.getReference(Warehouse.class, warehouseId);
                    newStock.setWarehouse(warehouseRef);

                    Stock saved = stockRepository.save(newStock);
                    log.info("Registro de stock creado: libro={}, almacen={}", bookId, warehouseId);
                    return saved;
                });
    }
}