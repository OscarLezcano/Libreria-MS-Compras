package com.bigobooks.purchases_service.repository;

import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends BaseRepository<Supplier>, JpaSpecificationExecutor<Supplier> {
	Optional<Supplier> findByNameIgnoreCase(String name);

	Optional<Supplier> findByRuc(String ruc);

	@Query("""
			select supplier from Supplier supplier
			where lower(supplier.name) like lower(concat('%', :search, '%'))
			   or lower(coalesce(supplier.ruc, '')) like lower(concat('%', :search, '%'))
			""")
	Page<Supplier> searchByNameOrRuc(@Param("search") String search, Pageable pageable);

	@Override
	@Query(value = "SELECT * FROM supplier", nativeQuery = true)
	List<Supplier> findAllIncludingDeleted();

	@Override
	@Query(value = "SELECT * FROM supplier WHERE is_deleted = true", nativeQuery = true)
	List<Supplier> findDeleted();
}
