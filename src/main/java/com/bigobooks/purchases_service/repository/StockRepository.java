package com.bigobooks.purchases_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.bigobooks.entities.stock.Stock;
import com.bigobooks.repository.BaseRepository;

public interface StockRepository extends BaseRepository<Stock> {
    Optional<Stock> findByBookIdAndWarehouse_Id(Long bookId, Long warehouseId);

    @Modifying(clearAutomatically = true)
    @Query("update Stock s set s.quantity = s.quantity + :quantity where s.id = :id")
    int increaseQuantity(@Param("id") Long id, @Param("quantity") int quantity);
}