package com.bigobooks.purchases_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bigobooks.entities.purchases.Purchase;
import com.bigobooks.repository.BaseRepository;

@Repository
public interface PurchaseRepository extends BaseRepository<Purchase>, JpaSpecificationExecutor<Purchase> {
	@Query("SELECT p FROM Purchase p LEFT JOIN FETCH p.purchaseDetails WHERE p.id = :id")
	Optional<Purchase> findByIdWithDetails(@Param("id") Long id);
}