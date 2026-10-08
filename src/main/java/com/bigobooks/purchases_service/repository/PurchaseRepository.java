package com.bigobooks.purchases_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bigobooks.entities.purchases.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long>, JpaSpecificationExecutor<Purchase> {

	@EntityGraph(attributePaths = { "purchaseDetails", "supplier" })
	@Query("select p from Purchase p where p.id = :id")
	Optional<Purchase> findByIdWithDetails(@Param("id") Long id);

	@EntityGraph(attributePaths = { "purchaseDetails", "supplier" })
	@Override
	Page<Purchase> findAll(Specification<Purchase> spec, Pageable pageable);
}
