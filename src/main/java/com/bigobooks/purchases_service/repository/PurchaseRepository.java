package com.bigobooks.purchases_service.repository;

import java.util.List;
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

	@Override
	@Query(value = "SELECT * FROM purchase", nativeQuery = true)
	List<Purchase> findAllIncludingDeleted();

	@Override
	@Query(value = "SELECT * FROM purchase WHERE is_deleted = true", nativeQuery = true)
	List<Purchase> findDeleted();
}