package com.bigobooks.purchases_service.repository;

import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.repository.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends BaseRepository<Supplier>, JpaSpecificationExecutor<Supplier> {
	Optional<Supplier> findByNameIgnoreCase(String name);

	Optional<Supplier> findByRuc(String ruc);

	@Override
	@Query(value = "SELECT * FROM supplier", nativeQuery = true)
	List<Supplier> findAllIncludingDeleted();

	@Override
	@Query(value = "SELECT * FROM supplier WHERE is_deleted = true", nativeQuery = true)
	List<Supplier> findDeleted();
}